package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ComplianceStatus
import com.example.data.model.DeviceStatus
import com.example.data.model.PlatformType
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusInfo
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning

@Composable
fun PlatformIcon(
    platform: PlatformType,
    modifier: Modifier = Modifier,
    tint: Color = platform.brandColor
) {
    val icon: ImageVector = when (platform) {
        PlatformType.IOS -> Icons.Default.PhoneIphone
        PlatformType.ANDROID -> Icons.Default.Android
        PlatformType.WINDOWS -> Icons.Default.Computer
        PlatformType.LINUX -> Icons.Default.Terminal
    }
    Icon(
        imageVector = icon,
        contentDescription = platform.displayName,
        modifier = modifier,
        tint = tint
    )
}

@Composable
fun PlatformBadge(platform: PlatformType, modifier: Modifier = Modifier) {
    Surface(
        color = platform.brandColor.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, platform.brandColor.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            PlatformIcon(platform = platform, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = when (platform) {
                    PlatformType.IOS -> "iOS"
                    PlatformType.ANDROID -> "Android"
                    PlatformType.WINDOWS -> "Windows"
                    PlatformType.LINUX -> "Linux"
                },
                color = platform.brandColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StatusBadge(status: DeviceStatus, modifier: Modifier = Modifier) {
    val (bg, textColor) = when (status) {
        DeviceStatus.ONLINE -> StatusSuccess.copy(alpha = 0.15f) to StatusSuccess
        DeviceStatus.OFFLINE -> Color(0xFF64748B).copy(alpha = 0.15f) to Color(0xFF94A3B8)
        DeviceStatus.SYNCING -> StatusInfo.copy(alpha = 0.15f) to StatusInfo
        DeviceStatus.LOCKED -> StatusWarning.copy(alpha = 0.15f) to StatusWarning
        DeviceStatus.WIPED -> StatusDanger.copy(alpha = 0.15f) to StatusDanger
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = status.displayName,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ComplianceBadge(compliance: ComplianceStatus, modifier: Modifier = Modifier) {
    val (icon, bg, color, text) = when (compliance) {
        ComplianceStatus.COMPLIANT -> Tuple4(
            Icons.Default.CheckCircle,
            StatusSuccess.copy(alpha = 0.15f),
            StatusSuccess,
            "Compliant"
        )
        ComplianceStatus.WARNING -> Tuple4(
            Icons.Default.Warning,
            StatusWarning.copy(alpha = 0.15f),
            StatusWarning,
            "Warning"
        )
        ComplianceStatus.NON_COMPLIANT -> Tuple4(
            Icons.Default.ErrorOutline,
            StatusDanger.copy(alpha = 0.15f),
            StatusDanger,
            "Non-Compliant"
        )
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun BatteryIndicator(
    batteryLevel: Int,
    isCharging: Boolean,
    modifier: Modifier = Modifier
) {
    val color = when {
        batteryLevel > 50 -> StatusSuccess
        batteryLevel > 20 -> StatusWarning
        else -> StatusDanger
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        if (isCharging) {
            Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = "Sedang Mengisi Daya",
                tint = StatusWarning,
                modifier = Modifier.size(14.dp)
            )
        } else {
            val icon = if (batteryLevel > 20) Icons.Default.BatteryFull else Icons.Default.BatteryAlert
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = "$batteryLevel%",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun StorageProgressBar(
    usedGb: Int,
    totalGb: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (totalGb > 0) usedGb.toFloat() / totalGb.toFloat() else 0f
    val progressColor = when {
        progress > 0.9f -> StatusDanger
        progress > 0.75f -> StatusWarning
        else -> MaterialTheme.colorScheme.primary
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Penyimpanan",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$usedGb GB / $totalGb GB (${(progress * 100).toInt()}%)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = progressColor,
            trackColor = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subLabel: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = modifier.testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Surface(
                    color = accentColor.copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subLabel,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
