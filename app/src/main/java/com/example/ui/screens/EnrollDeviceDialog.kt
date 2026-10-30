package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.PlatformType
import com.example.ui.FleetViewModel
import com.example.ui.components.PlatformIcon

@Composable
fun EnrollDeviceDialog(
    viewModel: FleetViewModel,
    onDismiss: () -> Unit
) {
    var selectedPlatform by remember { mutableStateOf(PlatformType.ANDROID) }
    var deviceName by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var osVersion by remember { mutableStateOf("") }
    var serialNumber by remember { mutableStateOf("") }
    var assignedUser by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("Engineering") }
    var storageGb by remember { mutableStateOf("256") }
    var isEncrypted by remember { mutableStateOf(true) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp)
                .testTag("enroll_device_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Pendaftaran Armada Baru",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Pilih platform OS & lengkapi data profil MDM",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_enroll_dialog")) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Platform Selection
                Text(
                    text = "Pilih Platform Sistem Operasi:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PlatformType.values().forEach { platform ->
                        val isSelected = selectedPlatform == platform
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedPlatform = platform
                                if (model.isBlank()) {
                                    model = when (platform) {
                                        PlatformType.IOS -> "iPhone 15 / MacBook M3"
                                        PlatformType.ANDROID -> "Pixel 8 / Galaxy S24"
                                        PlatformType.WINDOWS -> "Dell Latitude / ThinkPad"
                                        PlatformType.LINUX -> "Ubuntu Workstation / Server"
                                    }
                                }
                                if (osVersion.isBlank()) {
                                    osVersion = when (platform) {
                                        PlatformType.IOS -> "iOS 18.0 / macOS 15.0"
                                        PlatformType.ANDROID -> "Android 15"
                                        PlatformType.WINDOWS -> "Windows 11 23H2"
                                        PlatformType.LINUX -> "Ubuntu 24.04 LTS"
                                    }
                                }
                            },
                            leadingIcon = {
                                PlatformIcon(platform = platform, modifier = Modifier.size(16.dp))
                            },
                            label = {
                                Text(
                                    text = when (platform) {
                                        PlatformType.IOS -> "iOS"
                                        PlatformType.ANDROID -> "Android"
                                        PlatformType.WINDOWS -> "Win"
                                        PlatformType.LINUX -> "Linux"
                                    },
                                    fontSize = 11.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = platform.brandColor.copy(alpha = 0.2f),
                                selectedLabelColor = platform.brandColor
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("select_platform_${platform.name.lowercase()}")
                        )
                    }
                }

                // Platform Enrollment Snippet / Token
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Instruksi Enrollment Agent:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        val snippet = when (selectedPlatform) {
                            PlatformType.IOS -> "https://enroll.omnifleet.io/apple/profile.mobileconfig"
                            PlatformType.ANDROID -> "DPC Token: afw#omnifleet-enterprise-enroll"
                            PlatformType.WINDOWS -> "ms-device-enrollment:?mode=MDM&server=omnifleet.io"
                            PlatformType.LINUX -> "curl -sSL https://get.omnifleet.io | sudo bash"
                        }
                        Text(
                            text = snippet,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Form Fields
                OutlinedTextField(
                    value = deviceName,
                    onValueChange = { deviceName = it },
                    label = { Text("Nama Perangkat (contoh: Laptop Sales - Sari)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_device_name")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it },
                        label = { Text("Model Hardware") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_model")
                    )
                    OutlinedTextField(
                        value = osVersion,
                        onValueChange = { osVersion = it },
                        label = { Text("Versi OS") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_os_version")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = assignedUser,
                        onValueChange = { assignedUser = it },
                        label = { Text("Nama Pengguna") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_user")
                    )
                    OutlinedTextField(
                        value = department,
                        onValueChange = { department = it },
                        label = { Text("Departemen") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_department")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = serialNumber,
                        onValueChange = { serialNumber = it },
                        label = { Text("Nomor Seri / UUID") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f).testTag("input_serial")
                    )
                    OutlinedTextField(
                        value = storageGb,
                        onValueChange = { storageGb = it },
                        label = { Text("Storage (GB)") },
                        singleLine = true,
                        modifier = Modifier.weight(0.8f).testTag("input_storage")
                    )
                }

                // Encryption Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enkripsi Hardware Aktif",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "BitLocker / FileVault / LUKS / FBE aktif di perangkat",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isEncrypted,
                        onCheckedChange = { isEncrypted = it },
                        modifier = Modifier.testTag("switch_encryption")
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.enrollDevice(
                                name = deviceName,
                                platform = selectedPlatform,
                                model = model,
                                osVersion = osVersion,
                                serialNumber = serialNumber,
                                assignedUser = assignedUser,
                                department = department,
                                storageGb = storageGb.toIntOrNull() ?: 256,
                                isEncrypted = isEncrypted
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_submit_enroll")
                    ) {
                        Text("Daftarkan ke Armada")
                    }
                }
            }
        }
    }
}
