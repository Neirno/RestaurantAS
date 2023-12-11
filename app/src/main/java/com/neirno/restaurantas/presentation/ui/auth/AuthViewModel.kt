package com.neirno.restaurantas.presentation.ui.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.AuthCredential
import com.neirno.restaurantas.core.constans.UserType
import com.neirno.restaurantas.core.util.UiStatus
import com.neirno.restaurantas.core.util.Response
import com.neirno.restaurantas.domain.use_case.auth.IsLoggedInUseCase
import com.neirno.restaurantas.domain.use_case.auth.SignInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val isSignInUseCase: IsLoggedInUseCase,
): ViewModel(), ContainerHost<AuthState, AuthSideEffect> {

    override val container: Container<AuthState, AuthSideEffect> = container(AuthState())

    init {
        onEvent(AuthEvent.IsLoggedIn)
    }

    fun onEvent(event: AuthEvent) {
        when(event) {
            is AuthEvent.SignIn -> {
                signIn(event.email, event.password)
            }
            is AuthEvent.IsLoggedIn -> {
                isLoggedIn()
            }
        }
    }

    private fun isLoggedIn() = intent {
        isSignInUseCase.invoke().collect { response ->
            when (response) {
                is Response.Success -> {
                    val userRole = response.data

                    postSideEffect(AuthSideEffect.Navigate(userRole))
                }
                is Response.Error -> {
                    //reduce{ state.copy(status = UiStatus.Error(response.msg)) }
                    reduce { state.copy(status = UiStatus.Success) }
                }
                is Response.Loading -> {
                    reduce { state.copy(status = UiStatus.Loading) }
                }
            }
        }
    }

    private fun signIn(email: String, password: String) = intent {
        if (!isValidEmail(email)) {
            //reduce { state.copy(status = UiStatus.Error("Ошибка ввода email")) }
            postSideEffect(AuthSideEffect.AuthError)
            return@intent
        }
        signInUseCase.invoke(email, password).collect { response ->
            when (response) {
                is Response.Success -> {
                    val (authResult, userRole) = response.data

                    reduce{ state.copy(status = UiStatus.Loading) }
                    postSideEffect(AuthSideEffect.Navigate(userRole))
                }
                is Response.Error -> {
                    reduce { state.copy(status = UiStatus.Success) }
                    //reduce{ state.copy(status = UiStatus.Error("Неверен логин или пароль!")) }

                    postSideEffect(AuthSideEffect.AuthError)
                }
                is Response.Loading -> {
                    reduce{ state.copy(status = UiStatus.Loading) }
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex()
        return email.matches(emailRegex)
    }
}

sealed class AuthEvent {
    data class SignIn(val email: String, val password: String) : AuthEvent()
    object IsLoggedIn: AuthEvent()
}

data class AuthState(
    val status: UiStatus = UiStatus.Loading,
)

sealed class AuthSideEffect {
    object AuthError: AuthSideEffect()
    data class Navigate(val nav: UserType) : AuthSideEffect()
}