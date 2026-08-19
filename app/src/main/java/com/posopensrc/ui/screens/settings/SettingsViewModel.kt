package com.posopensrc.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.posopensrc.core.session.SessionManager
import com.posopensrc.data.repository.SettingsRepository
import com.posopensrc.domain.model.Settings
import com.posopensrc.printer.PaperSize
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _language = MutableStateFlow("id")
    val language: StateFlow<String> = _language.asStateFlow()

    init {
        loadSettings()
        observeSession()
    }

    private fun observeSession() {
        viewModelScope.launch {
            sessionManager.isDarkMode.collect {
                _isDarkMode.value = it
            }
        }
        viewModelScope.launch {
            sessionManager.language.collect {
                _language.value = it
            }
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.getSettings().collect { settings ->
                _uiState.value = _uiState.value.copy(settings = settings)
            }
        }
    }

    fun updateStoreInfo(
        name: String,
        address: String?,
        phone: String?
    ) {
        viewModelScope.launch {
            settingsRepository.updateStoreInfo(name, address, phone)
        }
    }

    fun updateReceiptFooter(footer: String) {
        viewModelScope.launch {
            val currentSettings = _uiState.value.settings
            settingsRepository.updateSettings(
                currentSettings.copy(receiptFooter = footer)
            )
        }
    }

    fun updateTaxPercentage(percentage: Double) {
        viewModelScope.launch {
            settingsRepository.updateTaxPercentage(percentage)
        }
    }

    fun onPaperSizeSelected(size: PaperSize) {
        _uiState.value = _uiState.value.copy(selectedPaperSize = size)
    }
}

data class SettingsUiState(
    val settings: Settings = Settings(),
    val selectedPaperSize: PaperSize = PaperSize.WIDTH_80MM
)
