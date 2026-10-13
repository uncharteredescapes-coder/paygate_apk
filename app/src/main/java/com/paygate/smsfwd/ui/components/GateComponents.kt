package com.paygate.smsfwd.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paygate.smsfwd.ui.theme.GateMonoFontFamily
import com.paygate.smsfwd.ui.theme.GateTheme

/**
 * Gate UI Design System Component Suite (DESIGN.md / App Icon specification)
 *
 * Rules:
 * - Palette derived directly from icon:
 *   Near-black #0A0A0A canvas
 *   White #F8FAFC arch text
 *   Emerald #10B981 coin accent: primary action, money, and success
 * - Text on emerald is ALWAYS #0A0A0A, never white (7.8:1 contrast).
 * - Single primary action per screen.
 * - Tabular numbers on financial data (Money is the hero).
 * - Hairline borders (1.dp) over drop shadows.
 */

// ---------------------------------------------------------------------------
// 1. Brand Dot (bKash, Nagad, Rocket, Generic)
// ---------------------------------------------------------------------------
@Composable
fun ProviderBrandDot(
    provider: String?,
    size: Dp = 8.dp,
    modifier: Modifier = Modifier
) {
    val color = when (provider?.lowercase()) {
        "bkash" -> GateTheme.colors.bkash
        "nagad" -> GateTheme.colors.nagad
        "rocket" -> GateTheme.colors.rocket
        else -> GateTheme.colors.textSubtle
    }
    Box(
        modifier = modifier
            .size(size)
            .background(color, CircleShape)
    )
}

// ---------------------------------------------------------------------------
// 2. Gate UI Primary, Outlined & Danger Button (10.dp radius, 48.dp touch target)
// ---------------------------------------------------------------------------
@Composable
fun GateButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isDanger: Boolean = false,
    isOutlined: Boolean = false,
    isPrimary: Boolean = true,
    leadingIcon: ImageVector? = null,
    testTag: String = "",
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
) {
    val colors = GateTheme.colors
    val shape = RoundedCornerShape(10.dp)

    if (isOutlined) {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            shape = shape,
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = colors.text,
                disabledContentColor = colors.textSubtle
            ),
            contentPadding = contentPadding,
            modifier = modifier
                .height(48.dp)
                .testTag(testTag)
        ) {
            if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(16.dp), tint = colors.textMuted)
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    } else {
        // Emerald #10B981 is used for primary actions with #0A0A0A text.
        val containerColor = when {
            isDanger -> colors.danger
            isPrimary -> colors.accent // Emerald #10B981
            else -> colors.surface2
        }
        val contentColor = when {
            isDanger -> Color.White
            isPrimary -> colors.onAccent // #0A0A0A (Near-black)
            else -> colors.text
        }

        Button(
            onClick = onClick,
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor,
                disabledContainerColor = colors.surface2,
                disabledContentColor = colors.textSubtle
            ),
            contentPadding = contentPadding,
            modifier = modifier
                .height(48.dp)
                .testTag(testTag)
        ) {
            if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(16.dp), tint = contentColor)
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 3. Status Chip (Full rounded pill, subtle tint)
// ---------------------------------------------------------------------------
enum class ChipStatus {
    SUCCESS, WARNING, DANGER, NEUTRAL
}

@Composable
fun StatusChip(
    label: String,
    status: ChipStatus,
    modifier: Modifier = Modifier
) {
    val colors = GateTheme.colors
    val (bgColor, textColor) = when (status) {
        ChipStatus.SUCCESS -> colors.accentSubtle to colors.accentText
        ChipStatus.WARNING -> colors.warning.copy(alpha = 0.14f) to colors.warning
        ChipStatus.DANGER -> colors.danger.copy(alpha = 0.12f) to colors.danger
        ChipStatus.NEUTRAL -> colors.surface2 to colors.textMuted
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9999.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.25f), RoundedCornerShape(9999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 14.sp
        )
    }
}

// ---------------------------------------------------------------------------
// 4. Hairline Card Container (14.dp radius, 1.dp hairline border, zero drop shadow)
// ---------------------------------------------------------------------------
@Composable
fun GateCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = GateTheme.colors.surface,
    borderColor: Color = GateTheme.colors.border,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        content()
    }
}

// ---------------------------------------------------------------------------
// 5. Stat Card (Tabular Telemetry Numbers)
// ---------------------------------------------------------------------------
@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    statusColor: Color? = null,
    modifier: Modifier = Modifier
) {
    val colors = GateTheme.colors

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = GateMonoFontFamily,
                fontWeight = FontWeight.Bold,
                color = statusColor ?: colors.text
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSubtle
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 6. Device Health Row with 1-Tap Fix Action
// ---------------------------------------------------------------------------
@Composable
fun HealthRow(
    icon: ImageVector,
    label: String,
    isHealthy: Boolean,
    onFixClick: () -> Unit,
    statusHint: String = if (isHealthy) "Active" else "Action needed",
    modifier: Modifier = Modifier
) {
    val colors = GateTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (isHealthy) colors.accentSubtle else colors.warning.copy(alpha = 0.14f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isHealthy) colors.accentText else colors.warning,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.text
                )
                Text(
                    text = statusHint,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textMuted
                )
            }
        }

        if (isHealthy) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9999.dp))
                    .background(colors.accentSubtle)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "✓ OK",
                    color = colors.accentText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Button(
                onClick = onFixClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accent,
                    contentColor = colors.onAccent // #0A0A0A on emerald
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text("Fix", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 7. Reference Chip (Gate UI emerald accent-subtle pill with JetBrains Mono)
// ---------------------------------------------------------------------------
@Composable
fun ReferenceChip(
    reference: String?,
    modifier: Modifier = Modifier
) {
    val colors = GateTheme.colors

    if (reference.isNullOrBlank()) {
        Text(
            text = "No Ref",
            style = MaterialTheme.typography.labelSmall,
            color = colors.textSubtle,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(6.dp))
                .background(colors.accentSubtle)
                .border(1.dp, colors.accent.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = "Ref: $reference",
                color = colors.accentText,
                fontFamily = GateMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                lineHeight = 14.sp
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 8. Masked Tabular Value (TrxID / Token Telemetry)
// ---------------------------------------------------------------------------
@Composable
fun MaskedValue(
    value: String?,
    isRevealed: Boolean,
    modifier: Modifier = Modifier
) {
    val text = if (value.isNullOrBlank()) {
        "N/A"
    } else if (isRevealed) {
        value
    } else {
        if (value.length > 4) "••••" + value.takeLast(4) else "••••"
    }

    Text(
        text = text,
        fontFamily = GateMonoFontFamily,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.bodyMedium,
        color = GateTheme.colors.text,
        modifier = modifier
    )
}

// ---------------------------------------------------------------------------
// 9. Security PIN Lock Dialog
// ---------------------------------------------------------------------------
@Composable
fun PinLockDialog(
    title: String = "Security PIN Verification",
    subtitle: String = "Enter your 4-digit operator PIN to proceed",
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val colors = GateTheme.colors
    var pinInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold, color = colors.text) },
        text = {
            Column {
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = colors.textMuted)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { if (it.length <= 4) pinInput = it },
                    label = { Text("4-digit PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.border,
                        focusedLabelColor = colors.accentText,
                        cursorColor = colors.accent
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_security_pin")
                )
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(errorMessage!!, color = colors.danger, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (pinInput.length == 4) {
                        onSuccess()
                    } else {
                        errorMessage = "Enter exactly 4 digits"
                    }
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accent,
                    contentColor = colors.onAccent // #0A0A0A on emerald
                ),
                modifier = Modifier.testTag("btn_pin_confirm")
            ) {
                Text("Confirm", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.textMuted)
            }
        },
        containerColor = colors.surface,
        shape = RoundedCornerShape(14.dp)
    )
}
