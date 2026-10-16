package com.statussaver.app.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.statussaver.app.data.model.StatusMedia
import com.statussaver.app.ui.components.EmptyState
import com.statussaver.app.ui.components.StatusGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onMediaClick: (StatusMedia) -> Unit,
    onSettingsClick: () -> Unit,
    onRequestPermission: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Status Saver") },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (!state.hasPermission) {
                EmptyState(
                    title = "Storage Permission Needed",
                    subtitle = "Allow access so we can find WhatsApp statuses on your device.",
                    buttonText = "Allow Access",
                    onButtonClick = onRequestPermission
                )
                return@Column
            }

            TabRow(
                selectedTabIndex = state.selectedTab,
                containerColor = MaterialTheme.colorScheme.background
            ) {
                Tab(
                    selected = state.selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    text = { Text("Images (${state.images.size})") }
                )
                Tab(
                    selected = state.selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    text = { Text("Videos (${state.videos.size})") }
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    state.selectedTab == 0 && state.images.isEmpty() -> {
                        EmptyState(
                            title = "No Images Found",
                            subtitle = "View some photo statuses in WhatsApp, then pull to refresh.",
                            buttonText = "Refresh",
                            onButtonClick = { viewModel.refresh() }
                        )
                    }
                    state.selectedTab == 1 && state.videos.isEmpty() -> {
                        EmptyState(
                            title = "No Videos Found",
                            subtitle = "View some video statuses in WhatsApp, then pull to refresh.",
                            buttonText = "Refresh",
                            onButtonClick = { viewModel.refresh() }
                        )
                    }
                    else -> {
                        val items = if (state.selectedTab == 0) state.images else state.videos
                        StatusGrid(
                            items = items,
                            onItemClick = onMediaClick
                        )
                    }
                }
            }
        }
    }
}
