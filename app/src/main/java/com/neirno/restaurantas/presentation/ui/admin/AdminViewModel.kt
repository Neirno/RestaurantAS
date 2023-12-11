package com.neirno.restaurantas.presentation.ui.admin

import android.util.Log
import androidx.lifecycle.ViewModel
import com.neirno.restaurantas.core.constans.UserType
import com.neirno.restaurantas.core.util.Response
import com.neirno.restaurantas.core.util.UiStatus
import com.neirno.restaurantas.domain.model.UserModel
import com.neirno.restaurantas.domain.use_case.user.GetUserStatusUseCase
import com.neirno.restaurantas.domain.use_case.user.GetUserUIdUseCase
import com.neirno.restaurantas.domain.use_case.user.GetUsersUseCase
import com.neirno.restaurantas.domain.use_case.user.SetUserStatusUseCase
import com.neirno.restaurantas.presentation.ui.waiter.WaiterEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectIndexed
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.syntax.simple.repeatOnSubscription
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val getUserStatusUseCase: GetUserStatusUseCase,
    private val setUserStatusUseCase: SetUserStatusUseCase,
    private val getUsersUseCase: GetUsersUseCase,
    private val getUserUIdUseCase: GetUserUIdUseCase
): ViewModel(), ContainerHost<AdminState, AdminSideEffect> {

    override val container: Container<AdminState, AdminSideEffect> = container(AdminState())

    init {
        getUserStatus()
    }

    fun onEvent(event: AdminEvent) {
        when (event) {
            AdminEvent.LoadUsers -> {
                loadUsers()
            }
            AdminEvent.GetUserStatus -> {
                getUserStatus()
            }
            is AdminEvent.ChangeWorkingStatus -> {
                changeWorkingStatus(event.user)
            }
            AdminEvent.GoToSettings -> {
                intent { postSideEffect(AdminSideEffect.NavigateToSettings) }
            }
        }
    }

    private fun loadUsers() = intent {
        val userId = getUserUIdUseCase()

        if (userId == null) {
            reduce { state.copy(status = UiStatus.Error("Произошла ошибка")) }
            return@intent
        }

        if (!state.userStatus)
            return@intent

        getUsersUseCase().collect { response ->
            when (response) {
                is Response.Success -> {
                    val users = response.data
                    val filteredUsers = users.filter { it.userType != UserType.ADMIN.type && it.userId != userId }
                    val workingWorkers = filteredUsers.filter { it.working }
                    val nonWorkingWorkers = filteredUsers.filterNot { it.working }

                    reduce {
                        state.copy(
                            status = UiStatus.Success,
                            workingWorkers = workingWorkers,
                            nonWorkingWorkers = nonWorkingWorkers
                        )
                    }
                }
                is Response.Error -> {
                    reduce { state.copy(status = UiStatus.Error(response.msg)) }
                }
                Response.Loading -> {
                    reduce { state.copy(status = UiStatus.Loading) }
                }
            }
        }
    }


    private fun changeWorkingStatus(user: UserModel) = intent {
        setUserStatusUseCase(user.userId, !user.working).collect { response ->
            when (response) {
                is Response.Error -> {
                    reduce { state.copy(status = UiStatus.Error(response.msg)) }
                }
                else -> {}
            }
        }
    }

    private fun getUserStatus() = intent {
        getUserStatusUseCase().collect { response ->
            when (response) {
                is Response.Success -> {
                    if (!response.data) {
                        reduce { state.copy(status = UiStatus.Error("Вы не на работе!"), userStatus = false) }
                    } else {
                        reduce { state.copy(userStatus = response.data) }
                        loadUsers()
                    }
                }
                is Response.Error -> {
                    reduce { state.copy(status = UiStatus.Error(response.msg)) }
                }
                is Response.Loading -> {}
            }
        }
    }
}

sealed class AdminEvent{
    object GoToSettings: AdminEvent()
    object LoadUsers: AdminEvent()
    object GetUserStatus: AdminEvent()
    data class ChangeWorkingStatus(val user: UserModel): AdminEvent()
}

data class AdminState(
    val status: UiStatus = UiStatus.Loading,
    val userStatus: Boolean = false,
    val workingWorkers: List<UserModel> = emptyList(),
    val nonWorkingWorkers: List<UserModel> = emptyList()
)

sealed class AdminSideEffect{
    data class ShowError(val err: String): AdminSideEffect()
    object NavigateToSettings: AdminSideEffect()
}