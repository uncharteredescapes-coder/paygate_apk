package com.paygate.smsfwd.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paygate.smsfwd.ui.components.GateButton
import com.paygate.smsfwd.ui.components.StatusChip
import com.paygate.smsfwd.ui.components.ChipStatus
import com.paygate.smsfwd.ui.theme.GateDangerLight
import com.paygate.smsfwd.ui.theme.GateSuccessLight
import com.paygate.smsfwd.ui.theme.GateTheme
import com.paygate.smsfwd.ui.viewmodel.MainViewModel
import com.paygate.smsfwd.ui.viewmodel.PairingUiState

@Composable
fun PairingScreen(
    viewModel: MainViewModel,
    onPairingSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pairingState by viewModel.pairingState.collectAsStateWithLifecycle()
    val isPaired by viewModel.isPairedFlow.collectAsStateWithLifecycle()
    val wallets by viewModel.walletsFlow.collectAsStateWithLifecycle()

    var baseUrlInput by remember { mutableStateOf(viewModel.prefs.apiBaseUrl) }
    var tokenInput by remember { mutableStateOf(viewModel.getRawToken()) }
    var deviceIdInput by remember { mutableStateOf(viewModel.prefs.deviceId) }
    var pairingCodeInput by remember { mutableStateOf("") }
    var showPairCodeSection by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Sensors,
            contentDescription = null,
            tint = GateTheme.colors.accent,
            modifier = Modifier.size(48.dp)
        )

        Text(
            text = if (isPaired) "Device Connection Active" else "Configure Forwarder Connection",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = GateTheme.colors.ink,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Direct backend connection for forwarder SMS ingestion. No unneeded pairing steps required.",
            style = MaterialTheme.typography.bodySmall,
            color = GateTheme.colors.ink2,
            textAlign = TextAlign.Center
        )

        // Status Feedback Banner
        when (val state = pairingState) {
            is PairingUiState.InProgress -> {
                CircularProgressIndicator(modifier = Modifier.size(32.dp), color = GateTheme.colors.accent)
                Text("Connecting and testing credential...", fontSize = 13.sp, color = GateTheme.colors.ink2)
            }
            is PairingUiState.Success -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = GateSuccessLight.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, GateSuccessLight, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GateSuccessLight)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(state.message, color = GateSuccessLight, fontWeight = FontWeight.Bold)
                        }

                        if (wallets.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Assigned Wallet SIMs:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = GateTheme.colors.ink)
                            wallets.forEach { w ->
                                Text("• Slot ${w.slot}: ${w.provider.uppercase()} (${w.numberMasked})", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = GateTheme.colors.ink2)
                            }
                        }
                    }
                }
            }
            is PairingUiState.Error -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = GateDangerLight.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, GateDangerLight, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = GateDangerLight)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(state.message, color = GateDangerLight, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    }
                }
            }
            else -> {}
        }

        // Active Connection Summary Card (when paired)
        if (isPaired) {
            Card(
                colors = CardDefaults.cardColors(containerColor = GateTheme.colors.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GateTheme.colors.accent.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Live Backend Target",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GateTheme.colors.ink
                        )
                        StatusChip(
                            label = "CONNECTED",
                            status = ChipStatus.SUCCESS
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Base URL:", fontSize = 12.sp, color = GateTheme.colors.ink3)
                        Text(
                            text = viewModel.prefs.apiBaseUrl,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            color = GateTheme.colors.ink
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Device Token:", fontSize = 12.sp, color = GateTheme.colors.ink3)
                        Text(
                            text = viewModel.getMaskedToken(),
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            color = GateTheme.colors.accentText
                        )
                    }

                    if (viewModel.prefs.deviceId.isNotBlank()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Device ID:", fontSize = 12.sp, color = GateTheme.colors.ink3)
                            Text(
                                text = viewModel.prefs.deviceId,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                color = GateTheme.colors.ink2
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.triggerSyncNow()
                                onPairingSuccess()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sync Queue", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Direct Token Configuration Card (Manual Token Entry)
        Card(
            colors = CardDefaults.cardColors(containerColor = GateTheme.colors.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, GateTheme.colors.border, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Direct Token Configuration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GateTheme.colors.ink
                    )
                    Text(
                        text = "Skip Pairing",
                        style = MaterialTheme.typography.labelSmall,
                        color = GateTheme.colors.accentText
                    )
                }

                Text(
                    text = "Enter your backend endpoint and provisioned Device Token to forward SMS directly.",
                    style = MaterialTheme.typography.bodySmall,
                    color = GateTheme.colors.ink2
                )

                OutlinedTextField(
                    value = baseUrlInput,
                    onValueChange = { baseUrlInput = it },
                    label = { Text("Base URL (e.g. https://paygator.vercel.app)") },
                    leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = GateTheme.colors.ink2) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GateTheme.colors.accent,
                        unfocusedBorderColor = GateTheme.colors.border
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_base_url")
                )

                OutlinedTextField(
                    value = tokenInput,
                    onValueChange = { tokenInput = it },
                    label = { Text("Device Token (Bearer)") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = GateTheme.colors.ink2) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GateTheme.colors.accent,
                        unfocusedBorderColor = GateTheme.colors.border
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_device_token")
                )

                OutlinedTextField(
                    value = deviceIdInput,
                    onValueChange = { deviceIdInput = it },
                    label = { Text("Device ID (Optional)") },
                    placeholder = { Text("e.g. dev_01") },
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = GateTheme.colors.ink2) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GateTheme.colors.accent,
                        unfocusedBorderColor = GateTheme.colors.border
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_device_id")
                )

                GateButton(
                    text = "Save Token & Connect Directly",
                    onClick = {
                        viewModel.configureManualToken(
                            token = tokenInput,
                            baseUrl = baseUrlInput,
                            deviceId = deviceIdInput
                        )
                    },
                    enabled = tokenInput.isNotBlank() && pairingState !is PairingUiState.InProgress,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "btn_connect_manual_token"
                )
            }
        }

        // Optional 10-Minute Pairing Code Fallback
        Card(
            colors = CardDefaults.cardColors(containerColor = GateTheme.colors.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, GateTheme.colors.border, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Or Use 10-Min Pairing Code",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = GateTheme.colors.ink
                    )
                    OutlinedButton(
                        onClick = { showPairCodeSection = !showPairCodeSection }
                    ) {
                        Text(if (showPairCodeSection) "Hide" else "Show", fontSize = 11.sp)
                    }
                }

                AnimatedVisibility(visible = showPairCodeSection) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "If pairing from the web dashboard via single-use code (expires in 10 minutes):",
                            style = MaterialTheme.typography.bodySmall,
                            color = GateTheme.colors.ink3
                        )

                        OutlinedTextField(
                            value = pairingCodeInput,
                            onValueChange = { pairingCodeInput = it.uppercase() },
                            label = { Text("Pairing Code") },
                            placeholder = { Text("ABCD-1234") },
                            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = GateTheme.colors.ink2) },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_pairing_code")
                        )

                        GateButton(
                            text = "Exchange Pairing Code",
                            onClick = { viewModel.pairDevice(pairingCodeInput, baseUrlInput) },
                            enabled = pairingCodeInput.isNotBlank() && pairingState !is PairingUiState.InProgress,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "btn_connect_code"
                        )
                    }
                }
            }
        }
    }
}
