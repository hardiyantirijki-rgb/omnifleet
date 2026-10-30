package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class PlatformType(
    val displayName: String,
    val iconLabel: String,
    val brandColor: Color,
    val defaultArchitecture: String
) {
    IOS(
        displayName = "iOS / iPadOS",
        iconLabel = "Apple iOS",
        brandColor = Color(0xFF007AFF),
        defaultArchitecture = "ARM64 (Apple Silicon)"
    ),
    ANDROID(
        displayName = "Android",
        iconLabel = "Android Enterprise",
        brandColor = Color(0xFF10B981),
        defaultArchitecture = "AArch64 (ARMv8/v9)"
    ),
    WINDOWS(
        displayName = "Windows",
        iconLabel = "Windows 11",
        brandColor = Color(0xFF0284C7),
        defaultArchitecture = "x86_64 / ARM64"
    ),
    LINUX(
        displayName = "Linux",
        iconLabel = "Linux Fleet",
        brandColor = Color(0xFFF97316),
        defaultArchitecture = "x86_64 / AArch64"
    )
}

enum class DeviceStatus(val displayName: String, val color: Color) {
    ONLINE("Online", Color(0xFF10B981)),
    OFFLINE("Offline", Color(0xFF64748B)),
    SYNCING("Sinkronisasi...", Color(0xFF3B82F6)),
    LOCKED("Terkunci (Lock)", Color(0xFFF59E0B)),
    WIPED("Dihapus (Wiped)", Color(0xFFEF4444))
}

enum class ComplianceStatus(val displayName: String, val color: Color) {
    COMPLIANT("Memenuhi Syarat", Color(0xFF10B981)),
    WARNING("Perhatian", Color(0xFFF59E0B)),
    NON_COMPLIANT("Tidak Patuh", Color(0xFFEF4444))
}

data class ManagedDevice(
    val id: String,
    val name: String,
    val platform: PlatformType,
    val model: String,
    val osVersion: String,
    val serialNumber: String,
    val assignedUser: String,
    val department: String,
    val ipAddress: String,
    val macAddress: String,
    val storageTotalGb: Int,
    val storageUsedGb: Int,
    val batteryLevel: Int,
    val isCharging: Boolean,
    val isEncrypted: Boolean,
    val isPasscodeSet: Boolean,
    val isJailbrokenOrRooted: Boolean,
    val status: DeviceStatus,
    val compliance: ComplianceStatus,
    val lastSeenTimestamp: Long,
    val enrolledTimestamp: Long,
    val complianceNotes: String,
    val agentVersion: String,
    val platformSpecificDetail: String
)
