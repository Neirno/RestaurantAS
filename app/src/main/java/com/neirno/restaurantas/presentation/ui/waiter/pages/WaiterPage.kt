package com.neirno.restaurantas.presentation.ui.waiter.pages

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
import com.neirno.restaurantas.domain.model.TableModel
import com.neirno.restaurantas.presentation.ui.cook.pages.EmptyOrderView

object Tabs {
    const val Free = 0
    const val Occupied = 1
    const val Size = 2
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WaiterPage(
    modifier: Modifier = Modifier,
    freeTables: List<TableModel>,
    myTables: List<TableModel>,
    openOrder: (String, String) -> Unit,
    serviceTable: (String) -> Unit
) {
    val pagerState = rememberPagerState { Tabs.Size }
    var currentTab by remember { mutableIntStateOf(Tabs.Free) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedTableId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(pagerState.currentPage) {
        currentTab = pagerState.currentPage
    }

    LaunchedEffect(currentTab) {
        pagerState.animateScrollToPage(currentTab)
    }

    Column(modifier) {
        TabRow(selectedTabIndex = currentTab) {
            Tab(
                text = { Text("Свободные столы") },
                selected = pagerState.currentPage == Tabs.Free,
                onClick = { currentTab = Tabs.Free },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurface
            )
            Tab(
                text = { Text("Занятые столы") },
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
            val tables = when (page) {
                Tabs.Free -> freeTables
                Tabs.Occupied -> myTables
                else -> emptyList()
            }
            if (tables.isEmpty()) {
                EmptyTableView() // Компонент для пустого списка
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    itemsIndexed(tables) { _, table ->
                        if (currentTab == Tabs.Free) {
                            TableItem(
                                table = table,
                                onTableClick = {
                                    selectedTableId = table.tableId
                                    showDialog = true
                                }
                            )
                        } else {
                            TableItem(
                                table = table,
                                onTableClick = {
                                    openOrder(table.tableId, table.number)
                                }
                            )
                        }
                    }
                }
            }
        }
        if (showDialog) {
            ConfirmDialog(
                title = "Принять стол",
                text = "Вы хотите принять этот стол?",
                onConfirm = {
                    selectedTableId?.let { serviceTable(it) }
                    showDialog = false
                },
                onDismiss = { showDialog = false }
            )
        }
    }
}



@Composable
fun EmptyTableView(
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
        Text("Нет доступных столов", style = MaterialTheme.typography.bodyLarge)
        // Здесь также можно добавить изображение или другие элементы UI
    }
}

@Composable
fun TableItem(
    table: TableModel,
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
                text = "Стол №${table.number}",
                style = MaterialTheme.typography.titleMedium
            )
            if (table.reservedBy.isNotEmpty()) {
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = "Заказчики: ${table.reservedBy.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
