package com.posopensrc.core.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.posopensrc.core.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val sessionManager: SessionManager
) : ViewModel() {

    private val _navEvent = MutableSharedFlow<NavEvent>()
    val navEvent: SharedFlow<NavEvent> = _navEvent.asSharedFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _language = MutableStateFlow("id")
    val language: StateFlow<String> = _language.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.isDarkMode.collect { isDark ->
                _isDarkMode.value = isDark
            }
        }
        viewModelScope.launch {
            sessionManager.language.collect { lang ->
                _language.value = lang
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.clearSession()
            _navEvent.emit(NavEvent.Logout)
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            sessionManager.setDarkMode(!_isDarkMode.value)
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            sessionManager.setLanguage(lang)
        }
    }
}
