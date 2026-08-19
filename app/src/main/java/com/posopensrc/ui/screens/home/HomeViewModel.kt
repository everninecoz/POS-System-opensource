package com.posopensrc.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.posopensrc.core.session.SessionManager
import com.posopensrc.data.repository.ProductRepository
import com.posopensrc.data.repository.TransactionRepository
import com.posopensrc.domain.model.DashboardStats
import com.posopensrc.domain.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val productRepository: ProductRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            combine(
                transactionRepository.getTodaySales(),
                transactionRepository.getTodayTransactionCount(),
                productRepository.getProductCount(),
                productRepository.getLowStockCount(),
                transactionRepository.getWeekSales(),
                transactionRepository.getMonthSales()
            ) { values ->
                DashboardStats(
                    todaySales = values[0] as Double,
                    todayTransactions = values[1] as Int,
                    totalProducts = values[2] as Int,
                    lowStockCount = values[3] as Int,
                    weekSales = values[4] as Double,
                    monthSales = values[5] as Double
                )
            }.collect { stats ->
                _uiState.value = _uiState.value.copy(
                    stats = stats,
                    isLoading = false
                )
            }
        }

        viewModelScope.launch {
            transactionRepository.getAllTransactions().collect { transactions ->
                _uiState.value = _uiState.value.copy(
                    recentTransactions = transactions.take(5)
                )
            }
        }
    }

    fun refresh() {
        loadDashboardData()
    }
}

data class HomeUiState(
    val stats: DashboardStats = DashboardStats(),
    val recentTransactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false
)
