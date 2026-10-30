package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AuditLogEntry
import com.example.data.model.ComplianceStatus
import com.example.data.model.DeviceStatus
import com.example.data.model.ManagedDevice
import com.example.data.model.PlatformType
import com.example.data.model.SecurityPolicy
import com.example.data.repository.DeviceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class NavigationTab(val label: String) {
    DASHBOARD("Dasbor"),
    DEVICES("Perangkat"),
    POLICIES("Kebijakan"),
    AUDIT("Log Audit")
}

data class FleetMetrics(
    val totalDevices: Int = 0,
    val onlineDevices: Int = 0,
    val compliantDevices: Int = 0,
    val warningDevices: Int = 0,
    val nonCompliantDevices: Int = 0,
    val iosCount: Int = 0,
    val androidCount: Int = 0,
    val windowsCount: Int = 0,
    val linuxCount: Int = 0
)

class FleetViewModel(
    private val repository: DeviceRepository = DeviceRepository()
) : ViewModel() {

    val allDevices: StateFlow<List<ManagedDevice>> = repository.devices
    val policies: StateFlow<List<SecurityPolicy>> = repository.policies
    val auditLogs: StateFlow<List<AuditLogEntry>> = repository.auditLogs

    private val _currentTab = MutableStateFlow(NavigationTab.DASHBOARD)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    private val _selectedPlatformFilter = MutableStateFlow<PlatformType?>(null)
    val selectedPlatformFilter: StateFlow<PlatformType?> = _selectedPlatformFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedDevice = MutableStateFlow<ManagedDevice?>(null)
    val selectedDevice: StateFlow<ManagedDevice?> = _selectedDevice.asStateFlow()

    private val _isEnrollDialogOpen = MutableStateFlow(false)
    val isEnrollDialogOpen: StateFlow<Boolean> = _isEnrollDialogOpen.asStateFlow()

    private val _notificationMessage = MutableStateFlow<String?>(null)
    val notificationMessage: StateFlow<String?> = _notificationMessage.asStateFlow()

    val filteredDevices: StateFlow<List<ManagedDevice>> = combine(
        allDevices,
        _selectedPlatformFilter,
        _searchQuery
    ) { devices, platformFilter, query ->
        devices.filter { dev ->
            val matchPlatform = platformFilter == null || dev.platform == platformFilter
            val matchQuery = query.isBlank() ||
                dev.name.contains(query, ignoreCase = true) ||
                dev.assignedUser.contains(query, ignoreCase = true) ||
                dev.serialNumber.contains(query, ignoreCase = true) ||
                dev.model.contains(query, ignoreCase = true) ||
                dev.department.contains(query, ignoreCase = true) ||
                dev.ipAddress.contains(query, ignoreCase = true)
            matchPlatform && matchQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val fleetMetrics: StateFlow<FleetMetrics> = allDevices.combine(_searchQuery) { devices, _ ->
        FleetMetrics(
            totalDevices = devices.size,
            onlineDevices = devices.count { it.status == DeviceStatus.ONLINE },
            compliantDevices = devices.count { it.compliance == ComplianceStatus.COMPLIANT },
            warningDevices = devices.count { it.compliance == ComplianceStatus.WARNING },
            nonCompliantDevices = devices.count { it.compliance == ComplianceStatus.NON_COMPLIANT },
            iosCount = devices.count { it.platform == PlatformType.IOS },
            androidCount = devices.count { it.platform == PlatformType.ANDROID },
            windowsCount = devices.count { it.platform == PlatformType.WINDOWS },
            linuxCount = devices.count { it.platform == PlatformType.LINUX }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FleetMetrics())

    fun selectTab(tab: NavigationTab) {
        _currentTab.value = tab
    }

    fun setPlatformFilter(platform: PlatformType?) {
        _selectedPlatformFilter.value = platform
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectDevice(device: ManagedDevice?) {
        _selectedDevice.value = device
    }

    fun setEnrollDialogOpen(isOpen: Boolean) {
        _isEnrollDialogOpen.value = isOpen
    }

    fun clearNotification() {
        _notificationMessage.value = null
    }

    fun lockDevice(deviceId: String) {
        viewModelScope.launch {
            repository.lockDevice(deviceId)
            refreshSelectedDevice(deviceId)
            _notificationMessage.value = "Perintah Lock terkirim ke perangkat."
        }
    }

    fun unlockDevice(deviceId: String) {
        viewModelScope.launch {
            repository.unlockDevice(deviceId)
            refreshSelectedDevice(deviceId)
            _notificationMessage.value = "Kunci perangkat berhasil dibuka."
        }
    }

    fun rebootDevice(deviceId: String) {
        viewModelScope.launch {
            repository.rebootDevice(deviceId)
            refreshSelectedDevice(deviceId)
            _notificationMessage.value = "Perintah Reboot terkirim ke perangkat."
        }
    }

    fun syncDevice(deviceId: String) {
        viewModelScope.launch {
            repository.syncDevice(deviceId)
            refreshSelectedDevice(deviceId)
            _notificationMessage.value = "Sinkronisasi kebijakan perangkat berhasil."
        }
    }

    fun wipeDevice(deviceId: String) {
        viewModelScope.launch {
            repository.wipeDevice(deviceId)
            refreshSelectedDevice(deviceId)
            _notificationMessage.value = "Perintah Remote Wipe telah dieksekusi!"
        }
    }

    fun scanDevice(deviceId: String) {
        viewModelScope.launch {
            repository.scanDevice(deviceId)
            refreshSelectedDevice(deviceId)
            _notificationMessage.value = "Pemeriksaan keamanan & integritas kernel selesai."
        }
    }

    fun togglePolicy(policyId: String) {
        viewModelScope.launch {
            repository.togglePolicy(policyId)
            // also update currently selected device if any
            _selectedDevice.value?.let { current ->
                refreshSelectedDevice(current.id)
            }
        }
    }

    fun deleteDevice(deviceId: String) {
        viewModelScope.launch {
            repository.deleteDevice(deviceId)
            if (_selectedDevice.value?.id == deviceId) {
                _selectedDevice.value = null
            }
            _notificationMessage.value = "Perangkat telah dihapus dari armada."
        }
    }

    fun enrollDevice(
        name: String,
        platform: PlatformType,
        model: String,
        osVersion: String,
        serialNumber: String,
        assignedUser: String,
        department: String,
        storageGb: Int,
        isEncrypted: Boolean
    ) {
        val now = System.currentTimeMillis()
        val randomIpEnd = (10..240).random()
        val generatedIp = when (platform) {
            PlatformType.IOS -> "10.240.12.$randomIpEnd"
            PlatformType.ANDROID -> "10.240.10.$randomIpEnd"
            PlatformType.WINDOWS -> "10.240.16.$randomIpEnd"
            PlatformType.LINUX -> "10.240.20.$randomIpEnd"
        }
        val mac = String.format("%02X:%02X:%02X:%02X:%02X:%02X",
            (0..255).random(), (0..255).random(), (0..255).random(),
            (0..255).random(), (0..255).random(), (0..255).random())

        val platformDetail = when (platform) {
            PlatformType.IOS -> "Apple MDM Enrolled via ADE (Supervised: Ya)"
            PlatformType.ANDROID -> "Android Enterprise Work Profile / COBO"
            PlatformType.WINDOWS -> "Azure AD Joined + Intune/MDM Provisioning"
            PlatformType.LINUX -> "Linux Systemd OmniAgent Daemon + SSH Certs"
        }

        val newDev = ManagedDevice(
            id = "${platform.name.lowercase().take(3)}-${UUID.randomUUID().toString().take(6)}",
            name = name.ifBlank { "$model ($assignedUser)" },
            platform = platform,
            model = model.ifBlank { "Enterprise Standard" },
            osVersion = osVersion.ifBlank { "Latest Release" },
            serialNumber = serialNumber.ifBlank { "SN-${(100000..999999).random()}" },
            assignedUser = assignedUser.ifBlank { "Karyawan Baru" },
            department = department.ifBlank { "General" },
            ipAddress = generatedIp,
            macAddress = mac,
            storageTotalGb = storageGb,
            storageUsedGb = (storageGb * 0.35).toInt(),
            batteryLevel = (60..98).random(),
            isCharging = false,
            isEncrypted = isEncrypted,
            isPasscodeSet = true,
            isJailbrokenOrRooted = false,
            status = DeviceStatus.ONLINE,
            compliance = if (isEncrypted) ComplianceStatus.COMPLIANT else ComplianceStatus.WARNING,
            lastSeenTimestamp = now,
            enrolledTimestamp = now,
            complianceNotes = if (isEncrypted) "Perangkat baru terdaftar dan telah memenuhi standar kepatuhan"
                              else "Perangkat memerlukan enkripsi disk penuh",
            agentVersion = "OmniAgent v4.2.0-${platform.name.lowercase()}",
            platformSpecificDetail = platformDetail
        )

        repository.addDevice(newDev)
        _isEnrollDialogOpen.value = false
        _notificationMessage.value = "Perangkat '${newDev.name}' berhasil didaftarkan!"
    }

    private fun refreshSelectedDevice(deviceId: String) {
        val updated = repository.devices.value.find { it.id == deviceId }
        _selectedDevice.value = updated
    }
}
