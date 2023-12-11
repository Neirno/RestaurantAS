package com.neirno.restaurantas.presentation.ui.admin.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.neirno.restaurantas.presentation.ui.admin.AdminScreen
import com.neirno.restaurantas.presentation.ui.admin.AdminViewModel

fun NavGraphBuilder.adminNavigation(navController: NavHostController) {
    composable(route = "admin_screen") {
        val adminViewModel: AdminViewModel = hiltViewModel()
        AdminScreen(
            modifier = Modifier.fillMaxSize(),
            navController = navController,
            viewState = adminViewModel.container.stateFlow.collectAsState().value,
            onEvent = adminViewModel::onEvent,
            sideEffect = adminViewModel.container.sideEffectFlow
        )
    }
}