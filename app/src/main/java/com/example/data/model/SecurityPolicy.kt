package com.example.data.model

data class SecurityPolicy(
    val id: String,
    val title: String,
    val description: String,
    val targetPlatform: PlatformType?, // null means All Platforms
    val requireEncryption: Boolean,
    val requirePasscode: Boolean,
    val blockRootJailbreak: Boolean,
    val minOsVersion: String,
    val autoLockMinutes: Int,
    val allowCamera: Boolean,
    val allowUsbStorage: Boolean,
    val isEnabled: Boolean
)

data class AuditLogEntry(
    val id: String,
    val deviceId: String,
    val deviceName: String,
    val platform: PlatformType,
    val actionType: String,
    val performedBy: String,
    val timestamp: Long,
    val isSuccess: Boolean,
    val details: String
)
