package com.neirno.restaurantas.presentation.ui.cook.pages

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.neirno.restaurantas.R
import com.neirno.restaurantas.core.ui.ConfirmDialog
import com.neirno.restaurantas.domain.model.OrderModel
import com.neirno.restaurantas.presentation.ui.waiter.pages.Tabs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CookPage(
    modifier: Modifier = Modifier,
    freeOrders: List<OrderModel>,
    myOrders: List<OrderModel>,
    openOrder: (OrderModel) -> Unit,
    serviceOrder: (OrderModel) -> Unit
) {
    val pagerState = rememberPagerState { Tabs.Size }
    var currentTab by remember { mutableIntStateOf(Tabs.Free) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedOrder by remember { mutableStateOf<OrderModel?>(null) }

    LaunchedEffect(pagerState.currentPage) {
        currentTab = pagerState.currentPage
    }

    LaunchedEffect(currentTab) {
        pagerState.animateScrollToPage(currentTab)
    }

    Column(modifier) {
        TabRow(selectedTabIndex = currentTab) {
            Tab(
                text = { Text("Свободные заказы") },
                selected = currentTab == Tabs.Free,
                onClick = { currentTab = Tabs.Free },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurface
            )
            Tab(
                text = { Text("Мои заказы") },
                selected = pagerState.currentPage == Tabs.Occupied,
                onClick = { currentTab = Tabs.Occupied },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurface
            )
        }

        HorizontalPager(
            modifier = Modifier.fillMaxSize(),
            state = pagerState
        ) { page ->
            val orders = when (page) {
                Tabs.Free -> freeOrders
                Tabs.Occupied -> myOrders
                else -> emptyList()
            }
            if (orders.isEmpty()) {
                EmptyOrderView() // Компонент для пустого списка
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    itemsIndexed(orders) { _, order ->
                        if (currentTab == Tabs.Free) {
                            OrderItem(
                                order = order,
                                onTableClick = {
                                    openOrder(order)
                                }
                              /*  onTableClick = {
                                    selectedOrder = order
                                    showDialog = true
                                }*/
                            )
                        } else {
                            OrderItem(
                                order = order,
                                onTableClick = {
                                    openOrder(order)
                                }
                            )
                        }
                    }
                }
            }
        }
        if (showDialog && selectedOrder != null) {
            ConfirmDialog(
                title = "Принять заказ",
                text = "Вы хотите принять этот заказ?",
                onConfirm = {
                    selectedOrder?.let { order ->
                        serviceOrder(order)
                    }
                    showDialog = false
                    selectedOrder = null // Сброс выбранного заказа
                },
                onDismiss = {
                    showDialog = false
                    selectedOrder = null // Сброс выбранного заказа
                }
            )
        }
    }
}

@Preview
@Composable
fun EmptyOrderView(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier.padding(16.dp),
            painter = painterResource(id = R.drawable.ic_no_tables),
            contentDescription = null
        )
        Text("Нет доступных заказов", style = MaterialTheme.typography.bodyLarge)
        // Здесь также можно добавить изображение или другие элементы UI
    }
}

@Composable
fun OrderItem(
    order: OrderModel,
    onTableClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onTableClick() }
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Заказ: ${order.note}",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

