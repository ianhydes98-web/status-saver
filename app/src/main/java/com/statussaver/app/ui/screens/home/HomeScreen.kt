package com.statussaver.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.statussaver.app.data.repository.StatusRepository
import com.statussaver.app.ui.components.HomeTile
import com.statussaver.app.ui.theme.Background
import com.statussaver.app.ui.theme.PrimaryButton
import com.statussaver.app.ui.theme.Surface
import com.statussaver.app.ui.theme.TextPrimary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(
    onOpenImages: () -> Unit,
    onOpenVideos: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { StatusRepository(context) }

    var imageCount by remember { mutableStateOf(0) }
    var videoCount by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var refreshTrigger by remember { mutableStateOf(0) }

    // This LaunchedEffect will trigger every time the screen is composed or refreshTrigger changes
    LaunchedEffect(refreshTrigger) {
        isLoading = true
        val (imgs, vids) = withContext(Dispatchers.IO) {
            val images = repository.getImages().size
            val videos = repository.getVideos().size
            images to videos
        }
        imageCount = imgs
        videoCount = vids
        isLoading = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .windowInsetsPadding(WindowInsets.systemBars) // Fix for Safe Area
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "WAE", // Fixed Name
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row {
                    IconCircleButton(
                        icon = Icons.Outlined.Refresh,
                        onClick = { refreshTrigger++ }
                    )
                    Spacer(Modifier.size(8.dp))
                    IconCircleButton(
                        icon = Icons.Outlined.Settings,
                        onClick = onOpenSettings
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // 2-column tiles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HomeTile(
                    title = "Images",
                    count = imageCount,
                    icon = Icons.Outlined.Image,
                    onClick = onOpenImages,
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(0.85f)
                )
                HomeTile(
                    title = "Videos",
                    count = videoCount,
                    icon = Icons.Outlined.VideoLibrary,
                    onClick = onOpenVideos,
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(0.85f)
                )
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Background.copy(alpha = 0.75f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryButton)
            }
        }
    }
}

@Composable
private fun IconCircleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        color = Surface,
        shape = RoundedCornerShape(999.dp),
        modifier = Modifier
            .size(40.dp)
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
