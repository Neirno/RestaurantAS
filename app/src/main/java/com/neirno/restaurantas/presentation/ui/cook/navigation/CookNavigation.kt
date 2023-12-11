package com.neirno.restaurantas.presentation.ui.cook.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.neirno.restaurantas.presentation.ui.cook.CookScreen
import com.neirno.restaurantas.presentation.ui.cook.CookViewModel

fun NavGraphBuilder.cookNavigation(navController: NavHostController) {
    composable(route = "cook_screen") {
        val cookViewModel: CookViewModel = hiltViewModel()
        CookScreen(
            modifier = Modifier,
            navController = navController,
            viewState = cookViewModel.container.stateFlow.collectAsState().value,
            onEvent = cookViewModel::onEvent,
            sideEffect = cookViewModel.container.sideEffectFlow
        )
    }
}