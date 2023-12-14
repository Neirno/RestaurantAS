package com.neirno.restaurantas.core.services

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import com.neirno.restaurantas.core.constans.NotificationHelper
import com.neirno.restaurantas.core.constans.OrderStatus
import com.neirno.restaurantas.core.constans.UserType
import com.neirno.restaurantas.core.util.Response
import com.neirno.restaurantas.domain.model.OrderModel
import com.neirno.restaurantas.domain.model.TableModel
import com.neirno.restaurantas.domain.use_case.order.GetOrdersUseCase
import com.neirno.restaurantas.domain.use_case.table.GetTablesUseCase
import com.neirno.restaurantas.domain.use_case.user.GetUserStatusUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OrderStatusService : Service() {

    @Inject
    lateinit var getOrdersUseCase: GetOrdersUseCase
    @Inject
    lateinit var getTablesUseCase: GetTablesUseCase
    @Inject
    lateinit var getUserStatusUseCase: GetUserStatusUseCase

    private val notificationHelper: NotificationHelper by lazy { NotificationHelper(this) }


    override fun onBind(intent: Intent?): IBinder? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        Log.i("Service", "Start Service")
        //notificationHelper.createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val userType = intent.getStringExtra("userType") ?: return START_NOT_STICKY
        val userId = intent.getStringExtra("userId") ?: return START_NOT_STICKY
        observeUserStatus(userId)

        val notification = notificationHelper.createNotification("Получение статусов заказов...", 1)
        startForeground(1, notification)
        Log.i("Service", "onStartCommand")
        CoroutineScope(Dispatchers.IO).launch {
            Log.i("Service", "Корутина запущена")
            delay(1000)
            Log.i("Service", "Корутина завершена")
        }

        when (UserType.fromString(userType)) {
             UserType.WAITER -> {
                observeWaiterOrderUpdates(userId)
            }
            UserType.COOK -> {
                observeCookOrderUpdates(userId)
            }
            else -> return START_NOT_STICKY
        }

        return START_STICKY
    }

    private fun observeUserStatus(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            getUserStatusUseCase(userId).collect { response ->
                when (response) {
                    is Response.Success -> {
                        if (!response.data) {
                            Log.i("Service", "Пользователь не работает. Останавливаю сервис.")
                            clearAllNotifications()
                            stopSelf()
                        }
                    }
                    is Response.Error -> Log.e("Service", "Ошибка получения статуса пользователя: ${response.msg}")
                    else -> {}
                }
            }
        }
    }

    private fun observeWaiterOrderUpdates(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            getTablesUseCase().collect { tablesResponse ->
                when (tablesResponse) {
                    is Response.Success -> {
                        val myTables = tablesResponse.data.filter { it.servedBy == userId }
                        Log.i("Service", myTables.toString())
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
                        Log.i("Service", "start waiter filtering")
                        updateNotificationWithTables(tableOrdersMap)
                    }
                    // Обработка ошибок и состояния загрузки...
                    else -> {}
                }
            }
        }
    }

    private fun updateNotificationWithTables(tableOrdersMap: Map<String, List<OrderModel>>) {
        tableOrdersMap.forEach { (tableNumber, orders) ->
            val content = "Стол $tableNumber: " + orders.joinToString(separator = ", ") { order ->
                "Заказ с стаутусом: (${order.status})"
            }
            // Здесь создается уведомление для каждого стола.
            val notification = notificationHelper.createNotification(content, 1)
            val notificationId = tableNumber.hashCode() // Уникальный ID для каждого уведомления
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            Log.i("Service", "Create notif")
            notificationManager.notify(notificationId, notification)
        }
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
        orders.forEach { order ->
            val content = "Заказ с статусом: ${order.status}"
            // Здесь создается уведомление для каждого заказа.
            val notification = notificationHelper.createNotification(content, 1)
            val notificationId = order.orderId.hashCode() // Уникальный ID для каждого уведомления
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(notificationId, notification)
        }
    }

    private fun clearAllNotifications() {
        val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

}
