package com.neirno.restaurantas.core.services

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import com.neirno.restaurantas.core.constans.NotificationHelper
import com.neirno.restaurantas.core.constans.OrderStatus
import com.neirno.restaurantas.core.util.Response
import com.neirno.restaurantas.domain.model.OrderModel
import com.neirno.restaurantas.domain.model.TableModel
import com.neirno.restaurantas.domain.use_case.order.GetOrdersUseCase
import com.neirno.restaurantas.domain.use_case.table.GetTablesUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OrderStatusService : Service() {

    @Inject
    lateinit var getOrdersUseCase: GetOrdersUseCase
    @Inject
    lateinit var getTablesUseCase: GetTablesUseCase

    private val notificationHelper: NotificationHelper by lazy { NotificationHelper(this) }


    override fun onBind(intent: Intent?): IBinder? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        notificationHelper.createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val userType = intent.getStringExtra("userType") ?: return START_NOT_STICKY
        val userId = intent.getStringExtra("userId") ?: return START_NOT_STICKY

        val notification = notificationHelper.createNotification("Получение статусов заказов...")
        startForeground(1, notification)

        when (userType) {
            "WAITER" -> {
                observeWaiterOrderUpdates(userId)
            }
            "COOK" -> {
                observeCookOrderUpdates(userId)
            }
            else -> return START_NOT_STICKY
        }

        return START_NOT_STICKY
    }

    private fun observeWaiterOrderUpdates(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            getTablesUseCase().collect { tablesResponse ->
                when (tablesResponse) {
                    is Response.Success -> {
                        val myTables = tablesResponse.data.filter { it.servedBy == userId }
                        getOrdersForWaiter(myTables)
                    }
                    // Обработка ошибок и состояния загрузки...
                    else -> {}
                }
            }
        }
    }

    private fun getOrdersForWaiter(tables: List<TableModel>) {
        CoroutineScope(Dispatchers.IO).launch {
            getOrdersUseCase().collect { ordersResponse ->
                when (ordersResponse) {
                    is Response.Success -> {
                        val tableOrdersMap = mutableMapOf<String, MutableList<OrderModel>>()
                        ordersResponse.data.forEach { order ->
                            if (order.status != OrderStatus.COMPLETED.status && order.status != OrderStatus.CANCELLED.status) {
                                tables.find { it.tableId == order.tableId }?.let { table ->
                                    tableOrdersMap.getOrPut(table.number, ::mutableListOf).add(order)
                                } // спс чатгпт
                            }
                        }
                        updateNotificationWithTables(tableOrdersMap)
                    }
                    // Обработка ошибок и состояния загрузки...
                    else -> {}
                }
            }
        }
    }

    private fun updateNotificationWithTables(tableOrdersMap: Map<String, List<OrderModel>>) {
        val content = tableOrdersMap.entries.joinToString(separator = "\n") { (tableNumber, orders) ->
            "Стол $tableNumber: " + orders.joinToString(separator = ", ") { order ->
                "Заказ с стаутусом: (${order.status})"
            }
        }
        notificationHelper.createNotification(content)
    }


    private fun observeCookOrderUpdates(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            getOrdersUseCase().collect { response ->
                when (response) {
                    is Response.Success -> {
                        val relevantOrders = response.data.filter { order ->
                            order.takenBy.isEmpty() && order.takenBy == userId &&
                                    (order.status == OrderStatus.PENDING.status || order.status == OrderStatus.ACCEPTED.status)
                        }
                        updateNotification(relevantOrders)
                    }
                    // Обработка ошибок и состояния загрузки...
                    else -> {}
                }
            }
        }
    }


    private fun updateNotification(orders: List<OrderModel>) {
        val content = orders.joinToString(separator = "\n") { order ->
                "Заказ с статусом: ${order.status}"
            }
        notificationHelper.createNotification(content)
    }

}
