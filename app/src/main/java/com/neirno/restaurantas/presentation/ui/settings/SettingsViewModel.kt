package com.neirno.restaurantas.presentation.ui.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import com.neirno.restaurantas.core.util.Response
import com.neirno.restaurantas.domain.model.UserModel
import com.neirno.restaurantas.domain.use_case.auth.SignOutUseCase
import com.neirno.restaurantas.domain.use_case.user.GetUserInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val signOutUseCase: SignOutUseCase
): ViewModel(), ContainerHost<SettingsState, SettingsSideEffect> {

    override val container: Container<SettingsState, SettingsSideEffect> = container(SettingsState())

    init {
        loadUserInfo()
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            SettingsEvent.GoBack -> {
                goBack()
            }
            SettingsEvent.LoadUserInfo -> {
                loadUserInfo()
            }
            SettingsEvent.SignOut -> {
                signOut()
            }
        }
    }

    private fun signOut() = intent {
        signOutUseCase()
        postSideEffect(SettingsSideEffect.NavigateToLoginScreen)
    }

    private fun goBack() = intent {
        postSideEffect(SettingsSideEffect.NavigateToBack)
    }

    private fun loadUserInfo() = intent {
        getUserInfoUseCase().collect { response ->
            when (response) {
                is Response.Success -> {
                    reduce { state.copy(user = response.data) }
                }
                is Response.Error -> {
                    //postSideEffect(SettingsSideEffect.NavigateToBack)
                    Log.i("Settings VM", response.msg)
                }
                Response.Loading -> {}
            }
        }
    }
}

sealed class SettingsEvent {
    object GoBack: SettingsEvent()
    object LoadUserInfo: SettingsEvent()
    object SignOut: SettingsEvent()
}

data class SettingsState (
    val user: UserModel = UserModel(),
)

sealed class SettingsSideEffect {
    object NavigateToBack: SettingsSideEffect()
    object NavigateToLoginScreen: SettingsSideEffect()
}