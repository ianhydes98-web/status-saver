package com.statussaver.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.statussaver.app.BuildConfig
import com.statussaver.app.data.repository.StatusRepository
import com.statussaver.app.service.AutoSaveService
import com.statussaver.app.ui.theme.Background
import com.statussaver.app.ui.theme.PrimaryButton
import com.statussaver.app.ui.theme.PrimaryButtonText
import com.statussaver.app.ui.theme.Surface
import com.statussaver.app.ui.theme.SurfaceRaised
import com.statussaver.app.ui.theme.TextPrimary
import com.statussaver.app.ui.theme.TextSecondary
import com.statussaver.app.util.Prefs

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { StatusRepository(context) }

    var autoSave by remember { mutableStateOf(Prefs.isAutoSaveEnabled(context)) }
    var cacheSize by remember { mutableStateOf(repository.getCacheSize()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(24.dp))

        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Surface,
                shape = RoundedCornerShape(999.dp),
                modifier = Modifier
                    .size(40.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onBack
                    )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(Modifier.size(12.dp))
            Text(
                text = "Settings",
                color = TextPrimary,
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(16.dp))

        // Section label
        Text(
            text = "GENERAL",
            color = TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.4.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )

        // Auto-save row
        SettingsRow(
            icon = Icons.Outlined.Bolt,
            title = "Auto-save",
            subtitle = "Save new statuses automatically",
            trailing = {
                SwitchPill(
                    checked = autoSave,
                    onToggle = { newValue ->
                        autoSave = newValue
                        Prefs.setAutoSaveEnabled(context, newValue)
                        if (newValue) {
                            AutoSaveService.start(context)
                        } else {
                            AutoSaveService.stop(context)
                        }
                    }
                )
            }
        )

        Spacer(Modifier.height(8.dp))

        // Clear cache row
        SettingsRow(
            icon = Icons.Outlined.DeleteSweep,
            title = "Clear App Cache",
            subtitle = "Free up space",
            trailing = {
                Text(
                    text = formatBytes(cacheSize),
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            },
            onClick = {
                repository.clearAppCache()
                cacheSize = repository.getCacheSize()
            }
        )

        Spacer(Modifier.height(24.dp))

        // Section label
        Text(
            text = "INFO",
            color = TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.4.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )

        // App version row
        SettingsRow(
            icon = Icons.Outlined.Bolt,
            title = "App Version",
            subtitle = null,
            trailing = {
                Text(
                    text = BuildConfig.VERSION_NAME,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        )
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    trailing: @Composable () -> Unit = {},
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    Surface(
        color = Surface,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(SurfaceRaised, shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.size(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (subtitle != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
            trailing()
        }
    }
}

@Composable
private fun SwitchPill(
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Surface(
        color = if (checked) PrimaryButton else SurfaceRaised,
        shape = CircleShape,
        modifier = Modifier
            .width(48.dp)
            .height(28.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onToggle(!checked) }
            )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .align(if (checked) Alignment.CenterEnd else Alignment.CenterStart)
                    .padding(2.dp)
                    .size(24.dp)
                    .background(
                        if (checked) Background else TextSecondary,
                        shape = CircleShape
                    )
            )
        }
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 MB"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1) {
        String.format("%.0f MB", mb)
    } else {
        String.format("%.0f KB", kb)
    }
}
