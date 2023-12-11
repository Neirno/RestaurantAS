package com.neirno.restaurantas.domain.model

import com.neirno.restaurantas.core.constans.OrderStatus

data class OrderModel(
    val orderId: String = "",
    val note: String = "",
    val status: String = OrderStatus.UNKNOWN.status,
    val tableId: String = "",
    val takenBy: String = ""
)
