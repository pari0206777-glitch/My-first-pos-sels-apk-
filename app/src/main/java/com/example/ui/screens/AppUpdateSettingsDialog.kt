package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.update.AppUpdateCheckResult
import com.example.data.update.AppUpdateInfo
import com.example.data.update.AppUpdateManager
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.TealAccent
import kotlinx.coroutines.launch

@Composable
fun AppUpdateSettingsDialog(
    updateManager: AppUpdateManager,
    onDismiss: () -> Unit,
    onUpdateFound: (AppUpdateInfo, Boolean) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isChecking by remember { mutableStateOf(false) }
    var checkStatusMessage by remember { mutableStateOf<String?>(null) }
    var isStatusError by remember { mutableStateOf(false) }

    var showEditUrlDialog by remember { mutableStateOf(false) }
    var updateUrlInput by remember { mutableStateOf(updateManager.preferences.updateSourceUrl) }

    var simulationMode by remember { mutableStateOf(updateManager.preferences.simulationMode) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = null,
                        tint = NavyPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("App Updates", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Version Info Card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Current Installed Version", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "v${updateManager.currentVersionName} (Build ${updateManager.currentVersionCode})",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFE8F5E9)
                        ) {
                            Text(
                                text = "OFFLINE-FIRST",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                // Check for Updates Button
                Button(
                    onClick = {
                        isChecking = true
                        checkStatusMessage = null
                        isStatusError = false

                        coroutineScope.launch {
                            val result = updateManager.checkForUpdate(isManualCheck = true)
                            isChecking = false

                            when (result) {
                                is AppUpdateCheckResult.UpdateAvailable -> {
                                    onDismiss()
                                    onUpdateFound(result.updateInfo, result.isMandatory)
                                }
                                is AppUpdateCheckResult.UpToDate -> {
                                    checkStatusMessage = "You have the latest version (v${result.currentVersion}). No update needed."
                                    isStatusError = false
                                }
                                is AppUpdateCheckResult.Error -> {
                                    checkStatusMessage = result.message
                                    isStatusError = true
                                }
                            }
                        }
                    },
                    enabled = !isChecking,
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("button_check_for_updates")
                ) {
                    if (isChecking) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Checking...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Check for Updates Now", fontWeight = FontWeight.Bold)
                    }
                }

                // Status Message Box
                if (checkStatusMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isStatusError) MaterialTheme.colorScheme.errorContainer else Color(0xFFE8F5E9)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isStatusError) Icons.Default.WifiOff else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (isStatusError) MaterialTheme.colorScheme.error else Color(0xFF2E7D32),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = checkStatusMessage ?: "",
                                fontSize = 12.sp,
                                color = if (isStatusError) MaterialTheme.colorScheme.onErrorContainer else Color(0xFF2E7D32)
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Configured Update Source URL
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Remote Metadata URL", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        TextButton(
                            onClick = {
                                updateUrlInput = updateManager.preferences.updateSourceUrl
                                showEditUrlDialog = true
                            }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = NavyPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Change", fontSize = 12.sp, color = NavyPrimary)
                        }
                    }

                    Text(
                        text = updateManager.preferences.updateSourceUrl,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 14.sp
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Developer Testing Simulation Modes
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Testing & Verification Tools",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = NavyPrimary
                    )
                    Text(
                        text = "Simulate different update scenarios immediately without a web server:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    SimulationOptionItem(
                        title = "Live Remote Server (Default)",
                        subtitle = "Queries configured remote JSON URL over network",
                        selected = simulationMode == "NONE",
                        onClick = {
                            simulationMode = "NONE"
                            updateManager.preferences.simulationMode = "NONE"
                            checkStatusMessage = "Switched to Live Remote Server mode."
                            isStatusError = false
                        }
                    )

                    SimulationOptionItem(
                        title = "Simulate: Update Available (v1.0.1)",
                        subtitle = "Tests version comparison & optional update flow",
                        selected = simulationMode == "OPTIONAL",
                        onClick = {
                            simulationMode = "OPTIONAL"
                            updateManager.preferences.simulationMode = "OPTIONAL"
                            updateManager.preferences.resetDismissedVersion()
                            checkStatusMessage = "Active: Optional Update v1.0.1 simulation."
                            isStatusError = false
                        }
                    )

                    SimulationOptionItem(
                        title = "Simulate: Mandatory Update (v2.0.0)",
                        subtitle = "Tests critical security update with 'Later' disabled",
                        selected = simulationMode == "MANDATORY",
                        onClick = {
                            simulationMode = "MANDATORY"
                            updateManager.preferences.simulationMode = "MANDATORY"
                            checkStatusMessage = "Active: Mandatory Update v2.0.0 simulation."
                            isStatusError = false
                        }
                    )

                    SimulationOptionItem(
                        title = "Simulate: Up-To-Date (No Update)",
                        subtitle = "Tests when current version matches remote",
                        selected = simulationMode == "UP_TO_DATE",
                        onClick = {
                            simulationMode = "UP_TO_DATE"
                            updateManager.preferences.simulationMode = "UP_TO_DATE"
                            checkStatusMessage = "Active: Up-to-date simulation."
                            isStatusError = false
                        }
                    )

                    SimulationOptionItem(
                        title = "Simulate: Offline Network Timeout",
                        subtitle = "Tests graceful offline resilience without blocking billing",
                        selected = simulationMode == "NETWORK_ERROR",
                        onClick = {
                            simulationMode = "NETWORK_ERROR"
                            updateManager.preferences.simulationMode = "NETWORK_ERROR"
                            checkStatusMessage = "Active: Offline network simulation."
                            isStatusError = true
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        }
    )

    // Edit URL Dialog
    if (showEditUrlDialog) {
        AlertDialog(
            onDismissRequest = { showEditUrlDialog = false },
            title = { Text("Configure Update Source URL", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Enter the HTTPS URL pointing to your hosted version.json file (e.g. GitHub raw, S3, or your web server):",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = updateUrlInput,
                        onValueChange = { updateUrlInput = it },
                        label = { Text("Update JSON URL") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_update_source_url")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (updateUrlInput.isNotBlank()) {
                            updateManager.preferences.updateSourceUrl = updateUrlInput.trim()
                            showEditUrlDialog = false
                            Toast.makeText(context, "Update URL updated successfully", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditUrlDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SimulationOptionItem(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (selected) NavyPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(if (selected) NavyPrimary else Color.LightGray)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.sp,
                    color = if (selected) NavyPrimary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
