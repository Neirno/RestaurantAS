package com.neirno.restaurantas.presentation.ui.cook

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.neirno.restaurantas.core.ui.Loading
import com.neirno.restaurantas.core.ui.UserTopAppBar
import com.neirno.restaurantas.core.util.UiStatus
import com.neirno.restaurantas.presentation.ui.admin.AdminEvent
import com.neirno.restaurantas.presentation.ui.cook.pages.CookPage
import com.neirno.restaurantas.presentation.ui.cook.pages.ErrorPage
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewState: CookState,
    onEvent: (CookEvent) -> Unit,
    sideEffect: Flow<CookSideEffect>
) {
    LaunchedEffect(sideEffect) {
        sideEffect.collect { sideEffect ->
            handleSideEffect(
                sideEffect = sideEffect,
                navController = navController
            )
        }
    }

    Scaffold (
        topBar = {
            UserTopAppBar(
                userType = "Повар",
                workStatus = viewState.userStatus,
                navigate = { onEvent(CookEvent.GoToSettings) }
            )
        }
    ) { scaffoldPadding ->
        when (viewState.status) {
            is UiStatus.Success -> {
                CookPage(
                    modifier = modifier
                        .padding(scaffoldPadding)
                        .fillMaxSize(),
                    freeOrders = viewState.freeOrders,
                    myOrders = viewState.myOrders,
                    openOrder = { order -> onEvent(CookEvent.OpenOrder(order)) },
                    serviceOrder = { order -> onEvent(CookEvent.ServiceOrder(order))}
                )
            }
            is UiStatus.Error -> {
                ErrorPage(
                    modifier = modifier
                        .padding(scaffoldPadding)
                        .fillMaxSize()
                ) { onEvent(CookEvent.LoadOrders) }
            }
            is UiStatus.Loading -> {
                Loading()
            }
        }
    }
}

private fun handleSideEffect(
    sideEffect: CookSideEffect,
    navController: NavController
) {
    when (sideEffect) {
        is CookSideEffect.GoToOrder -> { // order_details_screen/{orderId}/{userType}/{tableNumber}/{tableId}
            navController.navigate("order_details_screen/" +
                    "${sideEffect.orderId}/" +
                    "${sideEffect.userType}/" +
                    "${sideEffect.tableNumber}/" +
                    sideEffect.tableId
            )
        }
        CookSideEffect.NavigateToSettings -> {
            navController.navigate("settings_screen")
        }
    }
}