package com.posopensrc.ui.screens.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.posopensrc.data.local.dao.UserDao
import com.posopensrc.data.local.entity.UserEntity
import com.posopensrc.core.security.PasswordUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserManagementUiState(
    val users: List<UserEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddEditDialog: Boolean = false,
    val editingUser: UserEntity? = null,
    val successMessage: String? = null
)

@HiltViewModel
class UserManagementViewModel @Inject constructor(
    private val userDao: UserDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserManagementUiState())
    val uiState: StateFlow<UserManagementUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            userDao.getAllUsers().collect { users ->
                _uiState.update {
                    it.copy(
                        users = users,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun showAddEditDialog(user: UserEntity? = null) {
        _uiState.update {
            it.copy(
                showAddEditDialog = true,
                editingUser = user
            )
        }
    }

    fun hideAddEditDialog() {
        _uiState.update {
            it.copy(
                showAddEditDialog = false,
                editingUser = null
            )
        }
    }

    fun createUser(
        username: String,
        password: String,
        fullName: String,
        role: String,
        pin: String
    ) {
        viewModelScope.launch {
            // Check if username already exists
            val existingUser = userDao.getUserByUsername(username)
            if (existingUser != null) {
                _uiState.update { it.copy(error = "Username sudah digunakan") }
                return@launch
            }

            val pinHash = PasswordUtils.hashPin(pin)
            val passwordHash = PasswordUtils.hashPassword(password)

            val user = UserEntity(
                username = username,
                pinHash = pinHash,
                passwordHash = passwordHash,
                fullName = fullName,
                role = role
            )

            userDao.insertUser(user)
            _uiState.update {
                it.copy(
                    showAddEditDialog = false,
                    editingUser = null,
                    successMessage = "User berhasil ditambahkan"
                )
            }
        }
    }

    fun updateUser(
        userId: Long,
        username: String,
        fullName: String,
        role: String,
        newPassword: String? = null,
        newPin: String? = null
    ) {
        viewModelScope.launch {
            val existingUser = userDao.getUserById(userId) ?: return@launch

            val updatedUser = existingUser.copy(
                username = username,
                fullName = fullName,
                role = role,
                passwordHash = if (newPassword != null) PasswordUtils.hashPassword(newPassword) else existingUser.passwordHash,
                pinHash = if (newPin != null) PasswordUtils.hashPin(newPin) else existingUser.pinHash,
                updatedAt = System.currentTimeMillis()
            )

            userDao.updateUser(updatedUser)
            _uiState.update {
                it.copy(
                    showAddEditDialog = false,
                    editingUser = null,
                    successMessage = "User berhasil diupdate"
                )
            }
        }
    }

    fun deleteUser(userId: Long) {
        viewModelScope.launch {
            userDao.deleteUser(userId)
            _uiState.update { it.copy(successMessage = "User berhasil dihapus") }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
}
