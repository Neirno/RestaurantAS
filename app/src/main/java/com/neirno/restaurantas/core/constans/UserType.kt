package com.neirno.restaurantas.core.constans

enum class UserType(val type: String) {
    COOK("cook"),
    ADMIN("admin"),
    WAITER("waiter"),
    HOSTESS("hostess"),
    UNKNOWN("unknown");

    companion object {
        fun fromString(role: String): UserType {
            return values().firstOrNull { it.type == role } ?: UNKNOWN
        }
    }

    fun getDisplayName(): String {
        return when (this) {
            COOK -> "Повар"
            ADMIN -> "Администратор"
            WAITER -> "Официант"
            HOSTESS -> "Хостесс"
            UNKNOWN -> "Неизвестная роль"
        }
    }
}
