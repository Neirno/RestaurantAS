package com.neirno.restaurantas.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.neirno.restaurantas.presentation.ui.admin.navigation.adminNavigation
import com.neirno.restaurantas.presentation.ui.auth.navigation.authNavigation
import com.neirno.restaurantas.presentation.ui.cook.navigation.cookNavigation
import com.neirno.restaurantas.presentation.ui.hostess.navigation.hostessNavigation
import com.neirno.restaurantas.presentation.ui.order_details.navigation.orderDetailsNavigation
import com.neirno.restaurantas.presentation.ui.orders.navigation.ordersNavigation
import com.neirno.restaurantas.presentation.ui.settings.navigation.settingsNavigation
import com.neirno.restaurantas.presentation.ui.waiter.navigation.waiterNavigation

@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "auth_screen") {
        authNavigation(navController = navController)
        hostessNavigation(navController = navController)
        waiterNavigation(navController = navController)
        cookNavigation(navController = navController)
        orderDetailsNavigation(navController = navController)
        ordersNavigation(navController = navController)
        adminNavigation(navController = navController)
        settingsNavigation(navController = navController)
    }
}