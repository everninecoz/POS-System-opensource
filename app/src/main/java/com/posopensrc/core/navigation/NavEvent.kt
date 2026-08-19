package com.posopensrc.core.navigation

sealed class NavEvent {
    data object Logout : NavEvent()
    data object NavigateToHome : NavEvent()
}
