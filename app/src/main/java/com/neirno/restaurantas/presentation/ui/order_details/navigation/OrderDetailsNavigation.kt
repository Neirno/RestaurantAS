package com.neirno.restaurantas.presentation.ui.order_details.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.neirno.restaurantas.presentation.ui.order_details.OrderDetailsScreen
import com.neirno.restaurantas.presentation.ui.order_details.OrderDetailsViewModel

fun NavGraphBuilder.orderDetailsNavigation(navController: NavController) {
    composable(
        "order_details_screen/{orderId}/{userType}/{tableNumber}/{tableId}",
        arguments = listOf(
            navArgument("orderId") { type = NavType.StringType },
            navArgument("userType") { type = NavType.StringType },
            navArgument("tableNumber") { type = NavType.StringType },
            navArgument("tableId") { type = NavType.StringType },
        ),
    ) {
        val orderDetailsViewModel: OrderDetailsViewModel = hiltViewModel()
        OrderDetailsScreen(
            modifier = Modifier,
            navController = navController,
            viewState = orderDetailsViewModel.container.stateFlow.collectAsState().value,
            onEvent = orderDetailsViewModel::onEvent,
            sideEffect = orderDetailsViewModel.container.sideEffectFlow
        )
    }
}