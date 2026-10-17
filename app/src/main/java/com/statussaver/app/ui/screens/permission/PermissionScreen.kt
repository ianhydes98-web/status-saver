package com.statussaver.app.ui.screens.permission

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.statussaver.app.ui.theme.Background
import com.statussaver.app.ui.theme.PrimaryButton
import com.statussaver.app.ui.theme.PrimaryButtonText
import com.statussaver.app.ui.theme.Surface
import com.statussaver.app.ui.theme.SurfaceRaised
import com.statussaver.app.ui.theme.TextPrimary
import com.statussaver.app.ui.theme.TextSecondary
import com.statussaver.app.util.PermissionUtils
import kotlinx.coroutines.delay

@Composable
fun PermissionScreen(
    onGrant: () -> Unit,
    onExit: () -> Unit,
    onPermissionGranted: () -> Unit
) {
    val context = LocalContext.current
    var checkTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(checkTrigger) {
        while (true) {
            if (PermissionUtils.hasStoragePermission(context)) {
                onPermissionGranted()
                break
            }
            delay(1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(80.dp))

        Text(
            text = "Save every status. Quietly.",
            color = TextPrimary,
            fontSize = 36.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 38.sp,
            letterSpacing = (-0.035).sp
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Status Saver needs access to your files to find WhatsApp statuses.",
            color = TextSecondary,
            fontSize = 14.sp,
            lineHeight = 22.sp
        )

        Spacer(Modifier.height(32.dp))

        FeatureCard(
            icon = Icons.Outlined.Folder,
            title = "Access your files",
            subtitle = "Read statuses stored on your device."
        )
        Spacer(Modifier.height(10.dp))
        FeatureCard(
            icon = Icons.Outlined.Bolt,
            title = "Auto-save new statuses",
            subtitle = "Saved the moment they appear."
        )
        Spacer(Modifier.height(10.dp))
        FeatureCard(
            icon = Icons.Outlined.Shield,
            title = "No data collection",
            subtitle = "Everything stays on your phone."
        )

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Exit App",
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier
                    .clickable { onExit() }
                    .padding(8.dp)
            )

            Surface(
                color = PrimaryButton,
                shape = RoundedCornerShape(999.dp),
                modifier = Modifier
                    .height(48.dp)
                    .clickable {
                        onGrant()
                        checkTrigger++
                    }
            ) {
                Row(
                    modifier = Modifier.padding(start = 20.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Grant Permission",
                        color = PrimaryButtonText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.size(12.dp))
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(PrimaryButtonText, shape = RoundedCornerShape(999.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = PrimaryButton,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureCard(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Surface(
        color = Surface,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
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
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.size(14.dp))
            Column {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
        }
    }
}
