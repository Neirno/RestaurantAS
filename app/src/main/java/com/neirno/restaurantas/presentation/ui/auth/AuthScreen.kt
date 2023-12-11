package com.neirno.restaurantas.presentation.ui.auth

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.neirno.restaurantas.core.constans.UserType
import com.neirno.restaurantas.core.util.UiStatus
import com.neirno.restaurantas.presentation.theme.RestaurantASTheme
import com.neirno.restaurantas.presentation.ui.auth.pages.AuthPage
import com.neirno.restaurantas.core.ui.Loading
import com.neirno.restaurantas.presentation.util.extensions.showToast
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewState: AuthState,
    onEvent: (AuthEvent) -> Unit,
    sideEffect: Flow<AuthSideEffect>
) {
    val context = LocalContext.current

    LaunchedEffect(sideEffect) {
        sideEffect.collect { sideEffect ->
            handleSideEffect(
                sideEffect = sideEffect,
                navController = navController,
                context = context,
            )
        }
    }


    Scaffold {
        when (viewState.status) {
            is UiStatus.Success -> {
                AuthPage(
                    modifier = modifier.padding(it),
                    singIn = { email, password -> onEvent(AuthEvent.SignIn(email, password))}
                )
            }
            is UiStatus.Loading -> {
                Loading()
            }
            is UiStatus.Error -> {
                // Пока не требуется.
            }
        }
    }
}

private fun handleSideEffect(
    sideEffect: AuthSideEffect,
    navController: NavController,
    context: Context
) {
    when (sideEffect) {
        is AuthSideEffect.AuthError -> {
            context.showToast("Ошибка email или pass")
        }
        is AuthSideEffect.Navigate -> {
            // Преобразование строки в объект UserRole
            val userType = sideEffect.nav

            navController.popBackStack() // clear
            // Перенаправление пользователя в зависимости от его роли
            when (userType) {
                UserType.COOK -> {
                    navController.navigate("cook_screen")
                }
                UserType.ADMIN -> {
                    navController.navigate("admin_screen")
                }
                UserType.WAITER -> {
                    navController.navigate("waiter_screen")
                }
                UserType.HOSTESS -> {
                    navController.navigate("hostess_screen")
                }
                UserType.UNKNOWN -> {
                    context.showToast("Возможно с Вашим аккаунтом что-то произошло.")
                }
            }
        }
    }
}



@Preview
@Composable
fun AuthScreenLightPreview() {
    val fakeNavigationManager = NavController(LocalContext.current)
    val fakeViewState = AuthState()
    val fakeOnEvent: (AuthEvent) -> Unit = {}
    val fakeSideEffect = flowOf<AuthSideEffect>()

    RestaurantASTheme {
        AuthScreen(
            navController = fakeNavigationManager,
            viewState = fakeViewState,
            onEvent = fakeOnEvent,
            sideEffect = fakeSideEffect
        )
    }
}

@Preview
@Composable
fun AuthScreenDarkPreview() {
    val fakeNavigationManager = NavController(LocalContext.current)
    val fakeViewState = AuthState()
    val fakeOnEvent: (AuthEvent) -> Unit = {}
    val fakeSideEffect = flowOf<AuthSideEffect>()

    RestaurantASTheme (
        darkTheme = true
    ) {
        AuthScreen(
            navController = fakeNavigationManager,
            viewState = fakeViewState,
            onEvent = fakeOnEvent,
            sideEffect = fakeSideEffect
        )
    }
}