package com.neirno.restaurantas.core.extension

import androidx.lifecycle.SavedStateHandle

inline fun <reified T> SavedStateHandle.getOrThrow(key: String): T {
    return get<T>(key) ?: throw IllegalStateException("$key not found in SavedStateHandle")
}