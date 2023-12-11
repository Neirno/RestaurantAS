package com.neirno.restaurantas.domain.use_case.order

import com.neirno.restaurantas.core.constans.OrderStatus
import com.neirno.restaurantas.core.util.Response
import com.neirno.restaurantas.domain.model.OrderModel
import com.neirno.restaurantas.domain.repository.OrdersRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import java.util.concurrent.CancellationException
import javax.inject.Inject

class GetOrdersUseCase @Inject constructor(
    private val ordersRepository: OrdersRepository
) {
    operator fun invoke() = flow {
        try {
            emit(Response.Loading)
            ordersRepository.getOrdersFlow()
                .catch {
                    //if (it is CancellationException) throw it
                    emit(Response.Error("Неожиданная ошибка: ${it.localizedMessage}"))
                }
                .collect { order ->
                    if (order == null) {
                        emit(Response.Error("Ошибка вывода заказов"))
                    } else {
                        emit(Response.Success(order))
                    }
                }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Response.Error("Неожиданная ошибка: ${e.localizedMessage}"))
        }
    }
}
