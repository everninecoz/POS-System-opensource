package com.posopensrc.ui.screens.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.posopensrc.data.repository.ProductRepository
import com.posopensrc.domain.model.Category
import com.posopensrc.domain.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
        loadCategories()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            productRepository.getAllProducts().collect { products ->
                _uiState.value = _uiState.value.copy(
                    products = products,
                    filteredProducts = filterProducts(products, _uiState.value.selectedCategory, _uiState.value.searchQuery)
                )
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            productRepository.getAllCategories().collect { categories ->
                _uiState.value = _uiState.value.copy(
                    categories = categories.map { it.name }
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredProducts = filterProducts(_uiState.value.products, _uiState.value.selectedCategory, query)
        )
    }

    fun onCategorySelected(category: String?) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            filteredProducts = filterProducts(_uiState.value.products, category, _uiState.value.searchQuery)
        )
    }

    private fun filterProducts(
        products: List<Product>,
        category: String?,
        query: String
    ): List<Product> {
        return products.filter { product ->
            val matchesCategory = category == null || product.category == category
            val matchesQuery = query.isBlank() ||
                    product.name.contains(query, ignoreCase = true) ||
                    product.barcode?.contains(query, ignoreCase = true) == true
            matchesCategory && matchesQuery
        }
    }

    fun createProduct(
        name: String,
        price: Double,
        costPrice: Double,
        stock: Int,
        minStock: Int,
        category: String?,
        barcode: String?
    ) {
        viewModelScope.launch {
            productRepository.createProduct(
                name = name,
                price = price,
                costPrice = costPrice,
                stock = stock,
                minStock = minStock,
                category = category,
                barcode = barcode,
                imagePath = null
            )
        }
    }

    fun updateProduct(
        id: Long,
        name: String,
        price: Double,
        costPrice: Double,
        stock: Int,
        minStock: Int,
        category: String?,
        barcode: String?
    ) {
        viewModelScope.launch {
            productRepository.updateProduct(
                id = id,
                name = name,
                price = price,
                costPrice = costPrice,
                stock = stock,
                minStock = minStock,
                category = category,
                barcode = barcode,
                imagePath = null
            )
        }
    }

    fun deleteProduct(id: Long) {
        viewModelScope.launch {
            productRepository.deleteProduct(id)
        }
    }
}

data class ProductUiState(
    val products: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val searchQuery: String = ""
)
