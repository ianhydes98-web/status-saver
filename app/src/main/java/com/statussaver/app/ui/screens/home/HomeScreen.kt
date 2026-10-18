package com.statussaver.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.statussaver.app.data.repository.StatusRepository
import com.statussaver.app.ui.components.HomeTile
import com.statussaver.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(onOpenImages: () -> Unit, onOpenVideos: () -> Unit, onOpenSettings: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { StatusRepository(context) }
    var imageCount by remember { mutableStateOf(0) }
    var videoCount by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var refreshTrigger by remember { mutableStateOf(0) }

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

    Box(modifier = Modifier.fillMaxSize().background(Background).windowInsetsPadding(WindowInsets.systemBars)) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth().height(64.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("WAE", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                Row {
                    IconCircleButton(Icons.Outlined.Refresh) { refreshTrigger++ }
                    Spacer(Modifier.size(8.dp))
                    IconCircleButton(Icons.Outlined.Settings, onOpenSettings)
                }
            }
            Spacer(Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HomeTile("Images", imageCount, Icons.Outlined.Image, onOpenImages, Modifier.weight(1f).aspectRatio(0.85f))
                HomeTile("Videos", videoCount, Icons.Outlined.VideoLibrary, onOpenVideos, Modifier.weight(1f).aspectRatio(0.85f))
            }
        }
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().background(Background.copy(alpha = 0.75f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryButton)
            }
        }
    }
}

@Composable
private fun IconCircleButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(color = Surface, shape = RoundedCornerShape(999.dp), modifier = Modifier.size(40.dp).clickable { onClick() }) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = TextPrimary, modifier = Modifier.size(18.dp))
        }
    }
}
