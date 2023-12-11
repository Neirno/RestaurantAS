package com.neirno.restaurantas.presentation.ui.order_details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.neirno.restaurantas.core.constans.OrderStatus
import com.neirno.restaurantas.core.constans.UserType
import com.neirno.restaurantas.core.util.Response
import com.neirno.restaurantas.core.extension.getOrThrow
import com.neirno.restaurantas.domain.model.OrderModel
import com.neirno.restaurantas.domain.use_case.order.AcceptOrderUseCase
import com.neirno.restaurantas.domain.use_case.order.CancelOrderUseCase
import com.neirno.restaurantas.domain.use_case.order.CreateOrderUseCase
import com.neirno.restaurantas.domain.use_case.order.GetOrderByIdUseCase
import com.neirno.restaurantas.domain.use_case.order.SetNextOrderStatusUseCase
import com.neirno.restaurantas.domain.use_case.table.FreeTableUseCase
import com.neirno.restaurantas.domain.use_case.user.GetUserUIdUseCase
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
class OrderDetailsViewModel @Inject constructor(
    private val getOrderByIdUseCase: GetOrderByIdUseCase,
    private val createOrderUseCase: CreateOrderUseCase,
    private val acceptOrderUseCase: AcceptOrderUseCase,
    private val setNextOrderStatus: SetNextOrderStatusUseCase,
    private val cancelOrderUseCase: CancelOrderUseCase,
    private val freeTableUseCase: FreeTableUseCase,
    private val getUserUIdUseCase: GetUserUIdUseCase,
    savedStateHandle: SavedStateHandle
): ViewModel(), ContainerHost<OrderDetailsState, OrderDetailsSideEffect> {

    override val container: Container<OrderDetailsState, OrderDetailsSideEffect> = container(OrderDetailsState())

    init {
        val orderId: String = savedStateHandle.getOrThrow("orderId")
        val tableNumber: String = savedStateHandle.getOrThrow("tableNumber")
        val tableId: String = savedStateHandle.getOrThrow("tableId")
        val userType: UserType = UserType.fromString(savedStateHandle.getOrThrow("userType"))
        initializeAndLoadOrder(orderId, userType, tableNumber, tableId)
        loadOrder()
    }

    private fun initializeAndLoadOrder(
        orderId: String,
        userType: UserType,
        tableNumber: String,
        tableId: String
    ) = intent {
        reduce { state.copy(orderId = orderId, userType = userType, tableNumber = tableNumber, tableId = tableId,
            order = state.order.copy(tableId = tableId)
        ) }
    }

    fun onEvent(event: OrderDetailsEvent) {
        when (event) {
            OrderDetailsEvent.LoadOrderDetails -> {
                loadOrder()
            }
            OrderDetailsEvent.CancelOrderDetails -> {
                cancelOrder()
            }
            is OrderDetailsEvent.ChangeOrderStatusDetails -> {
                changeOrderStatus()
            }
            is OrderDetailsEvent.EnterNote -> {
                intent { reduce { state.copy(order = state.order.copy(note = event.data)) } }
            }
        }
    }

    private fun changeOrderStatus() = intent {
        val currentStatus = state.order.status
        if (currentStatus == OrderStatus.UNKNOWN.status && state.userType == UserType.WAITER) {
            // Создать новый заказ со статусом PENDING
            if (state.order.note == "") {
                postSideEffect(OrderDetailsSideEffect.OrderDetailsChangeStatusError("Вы не ввели заказ!"))
                return@intent
            }

            val nextStatus = moveToNextOrderStatus(currentStatus)
            reduce { state.copy(order = state.order.copy(status = nextStatus.status)) }
            createOrderUseCase(state.order).collect { response ->
                when (response) {
                    is Response.Success -> {
                        val orderId = response.data.id
                        reduce { state.copy(orderId = orderId, order = state.order.copy(orderId = orderId)) }
                        loadOrder() // Заново получаем значение
                    }
                    is Response.Error -> {}
                    Response.Loading -> {}
                }
            }
        } else if (currentStatus == OrderStatus.PENDING.status && state.userType == UserType.COOK) {
            val userId = getUserUIdUseCase()

            if (userId == null) {
                // Обработка случая, когда userId не найден
                postSideEffect(OrderDetailsSideEffect.OrderDetailsChangeStatusError("Пользователь не авторезирован!"))
                return@intent
            }
            val nextStatus = moveToNextOrderStatus(currentStatus)

            acceptOrderUseCase(state.order.orderId, nextStatus, userId).collect()
        } else if (currentStatus == OrderStatus.DELIVERED.status && state.userType == UserType.WAITER) {
            val nextStatus = moveToNextOrderStatus(currentStatus)
            setNextOrderStatus(state.order.orderId, nextStatus).collect()
            freeTableUseCase(state.orderId).collect()

            postSideEffect(OrderDetailsSideEffect.GoBack)
        } else if (currentStatus == OrderStatus.IN_PROGRESS.status && state.userType == UserType.COOK) {
            val nextStatus = moveToNextOrderStatus(currentStatus)
            setNextOrderStatus(state.order.orderId, nextStatus).collect()

            postSideEffect(OrderDetailsSideEffect.GoBack)
        } else {
            // Изменить статус существующего заказа
            val nextStatus = moveToNextOrderStatus(currentStatus)
            setNextOrderStatus(state.order.orderId, nextStatus).collect()
        }
    }

    private fun cancelOrder() = intent {
        if (state.userType == UserType.ADMIN ||
            (state.userType == UserType.WAITER && state.order.status == OrderStatus.PENDING.status)) {
            cancelOrderUseCase(state.order.orderId).collect()
            freeTableUseCase(state.orderId).collect()
            postSideEffect(OrderDetailsSideEffect.GoBack)
        } else {
            postSideEffect(OrderDetailsSideEffect.OrderDetailsCancellationError("Отмена заказа невозможна."))
        }
    }


    private fun loadOrder() = intent {
        getOrderByIdUseCase(state.orderId).collect { response ->
            when (response) {
                is Response.Success -> handleSuccess(response.data, state.userType)
                is Response.Error -> handleError(response.msg)
                is Response.Loading -> {}
            }
        }
    }

    private fun handleSuccess(order: OrderModel, userType: UserType) = intent {
        //Log.i("Order VM", "Status in handleSuccess: ${order.status}")

        val canEditNote = userType in listOf(UserType.WAITER, UserType.ADMIN) &&
                order.status == OrderStatus.UNKNOWN.status

        reduce { state.copy(order = order, canEditNote = canEditNote) }

        when (userType) {
            UserType.WAITER -> {
                val (setNextOrderStatus, setCancelOrder) = order.permissionsForWaiter()
                reduce { state.copy(setNextOrderStatus = setNextOrderStatus, setCancelOrder = setCancelOrder) }
            }
            UserType.COOK -> {
                val setNextOrderStatus = order.permissionsForCook()
                reduce { state.copy(setNextOrderStatus = setNextOrderStatus) }
            }
            UserType.ADMIN -> {
                val (setNextOrderStatus, setCancelOrder) = order.permissionsForAdmin()
                reduce { state.copy(setNextOrderStatus = setNextOrderStatus, setCancelOrder = setCancelOrder) }
            }
            else -> postSideEffect(OrderDetailsSideEffect.OrderDetailsLoadingError("У Вас нет доступа к заказу."))
        }
    }

    private fun handleError(errorMessage: String) = intent {
        if (state.userType == UserType.WAITER)
            reduce { state.copy(order = state.order.copy(status = OrderStatus.UNKNOWN.status), setNextOrderStatus = true) }
        else
            // Обработка ошибки для других ролей
            postSideEffect(OrderDetailsSideEffect.OrderDetailsLoadingError(errorMessage))
    }



    private fun moveToNextOrderStatus(status: String): OrderStatus {
        val _status = OrderStatus.fromString(status)
        return when (_status) {
            OrderStatus.UNKNOWN -> OrderStatus.PENDING
            OrderStatus.PENDING -> OrderStatus.ACCEPTED
            OrderStatus.ACCEPTED -> OrderStatus.IN_PROGRESS
            OrderStatus.IN_PROGRESS -> OrderStatus.READY
            OrderStatus.READY -> OrderStatus.DELIVERED
            OrderStatus.DELIVERED -> OrderStatus.COMPLETED
            OrderStatus.COMPLETED -> OrderStatus.COMPLETED
            else -> OrderStatus.CANCELLED
        }
    }
}


fun OrderModel.permissionsForWaiter(): Pair<Boolean, Boolean> = when (OrderStatus.fromString(this.status)) {
    OrderStatus.UNKNOWN,
    OrderStatus.READY, OrderStatus.DELIVERED,
    OrderStatus.COMPLETED -> Pair(true, false)
    OrderStatus.PENDING -> Pair(false, true)
    else -> Pair(false, false)
}

fun OrderModel.permissionsForCook(): Boolean = when (OrderStatus.fromString(this.status)) {
    OrderStatus.ACCEPTED, OrderStatus.IN_PROGRESS, OrderStatus.PENDING -> true
    else -> false
}

fun OrderModel.permissionsForAdmin(): Pair<Boolean, Boolean> = Pair(true, true)


sealed class OrderDetailsEvent {
    object LoadOrderDetails: OrderDetailsEvent()
    object CancelOrderDetails: OrderDetailsEvent()
    object ChangeOrderStatusDetails: OrderDetailsEvent()
    data class EnterNote(val data: String): OrderDetailsEvent()
}

data class OrderDetailsState(
    //val uiStatus: UiStatus = UiStatus.Loading,
    val order: OrderModel = OrderModel(),
    val setNextOrderStatus: Boolean = false,
    val setCancelOrder: Boolean = false,
    val orderId: String = "",
    val userType: UserType = UserType.UNKNOWN,
    val tableNumber: String = "",
    val tableId: String = "",
    val canEditNote: Boolean = true
)

sealed class OrderDetailsSideEffect {
    data class OrderDetailsLoadingError(val err: String): OrderDetailsSideEffect()
    data class OrderDetailsCancellationError(val err: String): OrderDetailsSideEffect()
    data class OrderDetailsChangeStatusError(val err: String): OrderDetailsSideEffect()
    object GoBack: OrderDetailsSideEffect()
}

