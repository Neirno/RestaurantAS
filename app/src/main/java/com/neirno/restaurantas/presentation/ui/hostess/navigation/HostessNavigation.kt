package com.neirno.restaurantas.presentation.ui.hostess.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.neirno.restaurantas.presentation.ui.hostess.HostessScreen
import com.neirno.restaurantas.presentation.ui.hostess.HostessViewModel


fun NavGraphBuilder.hostessNavigation(navController: NavHostController) {
    composable(route = "hostess_screen") {
        val hostessViewModel: HostessViewModel = hiltViewModel()
        HostessScreen(
            modifier = Modifier,
            navController = navController,
            viewState = hostessViewModel.container.stateFlow.collectAsState().value,
            onEvent = hostessViewModel::onEvent,
            sideEffect = hostessViewModel.container.sideEffectFlow
        )
    }
}
