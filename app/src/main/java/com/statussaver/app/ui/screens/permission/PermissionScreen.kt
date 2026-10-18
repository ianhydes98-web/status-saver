package com.statussaver.app.ui.screens.permission

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.statussaver.app.ui.theme.*
import com.statussaver.app.util.PermissionUtils

@Composable
fun PermissionScreen(
    onExit: () -> Unit,
    onPermissionGranted: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // THE FIX: The exact moment you return from settings, this fires and checks the permission
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (PermissionUtils.hasStoragePermission(context)) {
                    onPermissionGranted()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(40.dp))
        Text("Save every status. Quietly.", color = TextPrimary, fontSize = 36.sp, fontWeight = FontWeight.SemiBold, lineHeight = 38.sp, letterSpacing = (-0.035).sp)
        Spacer(Modifier.height(16.dp))
        Text("WAE needs access to your files to find WhatsApp statuses.", color = TextSecondary, fontSize = 14.sp, lineHeight = 22.sp)
        Spacer(Modifier.height(32.dp))
        FeatureCard(Icons.Outlined.Folder, "Access your files", "Read statuses stored on your device.")
        Spacer(Modifier.height(10.dp))
        FeatureCard(Icons.Outlined.Bolt, "Auto-save new statuses", "Saved the moment they appear.")
        Spacer(Modifier.height(10.dp))
        FeatureCard(Icons.Outlined.Shield, "No data collection", "Everything stays on your phone.")
        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Exit App", color = TextSecondary, fontSize = 14.sp, modifier = Modifier.clickable { onExit() }.padding(8.dp))
            Surface(
                color = PrimaryButton,
                shape = RoundedCornerShape(999.dp),
                modifier = Modifier.height(48.dp).clickable {
                    val activity = context as? android.app.Activity
                    if (activity != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            val intent = PermissionUtils.getStoragePermissionIntent(activity)
                            if (intent != null) activity.startActivity(intent)
                        } else {
                            PermissionUtils.requestLegacyStoragePermission(activity)
                        }
                    }
                }
            ) {
                Row(modifier = Modifier.padding(start = 20.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Grant Permission", color = PrimaryButtonText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.size(12.dp))
                    Box(modifier = Modifier.size(32.dp).background(PrimaryButtonText, shape = RoundedCornerShape(999.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.ArrowForward, null, tint = PrimaryButton, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureCard(icon: ImageVector, title: String, subtitle: String) {
    Surface(color = Surface, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(SurfaceRaised, shape = RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = TextPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.size(14.dp))
            Column {
                Text(title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(2.dp))
                Text(subtitle, color = TextSecondary, fontSize = 13.sp)
            }
        }
    }
}
