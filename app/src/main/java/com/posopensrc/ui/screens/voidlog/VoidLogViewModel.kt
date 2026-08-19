package com.posopensrc.ui.screens.voidlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.posopensrc.data.repository.TransactionRepository
import com.posopensrc.domain.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VoidLogUiState(
    val voidedTransactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val totalVoided: Double = 0.0,
    val voidedCount: Int = 0
)

@HiltViewModel
class VoidLogViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoidLogUiState())
    val uiState: StateFlow<VoidLogUiState> = _uiState.asStateFlow()

    init {
        loadVoidedTransactions()
    }

    private fun loadVoidedTransactions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            transactionRepository.getAllTransactionsIncludingVoided().collect { transactions ->
                val voided = transactions.filter { it.isVoided }
                val totalVoided = voided.sumOf { it.total }
                _uiState.update {
                    it.copy(
                        voidedTransactions = voided,
                        isLoading = false,
                        totalVoided = totalVoided,
                        voidedCount = voided.size
                    )
                }
            }
        }
    }
}
