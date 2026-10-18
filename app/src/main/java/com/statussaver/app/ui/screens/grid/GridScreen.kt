package com.statussaver.app.ui.screens.grid

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.statussaver.app.data.model.StatusMedia
import com.statussaver.app.data.repository.StatusRepository
import com.statussaver.app.ui.components.DeleteDialog
import com.statussaver.app.ui.components.EmptyState
import com.statussaver.app.ui.components.StatusGrid
import com.statussaver.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun GridScreen(mediaType: String, onBack: () -> Unit, onOpenPreview: (StatusMedia) -> Unit) {
    val context = LocalContext.current
    val repository = remember { StatusRepository(context) }
    val scope = rememberCoroutineScope()
    val isVideo = mediaType == "videos"
    val title = if (isVideo) "Videos" else "Images"

    var items by remember { mutableStateOf<List<StatusMedia>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectionMode by remember { mutableStateOf(false) }
    var selectedPaths by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(selectedPaths) {
        if (selectedPaths.isEmpty() && selectionMode) selectionMode = false
    }

    fun loadItems() {
        scope.launch {
            isLoading = true
            val result = withContext(Dispatchers.IO) {
                if (isVideo) repository.getVideos() else repository.getImages()
            }
            items = result
            isLoading = false
        }
    }

    LaunchedEffect(mediaType) { loadItems() }

    BackHandler(enabled = true) {
        if (selectionMode) { selectionMode = false; selectedPaths = emptySet() } else { onBack() }
    }

    Box(modifier = Modifier.fillMaxSize().background(Background).windowInsetsPadding(WindowInsets.systemBars)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconCircleButton(Icons.AutoMirrored.Outlined.ArrowBack) {
                        if (selectionMode) { selectionMode = false; selectedPaths = emptySet() } else { onBack() }
                    }
                    Spacer(Modifier.size(12.dp))
                    Text(if (selectionMode) "${selectedPaths.size} selected" else title, color = TextPrimary, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
                }
                if (selectionMode) {
                    IconCircleButton(Icons.Outlined.Close) { selectionMode = false; selectedPaths = emptySet() }
                } else {
                    Surface(color = Surface, shape = RoundedCornerShape(999.dp), modifier = Modifier.size(40.dp)) {
                        Box(contentAlignment = Alignment.Center) { Text(items.size.toString(), color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.weight(1f)) {
                when {
                    isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PrimaryButton) }
                    items.isEmpty() -> EmptyState(isVideo = isVideo)
                    else -> StatusGrid(items, selectedPaths, selectionMode, onItemClick = { media ->
                        if (selectionMode) {
                            selectedPaths = if (media.path in selectedPaths) selectedPaths - media.path else selectedPaths + media.path
                        } else onOpenPreview(media)
                    }, onItemLongClick = { media ->
                        if (!selectionMode) { selectionMode = true; selectedPaths = setOf(media.path) }
                    })
                }
            }
            if (selectionMode) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    val allSelected = selectedPaths.size == items.size && items.isNotEmpty()
                    ActionButton(if (allSelected) "Deselect All" else "Select All", Icons.Outlined.Check, false, Modifier.weight(1f)) {
                        selectedPaths = if (allSelected) emptySet() else items.map { it.path }.toSet()
                    }
                    ActionButton("Delete", Icons.Outlined.Delete, true, Modifier.weight(1f), enabled = selectedPaths.isNotEmpty()) { showDeleteDialog = true }
                }
            }
        }
        if (showDeleteDialog) {
            DeleteDialog(
                itemCount = selectedPaths.size,
                onRemoveFromApp = {
                    // Remove from UI list only
                    items = items.filter { it.path !in selectedPaths }
                    selectedPaths = emptySet(); selectionMode = false; showDeleteDialog = false
                },
                onDeleteFromGallery = {
                    val selectedItems = items.filter { it.path in selectedPaths }
                    scope.launch {
                        withContext(Dispatchers.IO) { 
                            selectedItems.forEach { repository.deleteFromGallery(it) } 
                        }
                        loadItems() // Reload to update the list
                        selectedPaths = emptySet(); selectionMode = false; showDeleteDialog = false
                    }
                },
                onCancel = { showDeleteDialog = false }
            )
        }
    }
}

@Composable
private fun IconCircleButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(color = Surface, shape = RoundedCornerShape(999.dp), modifier = Modifier.size(40.dp).clickable { onClick() }) {
        Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = TextPrimary, modifier = Modifier.size(18.dp)) }
    }
}

@Composable
private fun ActionButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, filled: Boolean, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Surface(color = if (filled) PrimaryButton else SurfaceRaised, shape = RoundedCornerShape(18.dp), modifier = modifier.height(56.dp).clickable(enabled = enabled) { onClick() }) {
        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = if (filled) PrimaryButtonText else TextPrimary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(8.dp))
            Text(text, color = if (filled) PrimaryButtonText else TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
