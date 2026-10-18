package com.statussaver.app.ui.screens.preview

import android.net.Uri
import android.widget.Toast
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.statussaver.app.data.model.StatusMedia
import com.statussaver.app.data.repository.StatusRepository
import com.statussaver.app.ui.components.DeleteDialog
import com.statussaver.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PreviewScreen(media: StatusMedia, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { StatusRepository(context) }
    var isSaving by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Background).windowInsetsPadding(WindowInsets.systemBars)) {
        if (media.isImage) {
            AsyncImage(model = media.file, contentDescription = media.name, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
        } else {
            AndroidView(factory = { ctx ->
                VideoView(ctx).apply {
                    setVideoURI(Uri.fromFile(media.file))
                    setOnPreparedListener { it.isLooping = true; start() }
                }
            }, modifier = Modifier.fillMaxSize())
        }

        Surface(color = SurfaceRaised.copy(alpha = 0.7f), shape = RoundedCornerShape(999.dp), modifier = Modifier.padding(16.dp).size(40.dp).clickable { onBack() }) {
            Box(contentAlignment = Alignment.Center) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = TextPrimary, modifier = Modifier.size(18.dp)) }
        }

        Row(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(color = SurfaceRaised.copy(alpha = 0.85f), shape = RoundedCornerShape(18.dp), modifier = Modifier.weight(1f).height(56.dp).clickable { showDeleteDialog = true }) {
                Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(Icons.Outlined.Delete, null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("Delete", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Surface(color = PrimaryButton, shape = RoundedCornerShape(18.dp), modifier = Modifier.weight(1f).height(56.dp).clickable(enabled = !isSaving) {
                if (isSaving) return@clickable
                isSaving = true
                scope.launch {
                    val result = repository.saveToGallery(media)
                    isSaving = false
                    Toast.makeText(context, if (result.isSuccess) "Saved to Gallery" else "Failed to save", Toast.LENGTH_SHORT).show()
                }
            }) {
                Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    if (isSaving) CircularProgressIndicator(color = PrimaryButtonText, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                    else {
                        Icon(Icons.Outlined.Download, null, tint = PrimaryButtonText, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(8.dp))
                        Text("Save", color = PrimaryButtonText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        if (showDeleteDialog) {
            DeleteDialog(
                itemCount = 1,
                onRemoveFromApp = { showDeleteDialog = false; onBack() },
                onDeleteFromGallery = {
                    scope.launch {
                        withContext(Dispatchers.IO) { repository.deleteFromGallery(media) }
                        showDeleteDialog = false; onBack()
                    }
                },
                onCancel = { showDeleteDialog = false }
            )
        }
    }
}
