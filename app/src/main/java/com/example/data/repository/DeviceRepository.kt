package com.example.data.repository

import com.example.data.model.AuditLogEntry
import com.example.data.model.ComplianceStatus
import com.example.data.model.DeviceStatus
import com.example.data.model.ManagedDevice
import com.example.data.model.PlatformType
import com.example.data.model.SecurityPolicy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class DeviceRepository {

    private val _devices = MutableStateFlow<List<ManagedDevice>>(initialDevices())
    val devices: StateFlow<List<ManagedDevice>> = _devices.asStateFlow()

    private val _policies = MutableStateFlow<List<SecurityPolicy>>(initialPolicies())
    val policies: StateFlow<List<SecurityPolicy>> = _policies.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AuditLogEntry>>(initialAuditLogs())
    val auditLogs: StateFlow<List<AuditLogEntry>> = _auditLogs.asStateFlow()

    fun addDevice(device: ManagedDevice) {
        val updated = listOf(device) + _devices.value
        _devices.value = updated
        addAudit(
            deviceId = device.id,
            deviceName = device.name,
            platform = device.platform,
            actionType = "ENROLLMENT",
            details = "Perangkat berhasil didaftarkan ke konsol MDM OmniFleet."
        )
    }

    fun deleteDevice(deviceId: String) {
        val target = _devices.value.find { it.id == deviceId }
        _devices.value = _devices.value.filterNot { it.id == deviceId }
        if (target != null) {
            addAudit(
                deviceId = target.id,
                deviceName = target.name,
                platform = target.platform,
                actionType = "UNENROLL",
                details = "Pendaftaran perangkat dicabut dan dihapus dari armada."
            )
        }
    }

    fun lockDevice(deviceId: String) {
        _devices.value = _devices.value.map { dev ->
            if (dev.id == deviceId) {
                dev.copy(
                    status = DeviceStatus.LOCKED,
                    lastSeenTimestamp = System.currentTimeMillis()
                )
            } else dev
        }
        val target = _devices.value.find { it.id == deviceId }
        if (target != null) {
            addAudit(
                deviceId = target.id,
                deviceName = target.name,
                platform = target.platform,
                actionType = "REMOTE_LOCK",
                details = "Perintah penguncian jarak jauh berhasil dieksekusi. Layar dikunci segera."
            )
        }
    }

    fun unlockDevice(deviceId: String) {
        _devices.value = _devices.value.map { dev ->
            if (dev.id == deviceId) {
                dev.copy(
                    status = DeviceStatus.ONLINE,
                    lastSeenTimestamp = System.currentTimeMillis()
                )
            } else dev
        }
        val target = _devices.value.find { it.id == deviceId }
        if (target != null) {
            addAudit(
                deviceId = target.id,
                deviceName = target.name,
                platform = target.platform,
                actionType = "REMOTE_UNLOCK",
                details = "Kunci perangkat berhasil dibuka kembali oleh administrator."
            )
        }
    }

    fun rebootDevice(deviceId: String) {
        _devices.value = _devices.value.map { dev ->
            if (dev.id == deviceId) {
                dev.copy(
                    status = DeviceStatus.SYNCING,
                    lastSeenTimestamp = System.currentTimeMillis()
                )
            } else dev
        }
        val target = _devices.value.find { it.id == deviceId }
        if (target != null) {
            addAudit(
                deviceId = target.id,
                deviceName = target.name,
                platform = target.platform,
                actionType = "REBOOT",
                details = "Sinyal reboot jarak jauh terkirim. Daemon OS memulai reboot aman."
            )
        }
    }

    fun syncDevice(deviceId: String) {
        _devices.value = _devices.value.map { dev ->
            if (dev.id == deviceId) {
                dev.copy(
                    status = DeviceStatus.ONLINE,
                    lastSeenTimestamp = System.currentTimeMillis()
                )
            } else dev
        }
        val target = _devices.value.find { it.id == deviceId }
        if (target != null) {
            addAudit(
                deviceId = target.id,
                deviceName = target.name,
                platform = target.platform,
                actionType = "POLICY_SYNC",
                details = "Sinkronisasi profil konfigurasi dan sertifikat berhasil diperbarui."
            )
        }
    }

    fun wipeDevice(deviceId: String) {
        _devices.value = _devices.value.map { dev ->
            if (dev.id == deviceId) {
                dev.copy(
                    status = DeviceStatus.WIPED,
                    compliance = ComplianceStatus.NON_COMPLIANT,
                    complianceNotes = "Perangkat telah di-wipe dari jarak jauh.",
                    lastSeenTimestamp = System.currentTimeMillis()
                )
            } else dev
        }
        val target = _devices.value.find { it.id == deviceId }
        if (target != null) {
            addAudit(
                deviceId = target.id,
                deviceName = target.name,
                platform = target.platform,
                actionType = "REMOTE_WIPE",
                details = "Perintah Enterprise Wipe terkirim: Seluruh kontainer data perusahaan dihapus tuntas."
            )
        }
    }

    fun scanDevice(deviceId: String) {
        val target = _devices.value.find { it.id == deviceId }
        if (target != null) {
            addAudit(
                deviceId = target.id,
                deviceName = target.name,
                platform = target.platform,
                actionType = "SECURITY_SCAN",
                details = "Pemindaian ancaman dan integritas kernel selesai. Tidak ditemukan malware."
            )
        }
    }

    fun togglePolicy(policyId: String) {
        _policies.value = _policies.value.map { policy ->
            if (policy.id == policyId) {
                policy.copy(isEnabled = !policy.isEnabled)
            } else policy
        }
        val changed = _policies.value.find { it.id == policyId }
        if (changed != null) {
            addAudit(
                deviceId = "POLICY_ENGINE",
                deviceName = "Kebijakan: ${changed.title}",
                platform = changed.targetPlatform ?: PlatformType.WINDOWS,
                actionType = "POLICY_UPDATE",
                details = "Kebijakan '${changed.title}' diubah status menjadi ${if (changed.isEnabled) "AKTIF" else "NONAKTIF"}."
            )
        }
        reevaluateFleetCompliance()
    }

    private fun reevaluateFleetCompliance() {
        val activePolicies = _policies.value.filter { it.isEnabled }
        _devices.value = _devices.value.map { dev ->
            if (dev.status == DeviceStatus.WIPED) return@map dev

            val relevantPolicies = activePolicies.filter { it.targetPlatform == null || it.targetPlatform == dev.platform }
            val issues = mutableListOf<String>()

            for (p in relevantPolicies) {
                if (p.requireEncryption && !dev.isEncrypted) {
                    issues.add("Enkripsi disk belum aktif")
                }
                if (p.requirePasscode && !dev.isPasscodeSet) {
                    issues.add("PIN / Sandi belum diterapkan")
                }
                if (p.blockRootJailbreak && dev.isJailbrokenOrRooted) {
                    issues.add("Terdeteksi Root/Jailbreak")
                }
            }

            when {
                issues.contains("Terdeteksi Root/Jailbreak") || issues.size >= 2 -> {
                    dev.copy(
                        compliance = ComplianceStatus.NON_COMPLIANT,
                        complianceNotes = issues.joinToString(", ")
                    )
                }
                issues.isNotEmpty() -> {
                    dev.copy(
                        compliance = ComplianceStatus.WARNING,
                        complianceNotes = issues.joinToString(", ")
                    )
                }
                else -> {
                    dev.copy(
                        compliance = ComplianceStatus.COMPLIANT,
                        complianceNotes = "Semua parameter keamanan memenuhi standar perusahaan"
                    )
                }
            }
        }
    }

    private fun addAudit(deviceId: String, deviceName: String, platform: PlatformType, actionType: String, details: String) {
        val entry = AuditLogEntry(
            id = UUID.randomUUID().toString(),
            deviceId = deviceId,
            deviceName = deviceName,
            platform = platform,
            actionType = actionType,
            performedBy = "Admin (IT SecOps)",
            timestamp = System.currentTimeMillis(),
            isSuccess = true,
            details = details
        )
        _auditLogs.value = listOf(entry) + _auditLogs.value
    }

    companion object {
        fun initialDevices(): List<ManagedDevice> {
            val now = System.currentTimeMillis()
            return listOf(
                // iOS Devices
                ManagedDevice(
                    id = "ios-001",
                    name = "MacBook Pro M3 Max - CEO",
                    platform = PlatformType.IOS,
                    model = "MacBook Pro 16\" (M3 Max)",
                    osVersion = "macOS Sequoia 15.1",
                    serialNumber = "C02G879XMD6R",
                    assignedUser = "Budi Hartono",
                    department = "Executive",
                    ipAddress = "10.240.12.44",
                    macAddress = "A4:83:E7:22:11:BC",
                    storageTotalGb = 1000,
                    storageUsedGb = 412,
                    batteryLevel = 88,
                    isCharging = true,
                    isEncrypted = true,
                    isPasscodeSet = true,
                    isJailbrokenOrRooted = false,
                    status = DeviceStatus.ONLINE,
                    compliance = ComplianceStatus.COMPLIANT,
                    lastSeenTimestamp = now - 180_000,
                    enrolledTimestamp = now - 90L * 86_400_000,
                    complianceNotes = "FileVault XTS-AES 128 aktif, SIP Enforcing, MDM Supervised",
                    agentVersion = "OmniAgent v4.2.0-darwin",
                    platformSpecificDetail = "Apple Business Manager (DEP), Supervised: Ya, MDM Push Token Terverifikasi"
                ),
                ManagedDevice(
                    id = "ios-002",
                    name = "iPhone 15 Pro - VP Sales",
                    platform = PlatformType.IOS,
                    model = "iPhone 15 Pro 256GB",
                    osVersion = "iOS 18.0.1",
                    serialNumber = "F2LN88390MD1",
                    assignedUser = "Dewi Sartika",
                    department = "Sales",
                    ipAddress = "10.240.14.98",
                    macAddress = "F0:18:98:54:33:AA",
                    storageTotalGb = 256,
                    storageUsedGb = 118,
                    batteryLevel = 74,
                    isCharging = false,
                    isEncrypted = true,
                    isPasscodeSet = true,
                    isJailbrokenOrRooted = false,
                    status = DeviceStatus.ONLINE,
                    compliance = ComplianceStatus.COMPLIANT,
                    lastSeenTimestamp = now - 600_000,
                    enrolledTimestamp = now - 45L * 86_400_000,
                    complianceNotes = "Data Protection Class A aktif, Face ID Biometric Enforced",
                    agentVersion = "OmniAgent v4.2.0-ios",
                    platformSpecificDetail = "Supervised: Ya, Per-App VPN Aktif, Remote Lock Siaga"
                ),
                ManagedDevice(
                    id = "ios-003",
                    name = "iPad Air M2 - Gudang Logistik",
                    platform = PlatformType.IOS,
                    model = "iPad Air 11-inch (M2)",
                    osVersion = "iPadOS 17.5.1",
                    serialNumber = "DMPTJ198L10F",
                    assignedUser = "Ahmad Syakir",
                    department = "Operations",
                    ipAddress = "10.240.20.15",
                    macAddress = "3C:22:FB:99:41:02",
                    storageTotalGb = 128,
                    storageUsedGb = 95,
                    batteryLevel = 22,
                    isCharging = false,
                    isEncrypted = true,
                    isPasscodeSet = false,
                    isJailbrokenOrRooted = false,
                    status = DeviceStatus.ONLINE,
                    compliance = ComplianceStatus.WARNING,
                    lastSeenTimestamp = now - 1_200_000,
                    enrolledTimestamp = now - 120L * 86_400_000,
                    complianceNotes = "PIN sandi layar belum diperbarui sesuai kebijakan rotasi 90 hari",
                    agentVersion = "OmniAgent v4.1.8-ios",
                    platformSpecificDetail = "Single App Kiosk Mode: Enterprise ERP Warehouse"
                ),

                // Android Devices
                ManagedDevice(
                    id = "and-001",
                    name = "Google Pixel 8 Pro - Lead Mobile",
                    platform = PlatformType.ANDROID,
                    model = "Pixel 8 Pro (Enterprise)",
                    osVersion = "Android 15 (AP2A)",
                    serialNumber = "3A141FDH20005C",
                    assignedUser = "Rizki Pratama",
                    department = "Engineering",
                    ipAddress = "10.240.10.72",
                    macAddress = "9C:FC:01:88:AB:12",
                    storageTotalGb = 256,
                    storageUsedGb = 84,
                    batteryLevel = 92,
                    isCharging = true,
                    isEncrypted = true,
                    isPasscodeSet = true,
                    isJailbrokenOrRooted = false,
                    status = DeviceStatus.ONLINE,
                    compliance = ComplianceStatus.COMPLIANT,
                    lastSeenTimestamp = now - 60_000,
                    enrolledTimestamp = now - 60L * 86_400_000,
                    complianceNotes = "Android Enterprise Fully Managed (COBO), Play Protect Aktif",
                    agentVersion = "OmniAgent v4.2.0-android",
                    platformSpecificDetail = "Titan M2 Hardware Key, StrongBox Keymaster, Profil Kerja Terisolasi"
                ),
                ManagedDevice(
                    id = "and-002",
                    name = "Samsung Galaxy Tab S9 - Field Tech",
                    platform = PlatformType.ANDROID,
                    model = "Galaxy Tab S9 FE 5G",
                    osVersion = "Android 14 (OneUI 6.1)",
                    serialNumber = "R52W801K9LM",
                    assignedUser = "Indra Gunawan",
                    department = "Field Support",
                    ipAddress = "10.240.33.109",
                    macAddress = "68:72:C3:44:81:6F",
                    storageTotalGb = 128,
                    storageUsedGb = 62,
                    batteryLevel = 61,
                    isCharging = false,
                    isEncrypted = true,
                    isPasscodeSet = true,
                    isJailbrokenOrRooted = false,
                    status = DeviceStatus.ONLINE,
                    compliance = ComplianceStatus.COMPLIANT,
                    lastSeenTimestamp = now - 900_000,
                    enrolledTimestamp = now - 30L * 86_400_000,
                    complianceNotes = "Samsung Knox Enterprise Attestation: PASSED (Warranty 0x0)",
                    agentVersion = "OmniAgent v4.2.0-android",
                    platformSpecificDetail = "Knox Platform for Enterprise, Dual-DAR Enkripsi Data Aktif"
                ),
                ManagedDevice(
                    id = "and-003",
                    name = "Xiaomi Pad 6 - QA Staging",
                    platform = PlatformType.ANDROID,
                    model = "Xiaomi Pad 6 Pro",
                    osVersion = "Android 13 (HyperOS 1.0)",
                    serialNumber = "88931AA020199B",
                    assignedUser = "Bambang Sudiro",
                    department = "QA Testing",
                    ipAddress = "10.240.10.198",
                    macAddress = "44:55:C4:11:32:09",
                    storageTotalGb = 128,
                    storageUsedGb = 110,
                    batteryLevel = 45,
                    isCharging = false,
                    isEncrypted = false,
                    isPasscodeSet = false,
                    isJailbrokenOrRooted = true,
                    status = DeviceStatus.ONLINE,
                    compliance = ComplianceStatus.NON_COMPLIANT,
                    lastSeenTimestamp = now - 3_600_000,
                    enrolledTimestamp = now - 15L * 86_400_000,
                    complianceNotes = "Terdeteksi Magisk SU Binary (Root), Enkripsi File-Based dimodifikasi",
                    agentVersion = "OmniAgent v3.9.4-android",
                    platformSpecificDetail = "Bootloader Status: UNLOCKED! Blokir akses VPN & database internal!"
                ),

                // Windows Devices
                ManagedDevice(
                    id = "win-001",
                    name = "Lenovo ThinkPad X1 - DevOps Lead",
                    platform = PlatformType.WINDOWS,
                    model = "ThinkPad X1 Carbon Gen 11",
                    osVersion = "Windows 11 Enterprise 23H2",
                    serialNumber = "PF4M9021",
                    assignedUser = "Farhan Fauzi",
                    department = "DevOps & Cloud",
                    ipAddress = "10.240.10.15",
                    macAddress = "00:2B:67:88:51:7E",
                    storageTotalGb = 512,
                    storageUsedGb = 289,
                    batteryLevel = 98,
                    isCharging = true,
                    isEncrypted = true,
                    isPasscodeSet = true,
                    isJailbrokenOrRooted = false,
                    status = DeviceStatus.ONLINE,
                    compliance = ComplianceStatus.COMPLIANT,
                    lastSeenTimestamp = now - 45_000,
                    enrolledTimestamp = now - 180L * 86_400_000,
                    complianceNotes = "BitLocker XTS-AES 256 Enforced, TPM 2.0 Secure Boot Validated",
                    agentVersion = "OmniAgent v4.2.0-win64",
                    platformSpecificDetail = "Defender ATP Realtime: Bersih, Windows Hello PIN + Fingerprint Enforced"
                ),
                ManagedDevice(
                    id = "win-002",
                    name = "Dell Latitude 5440 - Finance Staff",
                    platform = PlatformType.WINDOWS,
                    model = "Dell Latitude 5440 i7-1365U",
                    osVersion = "Windows 11 Pro 22H2",
                    serialNumber = "8HG7TY2",
                    assignedUser = "Siti Rahmawati",
                    department = "Finance",
                    ipAddress = "10.240.16.88",
                    macAddress = "50:9A:4C:12:33:FF",
                    storageTotalGb = 512,
                    storageUsedGb = 490,
                    batteryLevel = 67,
                    isCharging = false,
                    isEncrypted = true,
                    isPasscodeSet = true,
                    isJailbrokenOrRooted = false,
                    status = DeviceStatus.ONLINE,
                    compliance = ComplianceStatus.WARNING,
                    lastSeenTimestamp = now - 180_000,
                    enrolledTimestamp = now - 210L * 86_400_000,
                    complianceNotes = "Penyimpanan hampir penuh (>95%), versi OS perlu pembaruan ke 23H2",
                    agentVersion = "OmniAgent v4.1.9-win64",
                    platformSpecificDetail = "BitLocker Kunci Pemulihan tersimpan di Azure AD / Fleet Vault"
                ),
                ManagedDevice(
                    id = "win-003",
                    name = "HP Elite Dragonfly - Regional Mgr",
                    platform = PlatformType.WINDOWS,
                    model = "HP Dragonfly G4",
                    osVersion = "Windows 11 Enterprise 23H2",
                    serialNumber = "5CD3380XZ1",
                    assignedUser = "Andi Wijaya",
                    department = "Management",
                    ipAddress = "192.168.1.105",
                    macAddress = "E4:54:E8:90:3A:41",
                    storageTotalGb = 1000,
                    storageUsedGb = 320,
                    batteryLevel = 15,
                    isCharging = false,
                    isEncrypted = true,
                    isPasscodeSet = true,
                    isJailbrokenOrRooted = false,
                    status = DeviceStatus.OFFLINE,
                    compliance = ComplianceStatus.COMPLIANT,
                    lastSeenTimestamp = now - 86_400_000 * 2,
                    enrolledTimestamp = now - 150L * 86_400_000,
                    complianceNotes = "BitLocker Aktif, Terakhir sinkronisasi 2 hari lalu",
                    agentVersion = "OmniAgent v4.2.0-win64",
                    platformSpecificDetail = "Koneksi VPN Always-On terputus saat offline"
                ),

                // Linux Devices
                ManagedDevice(
                    id = "lnx-001",
                    name = "Ubuntu DevStation - Backend Core",
                    platform = PlatformType.LINUX,
                    model = "Dell Precision 5820 Workstation",
                    osVersion = "Ubuntu 24.04 LTS (Noble)",
                    serialNumber = "SRV-LNX-UB2404",
                    assignedUser = "Aditya Pratama",
                    department = "Backend Core",
                    ipAddress = "10.240.10.5",
                    macAddress = "D8:5E:D3:10:99:A1",
                    storageTotalGb = 2000,
                    storageUsedGb = 780,
                    batteryLevel = 100, // AC powered
                    isCharging = true,
                    isEncrypted = true,
                    isPasscodeSet = true,
                    isJailbrokenOrRooted = false,
                    status = DeviceStatus.ONLINE,
                    compliance = ComplianceStatus.COMPLIANT,
                    lastSeenTimestamp = now - 12_000,
                    enrolledTimestamp = now - 300L * 86_400_000,
                    complianceNotes = "LUKS2 Full Disk Encryption, AppArmor Enforcing, Kernel 6.8.0-45-generic",
                    agentVersion = "OmniAgent v4.2.0-linux-systemd",
                    platformSpecificDetail = "SSH Key Agent Enrolled, Sudoers Audit Log Enabled, ClamAV Daemon Aktif"
                ),
                ManagedDevice(
                    id = "lnx-002",
                    name = "Fedora Workstation - AI Scientist",
                    platform = PlatformType.LINUX,
                    model = "System76 Thelio Major (NVIDIA RTX 4090)",
                    osVersion = "Fedora 40 Workstation",
                    serialNumber = "SYS76-THELIO-9921",
                    assignedUser = "Dr. Maya Kusuma",
                    department = "AI Research",
                    ipAddress = "10.240.10.88",
                    macAddress = "70:85:C2:55:40:DE",
                    storageTotalGb = 4000,
                    storageUsedGb = 2100,
                    batteryLevel = 100,
                    isCharging = true,
                    isEncrypted = true,
                    isPasscodeSet = true,
                    isJailbrokenOrRooted = false,
                    status = DeviceStatus.ONLINE,
                    compliance = ComplianceStatus.COMPLIANT,
                    lastSeenTimestamp = now - 30_000,
                    enrolledTimestamp = now - 110L * 86_400_000,
                    complianceNotes = "LUKS + TPM2 Clevis Auto-unlock, SELinux Enforcing (Targeted)",
                    agentVersion = "OmniAgent v4.2.0-linux-systemd",
                    platformSpecificDetail = "Docker container runtime audited, Root SSH login disabled"
                ),
                ManagedDevice(
                    id = "lnx-003",
                    name = "Debian Edge IoT Gateway",
                    platform = PlatformType.LINUX,
                    model = "Advantech UNO-2271G (Industrial)",
                    osVersion = "Debian 12 (Bookworm)",
                    serialNumber = "ADV-IOT-091238",
                    assignedUser = "Gateway Fleet Daemon",
                    department = "IoT Infrastructure",
                    ipAddress = "10.240.99.12",
                    macAddress = "00:0A:F7:11:80:CC",
                    storageTotalGb = 64,
                    storageUsedGb = 48,
                    batteryLevel = 100,
                    isCharging = true,
                    isEncrypted = false,
                    isPasscodeSet = true,
                    isJailbrokenOrRooted = false,
                    status = DeviceStatus.ONLINE,
                    compliance = ComplianceStatus.WARNING,
                    lastSeenTimestamp = now - 120_000,
                    enrolledTimestamp = now - 240L * 86_400_000,
                    complianceNotes = "Partisi root belum terenkripsi LUKS (Edge Industrial constraint)",
                    agentVersion = "OmniAgent v4.0.2-linux-arm",
                    platformSpecificDetail = "UFW Firewall aktif, Port terbuka hanya 22 (SSH cert-only) & 8883 (MQTT TLS)"
                )
            )
        }

        fun initialPolicies(): List<SecurityPolicy> {
            return listOf(
                SecurityPolicy(
                    id = "pol-all-01",
                    title = "Wajib Enkripsi Disk Penuh (FDE)",
                    description = "Mewajibkan enkripsi hardware (BitLocker di Windows, FileVault di macOS/iOS, LUKS di Linux, FBE di Android).",
                    targetPlatform = null,
                    requireEncryption = true,
                    requirePasscode = true,
                    blockRootJailbreak = true,
                    minOsVersion = "Standar Enterprise",
                    autoLockMinutes = 5,
                    allowCamera = true,
                    allowUsbStorage = false,
                    isEnabled = true
                ),
                SecurityPolicy(
                    id = "pol-mobile-02",
                    title = "Anti Jailbreak & Rooting Guard",
                    description = "Mendeteksi modifikasi kernel, Magisk SU, Cydia/Sileo, dan bootloader unlocked.",
                    targetPlatform = PlatformType.ANDROID,
                    requireEncryption = true,
                    requirePasscode = true,
                    blockRootJailbreak = true,
                    minOsVersion = "Android 13+",
                    autoLockMinutes = 3,
                    allowCamera = true,
                    allowUsbStorage = false,
                    isEnabled = true
                ),
                SecurityPolicy(
                    id = "pol-win-03",
                    title = "Windows 11 Baseline & TPM 2.0",
                    description = "Standar keamanan Windows dengan Secure Boot, Virtualization-Based Security (VBS), dan BitLocker PIN.",
                    targetPlatform = PlatformType.WINDOWS,
                    requireEncryption = true,
                    requirePasscode = true,
                    blockRootJailbreak = false,
                    minOsVersion = "Windows 11 22H2+",
                    autoLockMinutes = 10,
                    allowCamera = true,
                    allowUsbStorage = false,
                    isEnabled = true
                ),
                SecurityPolicy(
                    id = "pol-lnx-04",
                    title = "Linux Kernel & Hardening (AppArmor/SELinux)",
                    description = "Memastikan Linux Workstation mengaktifkan LSM (SELinux / AppArmor) dan membatasi akses root SSH.",
                    targetPlatform = PlatformType.LINUX,
                    requireEncryption = true,
                    requirePasscode = true,
                    blockRootJailbreak = false,
                    minOsVersion = "Kernel 6.1+",
                    autoLockMinutes = 15,
                    allowCamera = true,
                    allowUsbStorage = true,
                    isEnabled = true
                ),
                SecurityPolicy(
                    id = "pol-ios-05",
                    title = "Apple Supervised & Passcode Complexity",
                    description = "Wajib sandi minimal 6 karakter alfanumerik dan perlindungan aktivasi MDM Supervised.",
                    targetPlatform = PlatformType.IOS,
                    requireEncryption = true,
                    requirePasscode = true,
                    blockRootJailbreak = true,
                    minOsVersion = "iOS 17.0+ / macOS 14.0+",
                    autoLockMinutes = 5,
                    allowCamera = true,
                    allowUsbStorage = false,
                    isEnabled = true
                )
            )
        }

        fun initialAuditLogs(): List<AuditLogEntry> {
            val now = System.currentTimeMillis()
            return listOf(
                AuditLogEntry(
                    id = "log-001",
                    deviceId = "lnx-001",
                    deviceName = "Ubuntu DevStation - Backend Core",
                    platform = PlatformType.LINUX,
                    actionType = "POLICY_SYNC",
                    performedBy = "Auto-Schedule (Hourly)",
                    timestamp = now - 12_000,
                    isSuccess = true,
                    details = "Sinkronisasi profil SELinux/AppArmor dan audit rule auditd sukses."
                ),
                AuditLogEntry(
                    id = "log-002",
                    deviceId = "and-003",
                    deviceName = "Xiaomi Pad 6 - QA Staging",
                    platform = PlatformType.ANDROID,
                    actionType = "COMPLIANCE_FLAG",
                    performedBy = "Compliance Daemon",
                    timestamp = now - 3_600_000,
                    isSuccess = false,
                    details = "Perangkat ditandai NON-COMPLIANT: Terdeteksi Magisk SU binary di /system/xbin/su."
                ),
                AuditLogEntry(
                    id = "log-003",
                    deviceId = "win-001",
                    deviceName = "Lenovo ThinkPad X1 - DevOps Lead",
                    platform = PlatformType.WINDOWS,
                    actionType = "SECURITY_SCAN",
                    performedBy = "Admin (IT SecOps)",
                    timestamp = now - 7_200_000,
                    isSuccess = true,
                    details = "Pemindaian Defender ATP terjadwal selesai. 0 ancaman terdeteksi."
                ),
                AuditLogEntry(
                    id = "log-004",
                    deviceId = "ios-001",
                    deviceName = "MacBook Pro M3 Max - CEO",
                    platform = PlatformType.IOS,
                    actionType = "CERT_RENEW",
                    performedBy = "Apple MDM APNs Gateway",
                    timestamp = now - 86_400_000,
                    isSuccess = true,
                    details = "Sertifikat profil MDM diperpanjang hingga Oktober 2027."
                )
            )
        }
    }
}
