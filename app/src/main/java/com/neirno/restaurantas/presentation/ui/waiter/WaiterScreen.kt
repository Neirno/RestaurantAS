package com.neirno.restaurantas.presentation.ui.waiter

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.neirno.restaurantas.core.ui.UserTopAppBar
import com.neirno.restaurantas.core.util.UiStatus
import com.neirno.restaurantas.presentation.ui.admin.AdminEvent
import com.neirno.restaurantas.presentation.ui.waiter.pages.ErrorPage
import com.neirno.restaurantas.presentation.ui.waiter.pages.WaiterPage
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaiterScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewState: WaiterState,
    onEvent: (WaiterEvent) -> Unit,
    sideEffect: Flow<WaiterSideEffect>
) {
    LaunchedEffect(sideEffect) {
        sideEffect.collect { sideEffect ->
            handleSideEffect(
                sideEffect = sideEffect,
                navController = navController,
            )
        }
    }

    Scaffold (
        topBar = {
            UserTopAppBar(
                userType = "Официант",
                workStatus = viewState.userStatus,
                navigate = { onEvent(WaiterEvent.GoToSettings) }
            )
        }
    ) { scaffoldPadding ->
        when (viewState.status) {
            is UiStatus.Success -> {
                WaiterPage(
                    modifier = modifier
                        .padding(scaffoldPadding)
                        .fillMaxSize(),
                    freeTables = viewState.freeTables,
                    myTables = viewState.myTables,
                    openOrder = { tableId, tableNumber -> onEvent(WaiterEvent.OpenOrder(tableId, tableNumber))},
                    serviceTable = { tableId -> onEvent(WaiterEvent.ServiceTable(tableId))}
                )
            }
            is UiStatus.Error -> {
                ErrorPage (
                    modifier = Modifier
                        .padding(scaffoldPadding)
                        .fillMaxSize(),
                    error = viewState.status.message,
                ) {
                    onEvent(WaiterEvent.GetUserStatus)
                }
            }
            is UiStatus.Loading -> {}
        }
    }

}

private fun handleSideEffect(
    sideEffect: WaiterSideEffect,
    navController: NavController,
) {
    when (sideEffect) {
        is WaiterSideEffect.NavigateToOrder -> {
            navController.navigate("orders_screen/${sideEffect.tableId}/" +
                        "${sideEffect.tableNumber}/${sideEffect.userType}")
        }
        WaiterSideEffect.NavigateToSettings -> {
            navController.navigate("settings_screen")
        }
    }
}
