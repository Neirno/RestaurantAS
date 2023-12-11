package com.neirno.restaurantas.presentation.ui.hostess

import android.util.Log
import androidx.lifecycle.ViewModel
import com.neirno.restaurantas.core.util.Response
import com.neirno.restaurantas.core.util.UiStatus
import com.neirno.restaurantas.domain.model.TableModel
import com.neirno.restaurantas.domain.use_case.table.GetTablesUseCase
import com.neirno.restaurantas.domain.use_case.table.ReserveTableUseCase
import com.neirno.restaurantas.domain.use_case.user.GetUserStatusUseCase
import com.neirno.restaurantas.presentation.ui.admin.AdminSideEffect
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.syntax.simple.repeatOnSubscription
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class HostessViewModel @Inject constructor(
    private val getTablesUseCase: GetTablesUseCase,
    private val getUserStatusUseCase: GetUserStatusUseCase,
    private val reserveTableUseCase: ReserveTableUseCase
): ViewModel(), ContainerHost<HostessState, HostessSideEffect> {

    override val container: Container<HostessState, HostessSideEffect> = container(HostessState())

    init {
        getUserStatus()
    }

    fun onEvent(event: HostessEvent) {
        when(event) {
            is HostessEvent.LoadTables -> {
                loadTables()
            }
            is HostessEvent.SetPersons -> {
                intent { reduce {state.copy(persons = event.persons)} }
            }
            is HostessEvent.GetUserStatus -> {
                getUserStatus()
            }
            is HostessEvent.ReserveTable -> {
                reserveTable(event.tableId, event.persons)
            }
            is HostessEvent.StartReservedMode -> {
                startReservedMode()
            }
            is HostessEvent.EndReservedMode -> {
                endReservedMode()
            }
            is HostessEvent.SetTableById -> {
                intent { reduce { state.copy(chosenTableById = event.tableId) } }
            }
            HostessEvent.GoToSettings -> {
                intent { postSideEffect(HostessSideEffect.NavigateToSettings) }
            }
        }
    }

    private fun endReservedMode() = intent {
        reduce { state.copy(reservedStatus = false) }
        postSideEffect(HostessSideEffect.ShowSnackbar("Режим выбора стола окончен."))
    }

    private fun startReservedMode() =   intent {
        // Проверяем, все ли столы зарезервированы
        val allTablesReserved = state.tables.all { it.reserved } || state.persons == listOf("")

        // Отправляем соответствующее сообщение в зависимости от результата
        if (allTablesReserved) {
            reduce { state.copy(reservedStatus = false) }
            postSideEffect(HostessSideEffect.ShowSnackbar("Все столы зарезервированы" +
                    " или Вы не ввели данные пользователей."))
        } else {
            reduce { state.copy(reservedStatus = true) }
            postSideEffect(HostessSideEffect.ShowSnackbar("Выберите стол."))
        }
    }

    private fun reserveTable(tableId: String?, persons: List<String>) = intent {
        if (state.chosenTableById == null) {
            postSideEffect(HostessSideEffect.ShowSnackbar("Выберите стол."))
            return@intent
        }
        if (state.persons == listOf("")) {
            postSideEffect(HostessSideEffect.ShowSnackbar("Впишите ФИО посетителей."))
            return@intent
        }
        reserveTableUseCase(tableId!!, persons).collect { response ->
            when (response) {
                is Response.Success -> {
                    reduce {
                        state.copy(
                            persons = listOf(""),
                            reservedStatus = false,
                            chosenTableById = null
                        )
                    }
                    postSideEffect(HostessSideEffect.ShowSnackbar("Посетитель на месте."))
                    Log.v("RESERVETABLE", "SUCCES")
                }
                is Response.Error -> {
                    reduce { state.copy(status = UiStatus.Error(
                        "Возникли проблемы с подключением." +
                                "Повторите позже")
                        )
                    }
                    Log.w("RESERVETABLE", "ERROR")
                }
                is Response.Loading -> {}
            }
        }
    }

    private fun getUserStatus() = intent {
        getUserStatusUseCase().collect { response ->
            when (response) {
                is Response.Success -> {
                    if (!response.data) {
                        reduce { state.copy(status = UiStatus.Error("Вы не на работе!"), userStatus = response.data) }
                    } else {
                        reduce { state.copy(userStatus = response.data) }
                        loadTables()
                    }
                }
                is Response.Error -> {
                    reduce { state.copy(status = UiStatus.Error(
                        "Возникли проблемы с подключением." +
                                "Повторите позже")
                        )
                    }
                }
                is Response.Loading -> {}
            }
        }
    }

    private fun loadTables() = intent {
        repeatOnSubscription {

            getTablesUseCase().collect { response ->
                when (response) {
                    is Response.Success -> {
                        val tables = response.data.reversed()
                        reduce {
                            state.copy(
                                tables = tables,
                                status = UiStatus.Success
                            )
                        }
                    }

                    is Response.Error -> {
                        reduce { state.copy(status = UiStatus.Error(
                                "Возникли проблемы с подключением." +
                                "Повторите позже")
                            )
                        }
                    }
                    is Response.Loading -> {
                        reduce { state.copy(status = UiStatus.Loading) }
                    }
                }
            }
        }
    }
}

sealed class HostessEvent {
    object GoToSettings: HostessEvent()
    object LoadTables: HostessEvent()
    data class SetPersons(val persons: List<String>): HostessEvent()
    object GetUserStatus: HostessEvent()
    data class ReserveTable(val tableId: String?, val persons: List<String>): HostessEvent()
    object StartReservedMode: HostessEvent()
    object EndReservedMode: HostessEvent()
    data class SetTableById(val tableId: String?): HostessEvent()
}

data class HostessState (
    val status: UiStatus = UiStatus.Loading,
    val tables: List<TableModel> = emptyList(),
    val persons: List<String> = listOf(""),
    val userStatus: Boolean = false,
    val reservedStatus: Boolean = false,
    val chosenTableById: String? = null
)

sealed class HostessSideEffect {
    data class ShowSnackbar(val text: String): HostessSideEffect()
    object NavigateToSettings: HostessSideEffect()
}