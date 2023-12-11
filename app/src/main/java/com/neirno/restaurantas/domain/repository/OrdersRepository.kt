package com.neirno.restaurantas.domain.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.neirno.restaurantas.core.constans.OrderStatus
import com.neirno.restaurantas.domain.model.OrderModel
import kotlinx.coroutines.flow.Flow

interface OrdersRepository {
    suspend fun getOrdersFlow(): Flow<List<OrderModel>?>

    suspend fun createOrder(order: OrderModel): Task<DocumentReference?>

    suspend fun acceptOrderUseCase(orderId: String, newStatus: OrderStatus, takenBy: String): Task<Void>

    suspend fun cancelOrder(orderId: String): Task<Void>

    suspend fun setNextOrderStatusUseCase(orderId: String, newStatus: OrderStatus): Task<Void>

    suspend fun getOrderById(orderId: String): Flow<OrderModel?>
}