package com.neirno.restaurantas.presentation.ui.order_details

import android.content.Context
import android.util.Log
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.material3.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.neirno.restaurantas.core.constans.OrderStatus
import com.neirno.restaurantas.core.extension.showToast
import com.neirno.restaurantas.core.ui.ConfirmDialog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewState: OrderDetailsState,
    onEvent: (OrderDetailsEvent) -> Unit,
    sideEffect: Flow<OrderDetailsSideEffect>
) {
    val context = LocalContext.current
    //var text = viewState.order.note
    val snackbarHostState = remember { SnackbarHostState() }
    val lazyListState = rememberLazyListState()
    var showConfirmDiralog by remember { mutableStateOf(false) }
    val readOnly by remember(viewState.canEditNote) {
        derivedStateOf { !viewState.canEditNote }
    }
    var text by remember { mutableStateOf(viewState.order.note) }
    LaunchedEffect(key1 = viewState.order.note) {
        text = viewState.order.note
    }


    val coroutineScope = rememberCoroutineScope()

    val orderStatuses = listOf(
        OrderStatus.PENDING,
        OrderStatus.ACCEPTED,
        OrderStatus.IN_PROGRESS,
        OrderStatus.READY,
        OrderStatus.DELIVERED,
        OrderStatus.COMPLETED,
    )

    LaunchedEffect(sideEffect) {
        sideEffect.collect { sideEffect ->
            handleSideEffect(
                sideEffect = sideEffect,
                navController = navController,
                snackbarHostState = snackbarHostState,
                context = context,
            )
        }
    }

    LaunchedEffect(key1 = viewState.order.status) {
        val index = orderStatuses.indexOfFirst { it == OrderStatus.fromString(viewState.order.status) }
        if (index != -1) {
            coroutineScope.launch {
                lazyListState.animateScrollToItem(index)
            }
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
            if (viewState.setCancelOrder || viewState.setNextOrderStatus) {
                FloatingActionButton(
                    containerColor = if (viewState.setCancelOrder) MaterialTheme.colorScheme.error
                    else FloatingActionButtonDefaults.containerColor,
                    onClick = {
                        if (viewState.setCancelOrder)
                            showConfirmDiralog = true
                        else {
                            onEvent(OrderDetailsEvent.EnterNote(text))
                            onEvent(OrderDetailsEvent.ChangeOrderStatusDetails)
                        }
                    }) {
                    if (viewState.setCancelOrder)
                        Icon(imageVector = Icons.Default.Cancel, contentDescription = null)
                    else
                        Icon(imageVector = Icons.Default.NavigateNext, contentDescription = null)
                }
            }
         },
        topBar = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        LazyRow(
                            state = lazyListState,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            items(orderStatuses) { status ->
                                StatusItem(
                                    status = status,
                                    isSelected = status == OrderStatus.fromString(viewState.order.status)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) {
        Column(
            modifier = modifier
                .padding(it)
                .fillMaxSize()
                //.background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Box (
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Стол №" + viewState.tableNumber,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .border(1.dp, color = MaterialTheme.colorScheme.onBackground)
            ) {
                  BasicTextField(
                      value = text,
                      onValueChange = { newValue ->
                          if (!readOnly) {
                              // обновляем текст только если поле редактируемо
                              text = newValue
                              //onEvent(OrderDetailsEvent.EnterNote(text))
                          }
                      },
                      textStyle = MaterialTheme.typography.titleSmall.copy(
                          color = MaterialTheme.colorScheme.onBackground
                      ),
                      readOnly = readOnly,
                      cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                      modifier = Modifier
                          .fillMaxSize()
                          .padding(8.dp)
                  )

            }
        }
    }

    if (showConfirmDiralog) {
        ConfirmDialog(
            title = "Отмена заказа",
            text = "Вы уверены, что хотите отменить заказ?",
            onConfirm = { onEvent(OrderDetailsEvent.CancelOrderDetails) },
            onDismiss = { showConfirmDiralog = false }
        )
    }
}

@Composable
fun StatusItem(status: OrderStatus, isSelected: Boolean) {
    val statusColor = status.getOrderStatusColor()

    Column(
        modifier = Modifier
            .padding(vertical = 10.dp)
            .size(75.dp), // Или другой размер по вашему усмотрению
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Круг с цветом
        Box(
            modifier = Modifier
                .size(50.dp) // Или другой размер по вашему усмотрению
                .clip(CircleShape)
                .background(statusColor)
                .border(
                    width = 2.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = CircleShape
                )
        )

        // Текст под кругом
        Spacer(modifier = Modifier.height(4.dp)) // Добавляем немного пространства между кругом и текстом
        Text(
            text = status.getDisplayName(),
            style = MaterialTheme.typography.bodySmall
        )
    }
}




private suspend fun handleSideEffect(
    sideEffect: OrderDetailsSideEffect,
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    context: Context
) {
    when (sideEffect) {
        is OrderDetailsSideEffect.OrderDetailsCancellationError -> {
            snackbarHostState.showSnackbar(sideEffect.err)
        }
        is OrderDetailsSideEffect.OrderDetailsChangeStatusError -> {
            Log.d("OrderDetailsScreen", "Showing snackbar: ${sideEffect.err}")

            snackbarHostState.showSnackbar(sideEffect.err)
        }
        is OrderDetailsSideEffect.OrderDetailsLoadingError -> {
            snackbarHostState.showSnackbar(sideEffect.err)
        }
        is OrderDetailsSideEffect.GoBack -> {
            Log.i("Order Screen", "Try go back screen")
            context.showToast("Заказ выполнен.")
            navController.popBackStack()
        }
    }
}