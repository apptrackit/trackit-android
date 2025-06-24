package com.ballabotond.trackit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ballabotond.trackit.data.model.SyncState
import com.ballabotond.trackit.data.repository.SyncRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SyncViewModel(private val syncRepository: SyncRepository) : ViewModel() {
    
    private val _syncState = MutableStateFlow(SyncState())
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    init {
        // Monitor sync state changes
        viewModelScope.launch {
            syncRepository.getSyncStateFlow().collect { state ->
                val isOnline = syncRepository.isOnline()
                _syncState.value = state.copy(isOnline = isOnline)
            }
        }
        
        // Auto-sync when online and logged in
        viewModelScope.launch {
            syncState
                .filter { it.isOnline && it.pendingUploads > 0 && !it.isSyncing }
                .collect {
                    performSync()
                }
        }
    }
    
    fun performSync() {
        viewModelScope.launch {
            if (_syncState.value.isSyncing) return@launch
            
            _syncState.value = _syncState.value.copy(isSyncing = true)
            _errorMessage.value = null
            
            val result = syncRepository.syncAllData()
            result.fold(
                onSuccess = { state ->
                    _syncState.value = state
                },
                onFailure = { error ->
                    _syncState.value = _syncState.value.copy(isSyncing = false)
                    _errorMessage.value = error.message ?: "Sync failed"
                }
            )
        }
    }
    
    fun retryFailedSync() = performSync()
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    fun checkConnectivity() {
        viewModelScope.launch {
            val isOnline = syncRepository.isOnline()
            _syncState.value = _syncState.value.copy(isOnline = isOnline)
            
            if (isOnline && _syncState.value.pendingUploads > 0) {
                performSync()
            }
        }
    }
}
