package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ComplianceStatus
import com.example.data.model.ManagedDevice
import com.example.data.model.PlatformType
import com.example.ui.FleetViewModel
import com.example.ui.NavigationTab
import com.example.ui.components.BatteryIndicator
import com.example.ui.components.ComplianceBadge
import com.example.ui.components.MetricCard
import com.example.ui.components.PlatformBadge
import com.example.ui.components.PlatformIcon
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AndroidGreen
import com.example.ui.theme.IosBlue
import com.example.ui.theme.LinuxOrange
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.WindowsBlue

@Composable
fun DashboardScreen(
    viewModel: FleetViewModel,
    modifier: Modifier = Modifier
) {
    val metrics by viewModel.fleetMetrics.collectAsState()
    val allDevices by viewModel.allDevices.collectAsState()

    val complianceRate = if (metrics.totalDevices > 0) {
        ((metrics.compliantDevices.toFloat() / metrics.totalDevices) * 100).toInt()
    } else 100

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header & Quick Action
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth().testTag("dashboard_header_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Konsol Armada OmniFleet",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Manajemen & Keamanan Multi-Platform Terpadu",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.setEnrollDialogOpen(true) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("dashboard_enroll_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Daftar Perangkat", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.selectTab(NavigationTab.DEVICES) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("dashboard_view_all_button")
                        ) {
                            Text("Lihat Semua", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }

        // Fleet Metrics Grid (2x2)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Ringkasan Armada",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Total Armada",
                        value = "${metrics.totalDevices}",
                        subLabel = "${metrics.onlineDevices} aktif online",
                        icon = Icons.Default.Devices,
                        accentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        testTag = "metric_total_devices"
                    )
                    MetricCard(
                        title = "Kepatuhan",
                        value = "$complianceRate%",
                        subLabel = "${metrics.compliantDevices} patuh kebijakan",
                        icon = Icons.Default.CheckCircle,
                        accentColor = StatusSuccess,
                        modifier = Modifier.weight(1f),
                        testTag = "metric_compliance_rate"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Online",
                        value = "${metrics.onlineDevices}",
                        subLabel = "Sinkronisasi real-time",
                        icon = Icons.Default.Wifi,
                        accentColor = StatusSuccess,
                        modifier = Modifier.weight(1f),
                        testTag = "metric_online"
                    )
                    MetricCard(
                        title = "Peringatan",
                        value = "${metrics.warningDevices + metrics.nonCompliantDevices}",
                        subLabel = "${metrics.nonCompliantDevices} butuh tindakan",
                        icon = Icons.Default.Warning,
                        accentColor = if (metrics.nonCompliantDevices > 0) StatusDanger else StatusWarning,
                        modifier = Modifier.weight(1f),
                        testTag = "metric_alerts"
                    )
                }
            }
        }

        // Cross-Platform Fleet Distribution Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Distribusi Platform Sistem Operasi",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PlatformSummaryItem(
                        platform = PlatformType.IOS,
                        count = metrics.iosCount,
                        color = IosBlue,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.setPlatformFilter(PlatformType.IOS)
                            viewModel.selectTab(NavigationTab.DEVICES)
                        }
                    )
                    PlatformSummaryItem(
                        platform = PlatformType.ANDROID,
                        count = metrics.androidCount,
                        color = AndroidGreen,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.setPlatformFilter(PlatformType.ANDROID)
                            viewModel.selectTab(NavigationTab.DEVICES)
                        }
                    )
                    PlatformSummaryItem(
                        platform = PlatformType.WINDOWS,
                        count = metrics.windowsCount,
                        color = WindowsBlue,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.setPlatformFilter(PlatformType.WINDOWS)
                            viewModel.selectTab(NavigationTab.DEVICES)
                        }
                    )
                    PlatformSummaryItem(
                        platform = PlatformType.LINUX,
                        count = metrics.linuxCount,
                        color = LinuxOrange,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.setPlatformFilter(PlatformType.LINUX)
                            viewModel.selectTab(NavigationTab.DEVICES)
                        }
                    )
                }
            }
        }

        // Fleet Security Posture Checklist
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth().testTag("fleet_security_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Postur Keamanan Seluruh Platform",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Default.Policy,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val encryptedCount = allDevices.count { it.isEncrypted }
                    val passcodeCount = allDevices.count { it.isPasscodeSet }
                    val cleanCount = allDevices.count { !it.isJailbrokenOrRooted }

                    SecurityItemRow(
                        title = "Enkripsi Hardware (BitLocker/FileVault/LUKS/FBE)",
                        count = "$encryptedCount/${allDevices.size}",
                        isOk = encryptedCount == allDevices.size
                    )
                    SecurityItemRow(
                        title = "Kunci Sandi & Biometrik Aktif",
                        count = "$passcodeCount/${allDevices.size}",
                        isOk = passcodeCount == allDevices.size
                    )
                    SecurityItemRow(
                        title = "Integritas OS (Bebas Root/Jailbreak)",
                        count = "$cleanCount/${allDevices.size}",
                        isOk = cleanCount == allDevices.size
                    )
                }
            }
        }

        // Recent Managed Devices Preview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Perangkat Terbaru",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Kelola Semua",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { viewModel.selectTab(NavigationTab.DEVICES) }
                )
            }
        }

        items(allDevices.take(4)) { device ->
            DeviceCompactRow(
                device = device,
                onClick = { viewModel.selectDevice(device) }
            )
        }
    }
}

@Composable
private fun PlatformSummaryItem(
    platform: PlatformType,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("platform_chip_${platform.name.lowercase()}")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)
        ) {
            PlatformIcon(platform = platform, modifier = Modifier.size(24.dp), tint = color)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$count",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = when (platform) {
                    PlatformType.IOS -> "iOS"
                    PlatformType.ANDROID -> "Android"
                    PlatformType.WINDOWS -> "Windows"
                    PlatformType.LINUX -> "Linux"
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SecurityItemRow(title: String, count: String, isOk: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = if (isOk) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (isOk) StatusSuccess else StatusWarning,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = count,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isOk) StatusSuccess else StatusWarning
        )
    }
}

@Composable
fun DeviceCompactRow(
    device: ManagedDevice,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("device_row_${device.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    color = device.platform.brandColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        PlatformIcon(platform = device.platform, modifier = Modifier.size(22.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = device.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${device.assignedUser} • ${device.department}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                StatusBadge(status = device.status)
                Spacer(modifier = Modifier.height(4.dp))
                ComplianceBadge(compliance = device.compliance)
            }
        }
    }
}
