package com.neirno.restaurantas.domain.use_case.order

import com.neirno.restaurantas.core.util.Response
import com.neirno.restaurantas.domain.model.OrderModel
import com.neirno.restaurantas.domain.repository.OrdersRepository
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException
import javax.inject.Inject

class CreateOrderUseCase @Inject constructor(
    private val ordersRepository: OrdersRepository
) {
    suspend operator fun invoke(order: OrderModel) = flow {
        try {
            emit(Response.Loading)
            val response = ordersRepository.createOrder(order).await()

            if (response == null)
                emit(Response.Error("Заказ не создан."))
            else
                emit(Response.Success(response))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Response.Error(e.localizedMessage ?: "An unexpected error occurred."))
        }
    }
}
