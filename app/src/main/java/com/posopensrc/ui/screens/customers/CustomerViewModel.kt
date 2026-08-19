package com.posopensrc.ui.screens.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.posopensrc.data.repository.CustomerRepository
import com.posopensrc.domain.model.Customer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CustomerUiState(
    val customers: List<Customer> = emptyList(),
    val filteredCustomers: List<Customer> = emptyList(),
    val searchQuery: String = "",
    val selectedCustomer: Customer? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddEditDialog: Boolean = false,
    val editingCustomer: Customer? = null
)

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerUiState())
    val uiState: StateFlow<CustomerUiState> = _uiState.asStateFlow()

    init {
        loadCustomers()
    }

    private fun loadCustomers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            customerRepository.getAllCustomers().collect { customers ->
                _uiState.update {
                    it.copy(
                        customers = customers,
                        filteredCustomers = customers,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        filterCustomers(query)
    }

    private fun filterCustomers(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _uiState.update { it.copy(filteredCustomers = it.customers) }
            } else {
                customerRepository.searchCustomers(query).collect { customers ->
                    _uiState.update { it.copy(filteredCustomers = customers) }
                }
            }
        }
    }

    fun onCustomerSelected(customer: Customer) {
        _uiState.update { it.copy(selectedCustomer = customer) }
    }

    fun showAddEditDialog(customer: Customer? = null) {
        _uiState.update {
            it.copy(
                showAddEditDialog = true,
                editingCustomer = customer
            )
        }
    }

    fun hideAddEditDialog() {
        _uiState.update {
            it.copy(
                showAddEditDialog = false,
                editingCustomer = null
            )
        }
    }

    fun createCustomer(
        name: String,
        phone: String?,
        email: String?,
        address: String?,
        notes: String?
    ) {
        viewModelScope.launch {
            customerRepository.createCustomer(name, phone, email, address, notes)
                .onSuccess {
                    hideAddEditDialog()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun updateCustomer(customer: Customer) {
        viewModelScope.launch {
            customerRepository.updateCustomer(customer)
                .onSuccess {
                    hideAddEditDialog()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun deleteCustomer(customerId: Long) {
        viewModelScope.launch {
            customerRepository.deleteCustomer(customerId)
                .onSuccess {
                    _uiState.update { it.copy(selectedCustomer = null) }
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
