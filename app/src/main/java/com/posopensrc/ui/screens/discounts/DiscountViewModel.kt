package com.posopensrc.ui.screens.discounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.posopensrc.data.repository.DiscountRepository
import com.posopensrc.domain.model.Discount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiscountUiState(
    val discounts: List<Discount> = emptyList(),
    val filteredDiscounts: List<Discount> = emptyList(),
    val searchQuery: String = "",
    val selectedDiscount: Discount? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddEditDialog: Boolean = false,
    val editingDiscount: Discount? = null
)

@HiltViewModel
class DiscountViewModel @Inject constructor(
    private val discountRepository: DiscountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscountUiState())
    val uiState: StateFlow<DiscountUiState> = _uiState.asStateFlow()

    init {
        loadDiscounts()
    }

    private fun loadDiscounts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            discountRepository.getAllDiscounts().collect { discounts ->
                _uiState.update {
                    it.copy(
                        discounts = discounts,
                        filteredDiscounts = discounts,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        filterDiscounts(query)
    }

    private fun filterDiscounts(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _uiState.update { it.copy(filteredDiscounts = it.discounts) }
            } else {
                val filtered = _uiState.value.discounts.filter {
                    it.name.contains(query, ignoreCase = true) ||
                            (it.code?.contains(query, ignoreCase = true) == true)
                }
                _uiState.update { it.copy(filteredDiscounts = filtered) }
            }
        }
    }

    fun onDiscountSelected(discount: Discount) {
        _uiState.update { it.copy(selectedDiscount = discount) }
    }

    fun showAddEditDialog(discount: Discount? = null) {
        _uiState.update {
            it.copy(
                showAddEditDialog = true,
                editingDiscount = discount
            )
        }
    }

    fun hideAddEditDialog() {
        _uiState.update {
            it.copy(
                showAddEditDialog = false,
                editingDiscount = null
            )
        }
    }

    fun createDiscount(
        name: String,
        code: String?,
        description: String?,
        discountType: String,
        discountValue: Double,
        minPurchase: Double,
        maxDiscount: Double?,
        buyQuantity: Int?,
        getQuantity: Int?,
        usageLimit: Int?,
        validFrom: Long,
        validUntil: Long
    ) {
        viewModelScope.launch {
            discountRepository.createDiscount(
                name = name,
                code = code,
                description = description,
                discountType = discountType,
                discountValue = discountValue,
                minPurchase = minPurchase,
                maxDiscount = maxDiscount,
                buyQuantity = buyQuantity,
                getQuantity = getQuantity,
                productId = null,
                categoryId = null,
                usageLimit = usageLimit,
                validFrom = validFrom,
                validUntil = validUntil
            ).onSuccess {
                hideAddEditDialog()
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun updateDiscount(discount: Discount) {
        viewModelScope.launch {
            discountRepository.updateDiscount(discount)
                .onSuccess {
                    hideAddEditDialog()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun deleteDiscount(discountId: Long) {
        viewModelScope.launch {
            discountRepository.deleteDiscount(discountId)
                .onSuccess {
                    _uiState.update { it.copy(selectedDiscount = null) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun toggleDiscountStatus(discount: Discount) {
        viewModelScope.launch {
            discountRepository.updateDiscount(discount.copy(isActive = !discount.isActive))
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
