package com.statussaver.app.ui.screens.grid

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.statussaver.app.ui.theme.Background
import com.statussaver.app.ui.theme.PrimaryButton
import com.statussaver.app.ui.theme.PrimaryButtonText
import com.statussaver.app.ui.theme.Surface
import com.statussaver.app.ui.theme.SurfaceRaised
import com.statussaver.app.ui.theme.TextPrimary
import com.statussaver.app.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun GridScreen(
    mediaType: String,
    onBack: () -> Unit,
    onOpenPreview: (StatusMedia) -> Unit
) {
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

    LaunchedEffect(mediaType) {
        loadItems()
    }

    BackHandler(enabled = true) {
        if (selectionMode) {
            selectionMode = false
            selectedPaths = emptySet()
        } else {
            onBack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconCircleButton(
                        icon = Icons.AutoMirrored.Outlined.ArrowBack,
                        onClick = {
                            if (selectionMode) {
                                selectionMode = false
                                selectedPaths = emptySet()
                            } else {
                                onBack()
                            }
                        }
                    )
                    Spacer(Modifier.size(12.dp))
                    Text(
                        text = if (selectionMode) "${selectedPaths.size} selected" else title,
                        color = TextPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (selectionMode) {
                    IconCircleButton(
                        icon = Icons.Outlined.Close,
                        onClick = {
                            selectionMode = false
                            selectedPaths = emptySet()
                        }
                    )
                } else {
                    Surface(
                        color = Surface,
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = items.size.toString(),
                                color = TextSecondary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Box(modifier = Modifier.weight(1f)) {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = PrimaryButton)
                        }
                    }
                    items.isEmpty() -> {
                        EmptyState(isVideo = isVideo)
                    }
                    else -> {
                        StatusGrid(
                            items = items,
                            selectedPaths = selectedPaths,
                            selectionMode = selectionMode,
                            onItemClick = { media ->
                                if (selectionMode) {
                                    selectedPaths = if (media.path in selectedPaths) {
                                        selectedPaths - media.path
                                    } else {
                                        selectedPaths + media.path
                                    }
                                } else {
                                    onOpenPreview(media)
                                }
                            },
                            onItemLongClick = { media ->
                                if (!selectionMode) {
                                    selectionMode = true
                                    selectedPaths = setOf(media.path)
                                }
                            }
                        )
                    }
                }
            }

            if (selectionMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val allSelected = selectedPaths.size == items.size && items.isNotEmpty()
                    ActionButton(
                        text = if (allSelected) "Deselect All" else "Select All",
                        icon = Icons.Outlined.Check,
                        filled = false,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedPaths = if (allSelected) emptySet() else items.map { it.path }.toSet()
                        }
                    )
                    ActionButton(
                        text = "Delete",
                        icon = Icons.Outlined.Delete,
                        filled = true,
                        modifier = Modifier.weight(1f),
                        enabled = selectedPaths.isNotEmpty(),
                        onClick = { showDeleteDialog = true }
                    )
                }
            }
        }

        if (showDeleteDialog) {
            DeleteDialog(
                itemCount = selectedPaths.size,
                onRemoveFromApp = {
                    val paths = selectedPaths
                    repository.hideAllFromApp(paths)
                    items = items.filter { it.path !in paths }
                    selectedPaths = emptySet()
                    selectionMode = false
                    showDeleteDialog = false
                },
                onDeleteFromGallery = {
                    val selectedItems = items.filter { it.path in selectedPaths }
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            selectedItems.forEach { media ->
                                repository.deleteFromGallery(media)
                                repository.hideFromApp(media.path)
                            }
                        }
                        items = items.filter { it.path !in selectedPaths }
                        selectedPaths = emptySet()
                        selectionMode = false
                        showDeleteDialog = false
                    }
                },
                onCancel = { showDeleteDialog = false }
            )
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

@Composable
private fun ActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    filled: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        color = if (filled) PrimaryButton else SurfaceRaised,
        shape = RoundedCornerShape(18.dp),
        modifier = modifier
            .height(56.dp)
            .clickable(enabled = enabled) { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (filled) PrimaryButtonText else TextPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.size(8.dp))
            Text(
                text = text,
                color = if (filled) PrimaryButtonText else TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
