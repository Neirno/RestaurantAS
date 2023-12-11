package com.neirno.restaurantas.presentation.ui.waiter

import android.util.Log
import androidx.lifecycle.ViewModel
import com.neirno.restaurantas.core.util.Response
import com.neirno.restaurantas.core.util.UiStatus
import com.neirno.restaurantas.domain.model.OrderModel
import com.neirno.restaurantas.domain.model.TableModel
import com.neirno.restaurantas.domain.use_case.order.GetOrdersUseCase
import com.neirno.restaurantas.domain.use_case.table.GetTablesUseCase
import com.neirno.restaurantas.domain.use_case.table.ServiceTableUseCase
import com.neirno.restaurantas.domain.use_case.user.GetUserStatusUseCase
import com.neirno.restaurantas.domain.use_case.user.GetUserTypeUseCase
import com.neirno.restaurantas.domain.use_case.user.GetUserUIdUseCase
import com.neirno.restaurantas.presentation.ui.admin.AdminSideEffect
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class WaiterViewModel @Inject constructor(
    private val getTablesUseCase: GetTablesUseCase,
    private val getUserStatusUseCase: GetUserStatusUseCase,
    private val getUserTypeUseCase: GetUserTypeUseCase,
    private val getUserUIdUseCase: GetUserUIdUseCase,
    private val serviceTableUseCase: ServiceTableUseCase,
): ViewModel(), ContainerHost<WaiterState, WaiterSideEffect> {

    override val container: Container<WaiterState, WaiterSideEffect> = container(WaiterState())

    init {
        getUserStatus()
    }

    fun onEvent(event: WaiterEvent) {
        when (event) {
            is WaiterEvent.LoadUnServedTables -> {
                loadTables()
            }
            is WaiterEvent.GetUserStatus -> {
                getUserStatus()
            }
            is WaiterEvent.OpenOrder -> {
                openOrders(event.tableId, event.tableNumber)
            }
            is WaiterEvent.ServiceTable -> {
                serviceTable(event.tableId)
            }
            WaiterEvent.GoToSettings -> {
                intent { postSideEffect(WaiterSideEffect.NavigateToSettings) }
            }
        }
    }

    private fun serviceTable(tableId: String) = intent {
        serviceTableUseCase(tableId).collect { response ->
            when (response) {
                is Response.Error -> {
                    reduce { state.copy(status = UiStatus.Error("Ошибка принятия стола.")) }
                }
                is Response.Success -> {
                    reduce { state.copy(status = UiStatus.Success) }
                }
                is Response.Loading -> {}
            }
        }
    }

    private fun openOrders(tableId: String, tableNumber: String) = intent {
        getUserTypeUseCase().collect { response ->
            when (response) {
                is Response.Success -> {
                    Log.i("WAITER VM", response.data)
                    postSideEffect(WaiterSideEffect.NavigateToOrder(
                        tableId = tableId,
                        userType = response.data,
                        tableNumber = tableNumber,
                    ))
                }
                else -> {}
            }

        }
    }

    private fun loadTables() = intent {
        getTablesUseCase().collect { response ->
            when (response) {
                is Response.Success -> {
                    reduce {
                        // Разделение столов на myTables и freeTables с учетом всех условий
                        val (myTables, notMyTables) = response.data.partition {
                            it.reserved && it.reservedBy.isNotEmpty() && it.servedBy == state.userId
                        }
                        val freeTables = notMyTables.filter {
                            it.reserved && it.reservedBy.isNotEmpty() && it.servedBy == ""
                        }

                        state.copy(
                            status = UiStatus.Success,
                            freeTables = freeTables,
                            myTables = myTables
                        )
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

    private fun getUserStatus() = intent {
        val userId = getUserUIdUseCase()
        if (userId == null) {
            reduce { state.copy(status = UiStatus.Error("Что-то пошло не так!")) }
            return@intent
        } else
            reduce { state.copy(userId = userId) }
        getUserStatusUseCase().collect { response ->
            when (response) {
                is Response.Success -> {
                    if (!response.data) {
                        reduce { state.copy(status = UiStatus.Error("Вы не на работе!"), userStatus = false) }
                    } else {
                        reduce { state.copy(userStatus = response.data) }
                        loadTables()
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

sealed class WaiterEvent {
    object GoToSettings: WaiterEvent()
    object LoadUnServedTables: WaiterEvent()
    object GetUserStatus: WaiterEvent()
    data class OpenOrder(val tableId: String, val tableNumber: String): WaiterEvent()
    data class ServiceTable(val tableId: String): WaiterEvent()
}

data class WaiterState (
    val status: UiStatus = UiStatus.Success,
    val userStatus: Boolean = false,
    val userId: String = "",
    val freeTables: List<TableModel> = emptyList(),
    val myTables: List<TableModel> = emptyList(),
    val orders: List<OrderModel> = emptyList()
)

sealed class WaiterSideEffect {
    data class NavigateToOrder(
        val tableId: String,
        val tableNumber: String,
        val userType: String,
    ): WaiterSideEffect()
    object NavigateToSettings: WaiterSideEffect()
}