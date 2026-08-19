package com.posopensrc.ui.screens.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.posopensrc.core.session.SessionManager
import com.posopensrc.data.repository.ProductRepository
import com.posopensrc.data.repository.StockOpnameRepository
import com.posopensrc.domain.model.Product
import com.posopensrc.domain.model.StockOpname
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StockOpnameUiState(
    val products: List<Product> = emptyList(),
    val stockOpnames: List<StockOpname> = emptyList(),
    val selectedProducts: Map<Long, Int> = emptyMap(), // productId -> physicalStock
    val isLoading: Boolean = false,
    val error: String? = null,
    val showCreateDialog: Boolean = false,
    val showHistory: Boolean = false,
    val lastOpname: StockOpname? = null
)

@HiltViewModel
class StockOpnameViewModel @Inject constructor(
    private val stockOpnameRepository: StockOpnameRepository,
    private val productRepository: ProductRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(StockOpnameUiState())
    val uiState: StateFlow<StockOpnameUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
        loadStockOpnames()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            productRepository.getAllProducts().collect { products ->
                _uiState.update {
                    it.copy(
                        products = products,
                        isLoading = false,
                        selectedProducts = products.associate { it.id to it.stock }
                    )
                }
            }
        }
    }

    private fun loadStockOpnames() {
        viewModelScope.launch {
            stockOpnameRepository.getAllStockOpnames().collect { opnames ->
                _uiState.update { it.copy(stockOpnames = opnames) }
            }
        }
    }

    fun onPhysicalStockChanged(productId: Long, stock: Int) {
        _uiState.update { state ->
            state.copy(selectedProducts = state.selectedProducts + (productId to stock))
        }
    }

    fun showCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = true) }
    }

    fun hideCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = false) }
    }

    fun toggleHistory() {
        _uiState.update { it.copy(showHistory = !it.showHistory) }
    }

    fun createStockOpname(notes: String?) {
        viewModelScope.launch {
            val userId = sessionManager.currentUserId.first()
            if (userId == null) {
                _uiState.update { it.copy(error = "User tidak ditemukan") }
                return@launch
            }

            val items = _uiState.value.selectedProducts.map { (productId, physicalStock) ->
                Pair(productId, physicalStock)
            }

            stockOpnameRepository.createStockOpname(userId, notes, items)
                .onSuccess { opname ->
                    _uiState.update {
                        it.copy(
                            showCreateDialog = false,
                            lastOpname = opname
                        )
                    }
                    loadProducts() // Refresh products with updated stock
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
