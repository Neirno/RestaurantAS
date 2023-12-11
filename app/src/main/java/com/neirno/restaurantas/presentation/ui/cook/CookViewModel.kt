package com.neirno.restaurantas.presentation.ui.cook

import android.util.Log
import androidx.lifecycle.ViewModel
import com.neirno.restaurantas.core.constans.OrderStatus
import com.neirno.restaurantas.core.util.Response
import com.neirno.restaurantas.core.util.UiStatus
import com.neirno.restaurantas.domain.model.OrderModel
import com.neirno.restaurantas.domain.use_case.order.AcceptOrderUseCase
import com.neirno.restaurantas.domain.use_case.order.GetOrdersUseCase
import com.neirno.restaurantas.domain.use_case.table.GetTableInfoUseCase
import com.neirno.restaurantas.domain.use_case.user.GetUserStatusUseCase
import com.neirno.restaurantas.domain.use_case.user.GetUserTypeUseCase
import com.neirno.restaurantas.domain.use_case.user.GetUserUIdUseCase
import com.neirno.restaurantas.presentation.ui.admin.AdminSideEffect
import com.neirno.restaurantas.presentation.ui.orders.OrdersSideEffect
import com.neirno.restaurantas.presentation.ui.waiter.WaiterEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class CookViewModel @Inject constructor(
    private val getUserStatusUseCase: GetUserStatusUseCase,
    private val getUserUIdUseCase: GetUserUIdUseCase,
    private val acceptOrderUseCase: AcceptOrderUseCase,
    private val getUserTypeUseCase: GetUserTypeUseCase,
    private val getOrdersUseCase: GetOrdersUseCase,
    private val getTableInfoUseCase: GetTableInfoUseCase
    ): ViewModel(), ContainerHost<CookState, CookSideEffect> {

    override val container: Container<CookState, CookSideEffect> = container(CookState())

    init {
        getUserStatus()
    }

    fun onEvent(event: CookEvent) {
        when (event) {
            CookEvent.LoadOrders -> {
                loadOrders()
            }
            is CookEvent.OpenOrder -> {
                openOrder(event.order)
            }
            is CookEvent.ServiceOrder -> {
                serviceOrder(event.order)
            }
            CookEvent.GetUserStatus -> {
                getUserStatus()
            }
            CookEvent.GetUserType -> {
                getUserType()
            }
            CookEvent.GoToSettings -> {
                intent { postSideEffect(CookSideEffect.NavigateToSettings) }
            }
        }
    }

    private fun openOrder(order: OrderModel) = intent {
        getTableInfoUseCase(order.tableId).collect { response ->
            when (response) {
                is Response.Success -> {
                    postSideEffect(CookSideEffect.GoToOrder(
                        orderId = order.orderId,
                        userType = state.userType,
                        tableId = order.tableId,
                        tableNumber = response.data.number
                    ))
                }
                is Response.Error -> {
                    reduce { state.copy(status = UiStatus.Error("Ошибка открытия заказа")) }
                }
                Response.Loading -> {}
            }
        }

    }

    private fun getUserType() = intent {
        getUserTypeUseCase().collect { response ->
            when (response) {
                is Response.Success -> {
                    val userType = response.data
                    reduce { state.copy(status = UiStatus.Success, userType = userType) }
                }
                is Response.Error -> {
                    reduce { state.copy(status = UiStatus.Error("Ошибка отображения типа пользователя")) }
                }
                is Response.Loading -> {}
            }
        }
    }


    private fun serviceOrder(order: OrderModel) = intent {
        acceptOrderUseCase(
            order.orderId,
            OrderStatus.fromString(order.status),
            state.userId
        ).collect { result ->
            when (result) {
                is Response.Success -> {
                    reduce { state.copy(status = UiStatus.Success) }
                }
                is Response.Error -> {
                    reduce { state.copy(status = UiStatus.Error("Ошибка принятия заказа")) }
                }
                is Response.Loading -> {}
            }
        }
    }

    private fun loadOrders() = intent {
        Log.i("Cook VM:", "start loadOrders")
        getOrdersUseCase().collect { response ->
            when (response) {
                is Response.Success -> {
                    reduce {
                        // Разделение и фильтрация заказов на myOrders (те, которые обслуживает текущий пользователь и имеют статус 'accepted' или 'in_progress')
                        val (myOrders, potentiallyFreeOrders) = response.data.partition { order ->
                            order.takenBy == state.userId && (order.status == "accepted" || order.status == "in_progress")
                        }
                        Log.i("Cook VM:", myOrders.toString())

                        // Фильтрация свободных заказов, которые не приняты и не отменены
                        val freeOrders = potentiallyFreeOrders.filter { order ->
                            order.takenBy.isEmpty() && order.status != "accepted" && order.status != "cancelled"
                        }
                        Log.i("Cook VM:", freeOrders.toString())

                        state.copy(
                            status = UiStatus.Success,
                            myOrders = myOrders,
                            freeOrders = freeOrders
                        )
                    }
                    // Получаем статус
                }
                is Response.Error -> {
                    reduce { state.copy(status = UiStatus.Error(response.msg)) }
                }
                is Response.Loading -> {}
            }
        }
    }

    private fun getUserStatus() = intent {
        val userId = getUserUIdUseCase()
        if (userId == null) {
            reduce { state.copy(status = UiStatus.Error("Что-то пошло не так!")) }
            return@intent
        } else {
            reduce { state.copy(userId = userId) }
        }

        getUserStatusUseCase().collect { response ->
            when (response) {
                is Response.Success -> {
                    if (!response.data) {
                        reduce { state.copy(status = UiStatus.Error("Вы не на работе!"), userStatus = false) }
                    } else {
                        reduce { state.copy(userStatus = response.data) }
                        loadOrders()
                        getUserType()
                    }
                }
                is Response.Error -> {
                    reduce { state.copy(status = UiStatus.Error(response.msg)) }
                }
                is Response.Loading -> {
                    reduce { state.copy(status = UiStatus.Loading) }
                }
            }
        }
    }

}

sealed class CookEvent {
    object GoToSettings: CookEvent()
    object LoadOrders: CookEvent()
    object GetUserStatus: CookEvent()
    object GetUserType: CookEvent()
    data class OpenOrder(val order: OrderModel): CookEvent()
    data class ServiceOrder(val order: OrderModel): CookEvent()
}

data class CookState(
    val status: UiStatus = UiStatus.Loading,
    val freeOrders: List<OrderModel> = emptyList(),
    val userStatus: Boolean = false,
    val userType: String = "",
    val userId: String = "",
    val myOrders: List<OrderModel> = emptyList()
)

sealed class CookSideEffect {
    data class GoToOrder(val orderId: String, val userType: String, val tableId: String, val tableNumber: String): CookSideEffect()
    object NavigateToSettings: CookSideEffect()
}