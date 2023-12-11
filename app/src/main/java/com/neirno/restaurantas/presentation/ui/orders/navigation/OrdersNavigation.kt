package com.neirno.restaurantas.presentation.ui.orders.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.neirno.restaurantas.presentation.ui.orders.OrdersScreen
import com.neirno.restaurantas.presentation.ui.orders.OrdersViewModel

fun NavGraphBuilder.ordersNavigation(navController: NavController) {
    composable(
        "orders_screen/{tableId}/{tableNumber}/{userType}",
        arguments = listOf(
            navArgument("tableId") { type = NavType.StringType},
            navArgument("userType") { type = NavType.StringType},
            navArgument("tableNumber") { type = NavType.StringType}
        )
    ) {
        val ordersViewModel: OrdersViewModel = hiltViewModel()
        OrdersScreen(
            modifier = Modifier,
            navController = navController,
            viewState = ordersViewModel.container.stateFlow.collectAsState().value,
            onEvent = ordersViewModel::onEvent,
            sideEffect = ordersViewModel.container.sideEffectFlow
        )
    }
}