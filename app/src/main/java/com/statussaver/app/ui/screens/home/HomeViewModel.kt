package com.statussaver.app.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.statussaver.app.data.model.StatusMedia
import com.statussaver.app.data.repository.StatusRepository
import com.statussaver.app.util.PermissionUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val images: List<StatusMedia> = emptyList(),
    val videos: List<StatusMedia> = emptyList(),
    val isLoading: Boolean = false,
    val hasPermission: Boolean = false,
    val error: String? = null,
    val selectedTab: Int = 0 // 0 = Images, 1 = Videos
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StatusRepository(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        checkPermissionAndLoad()
    }

    fun checkPermissionAndLoad() {
        val hasPerm = PermissionUtils.hasStoragePermission(getApplication())
        _uiState.update { it.copy(hasPermission = hasPerm) }
        if (hasPerm) {
            refresh()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val all = repository.getAllStatuses()
                _uiState.update {
                    it.copy(
                        images = all.filter { m -> m.isImage },
                        videos = all.filter { m -> m.isVideo },
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load statuses"
                    )
                }
            }
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    suspend fun saveMedia(media: StatusMedia): Result<Unit> {
        return repository.saveToGallery(media).map { }
    }
}
