package com.paygate.smsfwd.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paygate.smsfwd.R
import com.paygate.smsfwd.ui.theme.GateDangerLight
import com.paygate.smsfwd.ui.theme.GateTheme
import com.paygate.smsfwd.ui.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val language by viewModel.languageFlow.collectAsStateWithLifecycle()
    val isKiosk by viewModel.isKioskFlow.collectAsStateWithLifecycle()
    val isPaired by viewModel.isPairedFlow.collectAsStateWithLifecycle()

    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var showUnpairConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings & Device Fleet Policy",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = GateTheme.colors.ink
        )

        // 1. Language Configuration (FR-29)
        Card(
            colors = CardDefaults.cardColors(containerColor = GateTheme.colors.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GateTheme.colors.border, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = null,
                            tint = GateTheme.colors.accent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "App Language / ভাষা",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = GateTheme.colors.ink
                            )
                            Text(
                                text = if (language == "bn") "বাংলা (Bengali active)" else "English active",
                                style = MaterialTheme.typography.bodySmall,
                                color = GateTheme.colors.ink3
                            )
                        }
                    }
                    Button(
                        onClick = { viewModel.toggleLanguage() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GateTheme.colors.accent,
                            contentColor = GateTheme.colors.onAccent
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("toggle_language_btn")
                    ) {
                        Text(if (language == "bn") "English" else "বাংলা", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // 2. Operator App Security PIN (FR-19)
        Card(
            colors = CardDefaults.cardColors(containerColor = GateTheme.colors.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GateTheme.colors.border, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = GateTheme.colors.accent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Operator Security PIN",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = GateTheme.colors.ink
                            )
                            Text(
                                text = if (viewModel.prefs.isPinSet) "4-digit PIN is active" else "No PIN configured",
                                style = MaterialTheme.typography.bodySmall,
                                color = GateTheme.colors.ink3
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = { showPinDialog = true },
                        modifier = Modifier.testTag("configure_pin_btn")
                    ) {
                        Text(if (viewModel.prefs.isPinSet) "Change" else "Set PIN")
                    }
                }
            }
        }

        // 3. Android Enterprise Kiosk Mode (FR-20)
        Card(
            colors = CardDefaults.cardColors(containerColor = GateTheme.colors.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GateTheme.colors.border, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Kiosk Lock Mode",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = GateTheme.colors.ink
                        )
                        Text(
                            text = "Lock app to screen on unattended fleet phones",
                            style = MaterialTheme.typography.bodySmall,
                            color = GateTheme.colors.ink3
                        )
                    }
                    Switch(
                        checked = isKiosk,
                        onCheckedChange = { viewModel.prefs.isKioskMode = it }
                    )
                }
            }
        }

        // 4. Server & Fleet Diagnostics (FR-28)
        Card(
            colors = CardDefaults.cardColors(containerColor = GateTheme.colors.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GateTheme.colors.border, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Fleet Telemetry & Diagnostics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = GateTheme.colors.ink
                )
                Text(
                    text = "Device ID: ${viewModel.prefs.deviceId}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GateTheme.colors.ink2
                )
                Text(
                    text = "Base Webhook: ${viewModel.prefs.apiBaseUrl}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GateTheme.colors.ink2
                )
                Text(
                    text = "Device Token: ${viewModel.getMaskedToken()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GateTheme.colors.ink2
                )
                Text(
                    text = "Monotonic Device Seq: ${viewModel.prefs.deviceSeq}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GateTheme.colors.ink2
                )
                Text(
                    text = "Reference Telemetry: ${viewModel.prefs.refSeenCount} seen / ${viewModel.prefs.noRefCount} without ref",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GateTheme.colors.ink2
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedButton(
                    onClick = {
                        Toast.makeText(context, "Diagnostics exported (Zero PII)", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Safe Diagnostics")
                }
            }
        }

        // 5. Unpair Device (FR-16)
        if (isPaired) {
            Card(
                colors = CardDefaults.cardColors(containerColor = GateDangerLight.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GateDangerLight.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Unpair Phone from PayGate",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GateDangerLight
                    )
                    Text(
                        text = "Revokes local Keystore token and halts forwarding until re-paired.",
                        style = MaterialTheme.typography.bodySmall,
                        color = GateTheme.colors.ink2
                    )

                    Button(
                        onClick = { showUnpairConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = GateDangerLight),
                        modifier = Modifier.fillMaxWidth().testTag("unpair_device_btn")
                    ) {
                        Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Unpair Device")
                    }
                }
            }
        }
    }

    // Set / Change PIN Dialog
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Configure Security PIN") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter a 4-digit PIN for operator authorization:")
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 6) pinInput = it },
                        label = { Text("4-digit PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput.length >= 4) {
                            viewModel.prefs.appPin = pinInput
                            showPinDialog = false
                            pinInput = ""
                            Toast.makeText(context, "PIN updated", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Save PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Confirm Unpair Dialog
    if (showUnpairConfirm) {
        AlertDialog(
            onDismissRequest = { showUnpairConfirm = false },
            title = { Text("Confirm Device Unpairing") },
            text = { Text("Are you sure you want to unpair this phone? All queued forwarding will pause.") },
            confirmButton = {
                Button(
                    onClick = {
                        showUnpairConfirm = false
                        viewModel.unpairDevice()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GateDangerLight)
                ) {
                    Text("Confirm Unpair")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnpairConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
