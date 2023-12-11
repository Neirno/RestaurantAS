package com.neirno.restaurantas.presentation.ui.orders

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.neirno.restaurantas.core.constans.OrderStatus
import com.neirno.restaurantas.core.extension.showToast
import com.neirno.restaurantas.core.ui.BlinkingText
import com.neirno.restaurantas.core.ui.ConfirmDialog
import com.neirno.restaurantas.core.ui.LogoIcon
import com.neirno.restaurantas.core.ui.UserTopAppBar
import com.neirno.restaurantas.domain.model.OrderModel
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewState: OrdersState,
    onEvent: (OrdersEvent) -> Unit,
    sideEffect: Flow<OrdersSideEffect>
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showConfirmDialog by remember { mutableStateOf(false) }


    LaunchedEffect(sideEffect) {
        sideEffect.collect { sideEffect ->
            handleSideEffect(
                sideEffect = sideEffect,
                navController = navController,
                snackbarHostState = snackbarHostState,
                showConfirmDialog = { showConfirmDialog = it },
                context = context,
            )
        }
    }

    Scaffold (
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    modifier = Modifier
                        .padding(16.dp)
                        .clip(MaterialTheme.shapes.large)
                ) {
                    Text(data.visuals.message)
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onEvent(OrdersEvent.OpenOrder("_")) }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
            }
        },
        topBar =  {
            CenterAlignedTopAppBar(
                modifier = modifier.padding(bottom = 32.dp),
                title = {
                    LogoIcon(Modifier.size(60.dp))
                },
                navigationIcon = {
                    BlinkingText(
                        modifier = Modifier.padding(18.dp),
                        text = "Стол: ${viewState.tableNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        isBlinking = true // или ваше условие для мигания
                    )
                                 /*   Text(
                        modifier = Modifier.padding(18.dp),
                        text = "Стол: " + viewState.tableNumber,
                        style = MaterialTheme.typography.titleMedium
                    )*/
                },
                actions = {
                    IconButton(onClick = { showConfirmDialog = true }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null)
                    }
                }
            )
        }
    ) { scaffoldPadding ->
        Column (modifier = modifier.padding(scaffoldPadding), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(modifier = Modifier.padding(bottom = 8.dp), style = MaterialTheme.typography.titleMedium, text = "Список заказов")
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(viewState.orders) { order ->
                    OrderItem(openOrder = { onEvent(OrdersEvent.OpenOrder(order.orderId)) }, order = order)
                }
            }
        }
    }
    if (showConfirmDialog) {
        ConfirmDialog(
            title = "Завершение работы",
            text = "Вы уверены, что хотите завершить работу со столом?",
            onConfirm = { onEvent(OrdersEvent.FinishWorkWithTable) },
            onDismiss = { showConfirmDialog = false }
        )
    }
}

@Composable
fun OrderItem(
    modifier: Modifier = Modifier,
    openOrder: () -> Unit,
    order: OrderModel
) {
    val statusColor = (OrderStatus.fromString(order.status).getOrderStatusColor())

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { openOrder() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Заказ: ${order.note}",
                style = MaterialTheme.typography.bodySmall
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "Статус: ${OrderStatus.fromString(order.status).getDisplayName()}",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.Circle,
                    contentDescription = "Статус заказа",
                    tint = statusColor
                )

            }
        }
    }
}

private suspend fun handleSideEffect(
    sideEffect: OrdersSideEffect,
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    showConfirmDialog: (Boolean) -> Unit,
    context: Context
) {
    when (sideEffect) {
        is OrdersSideEffect.GoToOrder -> {
            navController.navigate("order_details_screen/" +
                    "${sideEffect.orderId}/" +
                    "${sideEffect.userType}/" +
                    "${sideEffect.tableNumber}/" +
                    sideEffect.tableId
            )
        }
        is OrdersSideEffect.ShowError -> {
            showConfirmDialog(false)
            snackbarHostState.showSnackbar(sideEffect.err)
        }
        is OrdersSideEffect.LoadingError -> {
            context.showToast(sideEffect.err)
            navController.popBackStack()
        }
        OrdersSideEffect.GoBack -> {
            navController.popBackStack()
        }
    }
}