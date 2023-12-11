package com.neirno.restaurantas.core.util

sealed class UiStatus {
    object Loading : UiStatus()
    object Success : UiStatus()
    data class Error(val message: String = "") : UiStatus()
}