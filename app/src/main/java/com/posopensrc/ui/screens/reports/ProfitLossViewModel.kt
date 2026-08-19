package com.posopensrc.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.posopensrc.data.repository.ProfitLossRepository
import com.posopensrc.domain.model.ProfitLossReport
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfitLossUiState(
    val report: ProfitLossReport? = null,
    val isLoading: Boolean = false,
    val selectedPeriod: String = "today",
    val error: String? = null
)

@HiltViewModel
class ProfitLossViewModel @Inject constructor(
    private val profitLossRepository: ProfitLossRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfitLossUiState())
    val uiState: StateFlow<ProfitLossUiState> = _uiState.asStateFlow()

    init {
        loadTodayReport()
    }

    fun loadTodayReport() {
        _uiState.update { it.copy(selectedPeriod = "today", isLoading = true) }
        viewModelScope.launch {
            profitLossRepository.getTodayProfitLoss().collect { report ->
                _uiState.update {
                    it.copy(report = report, isLoading = false)
                }
            }
        }
    }

    fun loadWeekReport() {
        _uiState.update { it.copy(selectedPeriod = "week", isLoading = true) }
        viewModelScope.launch {
            profitLossRepository.getWeekProfitLoss().collect { report ->
                _uiState.update {
                    it.copy(report = report, isLoading = false)
                }
            }
        }
    }

    fun loadMonthReport() {
        _uiState.update { it.copy(selectedPeriod = "month", isLoading = true) }
        viewModelScope.launch {
            profitLossRepository.getMonthProfitLoss().collect { report ->
                _uiState.update {
                    it.copy(report = report, isLoading = false)
                }
            }
        }
    }
}
