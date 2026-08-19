package com.posopensrc.ui.screens.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.posopensrc.core.session.SessionManager
import com.posopensrc.data.repository.ProductRepository
import com.posopensrc.data.repository.SettingsRepository
import com.posopensrc.data.repository.TransactionRepository
import com.posopensrc.domain.model.CartItem
import com.posopensrc.domain.model.PaymentMethod
import com.posopensrc.domain.model.Product
import com.posopensrc.domain.model.Transaction
import com.posopensrc.domain.model.TransactionItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PosViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val transactionRepository: TransactionRepository,
    private val settingsRepository: SettingsRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PosUiState())
    val uiState: StateFlow<PosUiState> = _uiState.asStateFlow()

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    private val _showPaymentDialog = MutableStateFlow(false)
    val showPaymentDialog: StateFlow<Boolean> = _showPaymentDialog.asStateFlow()

    private val _showReceiptDialog = MutableStateFlow(false)
    val showReceiptDialog: StateFlow<Boolean> = _showReceiptDialog.asStateFlow()

    private val _lastTransaction = MutableStateFlow<Transaction?>(null)
    val lastTransaction: StateFlow<Transaction?> = _lastTransaction.asStateFlow()

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            productRepository.getAllProducts().collect { products ->
                _uiState.value = _uiState.value.copy(products = products)
            }
        }

        viewModelScope.launch {
            productRepository.getAllCategories().collect { categories ->
                _uiState.value = _uiState.value.copy(categories = categories.map { it.name })
            }
        }

        viewModelScope.launch {
            settingsRepository.getSettings().collect { settings ->
                _uiState.value = _uiState.value.copy(taxPercentage = settings.taxPercentage)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        viewModelScope.launch {
            if (query.isBlank()) {
                productRepository.getAllProducts().collect { products ->
                    _uiState.value = _uiState.value.copy(filteredProducts = products)
                }
            } else {
                productRepository.searchProducts(query).collect { products ->
                    _uiState.value = _uiState.value.copy(filteredProducts = products)
                }
            }
        }
    }

    fun onCategorySelected(category: String?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        viewModelScope.launch {
            if (category == null) {
                productRepository.getAllProducts().collect { products ->
                    _uiState.value = _uiState.value.copy(filteredProducts = products)
                }
            } else {
                productRepository.getProductsByCategory(category).collect { products ->
                    _uiState.value = _uiState.value.copy(filteredProducts = products)
                }
            }
        }
    }

    fun addToCart(product: Product) {
        val currentCart = _cart.value.toMutableList()
        val existingIndex = currentCart.indexOfFirst { it.product.id == product.id }

        if (existingIndex >= 0) {
            val existing = currentCart[existingIndex]
            currentCart[existingIndex] = existing.copy(quantity = existing.quantity + 1)
        } else {
            currentCart.add(CartItem(product = product, quantity = 1))
        }
        _cart.value = currentCart
    }

    fun removeFromCart(productId: Long) {
        val currentCart = _cart.value.toMutableList()
        currentCart.removeAll { it.product.id == productId }
        _cart.value = currentCart
    }

    fun updateQuantity(productId: Long, delta: Int) {
        val currentCart = _cart.value.toMutableList()
        val index = currentCart.indexOfFirst { it.product.id == productId }

        if (index >= 0) {
            val item = currentCart[index]
            val newQuantity = item.quantity + delta
            if (newQuantity <= 0) {
                currentCart.removeAt(index)
            } else {
                currentCart[index] = item.copy(quantity = newQuantity)
            }
            _cart.value = currentCart
        }
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    fun showPaymentDialog() {
        if (_cart.value.isNotEmpty()) {
            _showPaymentDialog.value = true
        }
    }

    fun hidePaymentDialog() {
        _showPaymentDialog.value = false
    }

    fun getSubtotal(): Double {
        return _cart.value.sumOf { it.subtotal }
    }

    fun getTaxAmount(): Double {
        return getSubtotal() * (_uiState.value.taxPercentage / 100)
    }

    fun getTotal(): Double {
        return getSubtotal() + getTaxAmount()
    }

    fun processPayment(
        paymentMethod: PaymentMethod,
        amountPaid: Double
    ) {
        viewModelScope.launch {
            val userId = sessionManager.userId.first()
            val cartItems = _cart.value

            val transactionItems = cartItems.map { cartItem ->
                TransactionItem(
                    productId = cartItem.product.id,
                    productName = cartItem.product.name,
                    quantity = cartItem.quantity,
                    price = cartItem.product.price,
                    subtotal = cartItem.subtotal
                )
            }

            val result = transactionRepository.createTransaction(
                userId = userId,
                customerName = null,
                items = transactionItems,
                subtotal = getSubtotal(),
                taxPercentage = _uiState.value.taxPercentage,
                discount = 0.0,
                paymentMethod = paymentMethod,
                amountPaid = amountPaid,
                notes = null
            )

            result.fold(
                onSuccess = { transaction ->
                    // Reduce stock for each item
                    cartItems.forEach { cartItem ->
                        productRepository.reduceStock(cartItem.product.id, cartItem.quantity)
                    }

                    _lastTransaction.value = transaction
                    _showPaymentDialog.value = false
                    _showReceiptDialog.value = true
                    _cart.value = emptyList()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
            )
        }
    }

    fun hideReceiptDialog() {
        _showReceiptDialog.value = false
        _lastTransaction.value = null
    }
}

data class PosUiState(
    val products: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val categories: List<String> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val taxPercentage: Double = 10.0,
    val error: String? = null
)
