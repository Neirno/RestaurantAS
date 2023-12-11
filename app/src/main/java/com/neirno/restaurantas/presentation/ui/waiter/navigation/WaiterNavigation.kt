package com.neirno.restaurantas.presentation.ui.waiter.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.neirno.restaurantas.presentation.ui.waiter.WaiterScreen
import com.neirno.restaurantas.presentation.ui.waiter.WaiterViewModel

fun NavGraphBuilder.waiterNavigation(navController : NavHostController) {
    composable(route = "waiter_screen") {
        val waiteViewModel: WaiterViewModel = hiltViewModel()
        WaiterScreen(
            modifier = Modifier,
            navController = navController,
            viewState = waiteViewModel.container.stateFlow.collectAsState().value,
            onEvent = waiteViewModel::onEvent,
            sideEffect = waiteViewModel.container.sideEffectFlow
        )
    }
}