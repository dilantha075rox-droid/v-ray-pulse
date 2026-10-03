package com.example.ui

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.CyberBgDeep
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanDim
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberSurfaceLight
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary

@Composable
fun ImportVlessDialog(
    onDismiss: () -> Unit,
    onImport: (String) -> Boolean
) {
    val context = LocalContext.current
    var urlText by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf<String?>(null) }

    fun pasteFromClipboard() {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            if (clipboard != null && clipboard.hasPrimaryClip()) {
                val clipData = clipboard.primaryClip
                if (clipData != null && clipData.itemCount > 0) {
                    val pasted = clipData.getItemAt(0).coerceToText(context).toString().trim()
                    if (pasted.isNotBlank()) {
                        urlText = pasted
                        inputError = null
                    }
                }
            }
        } catch (_: Exception) {}
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .background(CyberBgDeep)
                .border(1.dp, CyberCardBorder, RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberSurfaceLight)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "VLESS Key Icon",
                            tint = CyberCyan
                        )
                    }
                    Column {
                        Text(
                            text = "IMPORT VLESS",
                            color = CyberCyan,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Paste standard vless:// URI configuration",
                            color = CyberTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                // Paste from clipboard button
                OutlinedButton(
                    onClick = { pasteFromClipboard() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("paste_clipboard_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = CyberCyanDim
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(CyberCardBorder)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = "Paste from clipboard",
                        tint = CyberCyanDim
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PASTE FROM CLIPBOARD",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                }

                // Text field
                OutlinedTextField(
                    value = urlText,
                    onValueChange = {
                        urlText = it
                        inputError = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .testTag("vless_url_input"),
                    placeholder = {
                        Text(
                            text = "vless://uuid@host:443?security=reality&encryption=none&pbk=...&type=tcp#ServerName",
                            color = CyberTextSecondary.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CyberSurfaceLight.copy(alpha = 0.5f),
                        unfocusedContainerColor = CyberSurfaceLight.copy(alpha = 0.3f),
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CyberCardBorder,
                        focusedTextColor = CyberTextPrimary,
                        unfocusedTextColor = CyberTextPrimary,
                        cursorColor = CyberCyan
                    ),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 6,
                    isError = inputError != null
                )

                // Error text if any
                if (inputError != null) {
                    Text(
                        text = inputError.orEmpty(),
                        color = CyberRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("cancel_import_button")
                    ) {
                        Text(
                            text = "CANCEL",
                            color = CyberTextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            if (urlText.isBlank()) {
                                inputError = "Please enter or paste a VLESS URL"
                            } else {
                                val success = onImport(urlText)
                                if (!success) {
                                    inputError = "Invalid VLESS URL format"
                                }
                            }
                        },
                        modifier = Modifier.testTag("submit_import_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberCyan,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "IMPORT",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}
