package com.neirno.restaurantas.domain.use_case.order

import com.neirno.restaurantas.core.util.Response
import com.neirno.restaurantas.domain.repository.OrdersRepository
import com.neirno.restaurantas.core.constans.OrderStatus
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException
import javax.inject.Inject

class SetNextOrderStatusUseCase @Inject constructor(
    private val ordersRepository: OrdersRepository
) {
    suspend operator fun invoke(orderId: String, newStatus: OrderStatus) = flow {
        try {
            emit(Response.Loading)
            emit(Response.Success(ordersRepository.setNextOrderStatusUseCase(orderId, newStatus).await()))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Response.Error(e.localizedMessage ?: "An unexpected error occurred."))
        }
    }
}
