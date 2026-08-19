package com.posopensrc.ui.screens.shifts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.posopensrc.core.session.SessionManager
import com.posopensrc.data.repository.ShiftRepository
import com.posopensrc.domain.model.Shift
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShiftUiState(
    val currentShift: Shift? = null,
    val recentShifts: List<Shift> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showOpenShiftDialog: Boolean = false,
    val showCloseShiftDialog: Boolean = false,
    val showShiftHistory: Boolean = false
)

@HiltViewModel
class ShiftViewModel @Inject constructor(
    private val shiftRepository: ShiftRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShiftUiState())
    val uiState: StateFlow<ShiftUiState> = _uiState.asStateFlow()

    init {
        loadCurrentShift()
        loadRecentShifts()
    }

    private fun loadCurrentShift() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            shiftRepository.getCurrentOpenShift().collect { shift ->
                _uiState.update {
                    it.copy(
                        currentShift = shift,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun loadRecentShifts() {
        viewModelScope.launch {
            shiftRepository.getAllShifts().collect { shifts ->
                _uiState.update { it.copy(recentShifts = shifts.take(10)) }
            }
        }
    }

    fun showOpenShiftDialog() {
        _uiState.update { it.copy(showOpenShiftDialog = true) }
    }

    fun hideOpenShiftDialog() {
        _uiState.update { it.copy(showOpenShiftDialog = false) }
    }

    fun showCloseShiftDialog() {
        _uiState.update { it.copy(showCloseShiftDialog = true) }
    }

    fun hideCloseShiftDialog() {
        _uiState.update { it.copy(showCloseShiftDialog = false) }
    }

    fun toggleShiftHistory() {
        _uiState.update { it.copy(showShiftHistory = !it.showShiftHistory) }
    }

    fun openShift(openingBalance: Double) {
        viewModelScope.launch {
            val userId = sessionManager.currentUserId.first()
            if (userId != null) {
                shiftRepository.openShift(userId, openingBalance)
                    .onSuccess {
                        hideOpenShiftDialog()
                    }
                    .onFailure { e ->
                        _uiState.update { it.copy(error = e.message) }
                    }
            }
        }
    }

    fun closeShift(closingBalance: Double, actualBalance: Double, notes: String?) {
        viewModelScope.launch {
            val shift = _uiState.value.currentShift
            if (shift != null) {
                shiftRepository.closeShift(shift.id, closingBalance, actualBalance, notes)
                    .onSuccess {
                        hideCloseShiftDialog()
                    }
                    .onFailure { e ->
                        _uiState.update { it.copy(error = e.message) }
                    }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
