package com.neirno.restaurantas.presentation.ui.auth.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.neirno.restaurantas.presentation.ui.auth.AuthScreen
import com.neirno.restaurantas.presentation.ui.auth.AuthViewModel

fun NavGraphBuilder.authNavigation(navController : NavHostController) {
    composable(route = "auth_screen") {
        val authViewModel: AuthViewModel = hiltViewModel()
        AuthScreen(
            modifier = Modifier,
            navController = navController,
            viewState = authViewModel.container.stateFlow.collectAsState().value,
            onEvent = authViewModel::onEvent,
            sideEffect = authViewModel.container.sideEffectFlow
        )
    }
}