package com.neirno.restaurantas.core.constans

import androidx.compose.ui.graphics.Color

enum class OrderStatus(val status: String) {
    PENDING("pending"), // Ожидание: официант только что создал заказ
    ACCEPTED("accepted"), // Принят: повар принял заказ в обработку
    IN_PROGRESS("in_progress"), // В процессе: повар активно работает над заказом
    READY("ready"), // Готов: заказ готов к подаче
    DELIVERED("delivered"), // Выдан: официант подал заказ клиентам
    COMPLETED("completed"), // Завершен: клиент оплатил заказ, и он закрыт в системе
    CANCELLED("cancelled"), // Отменен: заказ был отменен до его приготовления или выдачи
    UNKNOWN("unknown");

    companion object {
        fun fromString(status: String): OrderStatus {
            return values().firstOrNull { it.status == status } ?: UNKNOWN
        }
    }
    fun getDisplayName(): String {
        return when (this) {
            PENDING -> "Ожидание"
            ACCEPTED -> "Принят"
            IN_PROGRESS -> "В процессе"
            READY -> "Готов"
            DELIVERED -> "Выдан"
            COMPLETED -> "Завершен"
            CANCELLED -> "Отменен"
            UNKNOWN -> "Неизвестно"
        }
    }

    fun getOrderStatusColor(): Color {
        return when (this) {
            PENDING -> Color.Yellow
            ACCEPTED -> Color.Blue
            IN_PROGRESS -> Color.Cyan
            READY -> Color.Green
            DELIVERED -> Color.Magenta
            COMPLETED -> Color.Gray
            CANCELLED -> Color.Red
            UNKNOWN -> Color.Black
        }
    }
}

// Описание действий в зависимости от статуса:
// PENDING: Официанты могут создавать заказы. Это начальное состояние каждого нового заказа.
// ACCEPTED: Повара могут принимать заказы и начинать их обработку. Официанты уже не могут изменять заказ.
// IN_PROGRESS: Повара могут обновлять статус заказа на "В процессе", что означает, что заказ в стадии активной подготовки.
// READY: Повара обновляют статус на "Готов", когда заказ может быть подан. Официанты могут видеть, что заказ готов к подаче.
// DELIVERED: Официанты могут обновлять статус заказа на "Выдан" после того, как заказ был подан клиентам.
// COMPLETED: Администраторы или официанты могут обновлять статус на "Завершен" после оплаты заказа.
// CANCELLED: Официанты могут отменять заказы до их принятия поваром, а администраторы могут отменять заказы в любое время.
