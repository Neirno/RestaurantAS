package com.neirno.restaurantas.presentation.ui.settings.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.neirno.restaurantas.presentation.ui.settings.SettingsScreen
import com.neirno.restaurantas.presentation.ui.settings.SettingsViewModel

fun NavGraphBuilder.settingsNavigation(navController: NavHostController) {
    composable(route = "settings_screen") {
        val settingsViewModel: SettingsViewModel = hiltViewModel()
        SettingsScreen(
            modifier = Modifier.fillMaxSize(),
            navController = navController,
            viewState = settingsViewModel.container.stateFlow.collectAsState().value,
            onEvent = settingsViewModel::onEvent,
            sideEffect = settingsViewModel.container.sideEffectFlow
        )
    }
}
