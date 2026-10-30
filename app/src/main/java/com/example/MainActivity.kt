package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FleetViewModel
import com.example.ui.NavigationTab
import com.example.ui.screens.AuditLogsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DeviceDetailScreen
import com.example.ui.screens.DeviceListScreen
import com.example.ui.screens.EnrollDeviceDialog
import com.example.ui.screens.PoliciesScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: FleetViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val currentTab by viewModel.currentTab.collectAsState()
                val selectedDevice by viewModel.selectedDevice.collectAsState()
                val isEnrollOpen by viewModel.isEnrollDialogOpen.collectAsState()
                val notificationMessage by viewModel.notificationMessage.collectAsState()

                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(notificationMessage) {
                    notificationMessage?.let { msg ->
                        snackbarHostState.showSnackbar(msg)
                        viewModel.clearNotification()
                    }
                }

                if (selectedDevice != null) {
                    DeviceDetailScreen(
                        device = selectedDevice!!,
                        viewModel = viewModel,
                        onBack = { viewModel.selectDevice(null) }
                    )
                } else {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    Column {
                                        Text(
                                            text = "OmniFleet MDM",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "iOS • Android • Windows • Linux",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                },
                                actions = {
                                    IconButton(
                                        onClick = { viewModel.setEnrollDialogOpen(true) },
                                        modifier = Modifier.testTag("topbar_enroll_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Daftarkan Perangkat",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        },
                        bottomBar = {
                            NavigationBar(
                                modifier = Modifier
                                    .navigationBarsPadding()
                                    .testTag("bottom_nav_bar"),
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 8.dp
                            ) {
                                NavigationBarItem(
                                    selected = currentTab == NavigationTab.DASHBOARD,
                                    onClick = { viewModel.selectTab(NavigationTab.DASHBOARD) },
                                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dasbor") },
                                    label = { Text("Dasbor", fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier.testTag("nav_tab_dashboard")
                                )
                                NavigationBarItem(
                                    selected = currentTab == NavigationTab.DEVICES,
                                    onClick = { viewModel.selectTab(NavigationTab.DEVICES) },
                                    icon = { Icon(Icons.Default.Devices, contentDescription = "Perangkat") },
                                    label = { Text("Perangkat", fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier.testTag("nav_tab_devices")
                                )
                                NavigationBarItem(
                                    selected = currentTab == NavigationTab.POLICIES,
                                    onClick = { viewModel.selectTab(NavigationTab.POLICIES) },
                                    icon = { Icon(Icons.Default.Policy, contentDescription = "Kebijakan") },
                                    label = { Text("Kebijakan", fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier.testTag("nav_tab_policies")
                                )
                                NavigationBarItem(
                                    selected = currentTab == NavigationTab.AUDIT,
                                    onClick = { viewModel.selectTab(NavigationTab.AUDIT) },
                                    icon = { Icon(Icons.Default.History, contentDescription = "Log Audit") },
                                    label = { Text("Audit", fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier.testTag("nav_tab_audit")
                                )
                            }
                        },
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (currentTab) {
                                NavigationTab.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                                NavigationTab.DEVICES -> DeviceListScreen(viewModel = viewModel)
                                NavigationTab.POLICIES -> PoliciesScreen(viewModel = viewModel)
                                NavigationTab.AUDIT -> AuditLogsScreen(viewModel = viewModel)
                            }
                        }
                    }
                }

                if (isEnrollOpen) {
                    EnrollDeviceDialog(
                        viewModel = viewModel,
                        onDismiss = { viewModel.setEnrollDialogOpen(false) }
                    )
                }
            }
        }
    }
}
