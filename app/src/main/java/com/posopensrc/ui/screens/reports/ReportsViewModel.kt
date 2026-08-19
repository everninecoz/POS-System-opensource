package com.posopensrc.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.posopensrc.data.repository.TransactionRepository
import com.posopensrc.domain.model.SalesReport
import com.posopensrc.domain.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        loadTodayReport()
    }

    fun loadTodayReport() {
        val (start, end) = getTodayRange()
        loadReport(start, end)
    }

    fun loadWeekReport() {
        val (start, end) = getWeekRange()
        loadReport(start, end)
    }

    fun loadMonthReport() {
        val (start, end) = getMonthRange()
        loadReport(start, end)
    }

    private fun loadReport(startDate: Long, endDate: Long) {
        viewModelScope.launch {
            transactionRepository.getSalesReport(startDate, endDate).collect { report ->
                _uiState.value = _uiState.value.copy(report = report)
            }
        }

        viewModelScope.launch {
            transactionRepository.getTransactionsByDateRange(startDate, endDate).collect { transactions ->
                _uiState.value = _uiState.value.copy(transactions = transactions)
            }
        }
    }

    private fun getTodayRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val end = calendar.timeInMillis

        return Pair(start, end)
    }

    private fun getWeekRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis

        val end = System.currentTimeMillis()
        return Pair(start, end)
    }

    private fun getMonthRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis

        val end = System.currentTimeMillis()
        return Pair(start, end)
    }
}

data class ReportsUiState(
    val report: SalesReport? = null,
    val transactions: List<Transaction> = emptyList()
)
