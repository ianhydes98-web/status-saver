package com.statussaver.app.ui.screens.settings

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.statussaver.app.service.AutoSaveService
import com.statussaver.app.util.Prefs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var autoSaveEnabled by remember { mutableStateOf(Prefs.isAutoSaveEnabled(context)) }

    // Notification permission launcher (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            enableAutoSave(context, true)
            autoSaveEnabled = true
        } else {
            Toast.makeText(context, "Notification permission needed for auto-save", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Auto Save toggle
            ListItem(
                headlineContent = { Text("Auto Save Statuses") },
                supportingContent = {
                    Text(
                        if (autoSaveEnabled)
                            "On – new statuses are saved automatically"
                        else
                            "Off – save manually from the gallery"
                    )
                },
                trailingContent = {
                    Switch(
                        checked = autoSaveEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                // On Android 13+ we need notification permission for the foreground service
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    enableAutoSave(context, true)
                                    autoSaveEnabled = true
                                }
                            } else {
                                enableAutoSave(context, false)
                                autoSaveEnabled = false
                            }
                        }
                    )
                }
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Save Location") },
                supportingContent = { Text("Pictures / WhatsApp Statuses") }
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text("About") },
                supportingContent = { Text("Version 1.0.0") },
                leadingContent = {
                    Icon(Icons.Outlined.Info, contentDescription = null)
                }
            )

            HorizontalDivider()

            Text(
                text = "Auto Save runs a lightweight background service that watches the WhatsApp status folder and saves new media the moment you view them.\n\nNo internet • No tracking • Battery-friendly",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

private fun enableAutoSave(context: android.content.Context, enabled: Boolean) {
    Prefs.setAutoSaveEnabled(context, enabled)
    if (enabled) {
        AutoSaveService.start(context)
        Toast.makeText(context, "Auto Save enabled", Toast.LENGTH_SHORT).show()
    } else {
        AutoSaveService.stop(context)
        Toast.makeText(context, "Auto Save disabled", Toast.LENGTH_SHORT).show()
    }
}
