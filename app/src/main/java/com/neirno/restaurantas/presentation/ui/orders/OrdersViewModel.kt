package com.neirno.restaurantas.presentation.ui.orders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.neirno.restaurantas.core.constans.OrderStatus
import com.neirno.restaurantas.core.constans.UserType
import com.neirno.restaurantas.core.util.Response
import com.neirno.restaurantas.core.util.UiStatus
import com.neirno.restaurantas.core.extension.getOrThrow
import com.neirno.restaurantas.domain.model.OrderModel
import com.neirno.restaurantas.domain.use_case.order.GetOrdersUseCase
import com.neirno.restaurantas.domain.use_case.table.FreeTableUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val getOrdersUseCase: GetOrdersUseCase,
    private val freeTableUseCase: FreeTableUseCase,
    savedStateHandle: SavedStateHandle
): ViewModel(), ContainerHost<OrdersState, OrdersSideEffect> {

    override val container: Container<OrdersState, OrdersSideEffect> = container(OrdersState())

    init {
        val tableId: String = savedStateHandle.getOrThrow("tableId")
        val userType: String = savedStateHandle.getOrThrow("userType")
        val tableNumber: String = savedStateHandle.getOrThrow("tableNumber")
        initializeAndLoadOrders(tableId, userType, tableNumber)
    }

    private fun initializeAndLoadOrders(tableId: String, userType: String, tableNumber: String) = intent {
        reduce { state.copy(tableId = tableId, userType = UserType.fromString(userType), tableNumber = tableNumber) }
        loadOrders()
    }

    fun onEvent(event: OrdersEvent) {
        when (event) {
            OrdersEvent.LoadOrders -> {
                loadOrders()
            }
            is OrdersEvent.OpenOrder -> {
                openOrder(event.orderId)
            }
            OrdersEvent.FinishWorkWithTable -> {
                finishWorkWithTable()
            }
        }
    }

    private fun finishWorkWithTable() = intent {
        // Проверяем, есть ли незавершённые заказы
        val hasUnfinishedOrders = state.orders.any {
            it.status != OrderStatus.COMPLETED.status && it.status != OrderStatus.CANCELLED.status
        }

        if (hasUnfinishedOrders) {
            // Если есть незавершённые заказы, отправляем сайд-эффект с ошибкой
            postSideEffect(OrdersSideEffect.ShowError("Есть незавершённые заказы на столе"))
            return@intent
        } else {
            freeTableUseCase(state.tableId).collect{ response ->
                when (response) {
                    is Response.Success -> {
                        postSideEffect(OrdersSideEffect.GoBack)
                    }
                    is Response.Error -> {
                        postSideEffect(OrdersSideEffect.ShowError(response.msg))
                    }
                    else -> {}
                }
            }
        }
    }

    private fun openOrder(orderId: String) = intent {
        postSideEffect(OrdersSideEffect.GoToOrder(orderId, state.userType.type, state.tableId, state.tableNumber))
    }

    private fun loadOrders() = intent {
        getOrdersUseCase().collect { response ->
            when (response) {
                is Response.Error -> {
                    postSideEffect(OrdersSideEffect.LoadingError(response.msg))
                }
                Response.Loading -> {
                    reduce { state.copy(status = UiStatus.Loading) }
                }
                is Response.Success -> {
                    val orders = response.data.filter { it.tableId == state.tableId &&
                        it.status != OrderStatus.COMPLETED.status && it.status != OrderStatus.CANCELLED.status }
                    reduce { state.copy(orders = orders, status = UiStatus.Success)}
                }
            }
        }
    }
}

sealed class OrdersEvent {
    object LoadOrders: OrdersEvent()
    data class OpenOrder(val orderId: String): OrdersEvent()
    object FinishWorkWithTable: OrdersEvent()
}

data class OrdersState(
    val status: UiStatus = UiStatus.Loading,
    val orders: List<OrderModel> = emptyList(),
    val tableId: String = "",
    val userType: UserType = UserType.UNKNOWN,
    val tableNumber: String = "",
)

sealed class OrdersSideEffect {
    data class GoToOrder(val orderId: String, val userType: String, val tableId: String, val tableNumber: String): OrdersSideEffect()
    data class ShowError(val err: String): OrdersSideEffect()
    data class LoadingError(val err: String): OrdersSideEffect()
    object GoBack: OrdersSideEffect()
}