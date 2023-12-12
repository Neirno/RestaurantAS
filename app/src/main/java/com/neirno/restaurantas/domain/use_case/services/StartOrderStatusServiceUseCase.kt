package com.neirno.restaurantas.domain.use_case.services

import android.content.Context
import android.content.Intent
import com.neirno.restaurantas.core.services.OrderStatusService
import javax.inject.Inject

class StartOrderStatusServiceUseCase @Inject constructor(
    private val context: Context
) {
    operator fun invoke(userType: String, userId: String) {
        val intent = Intent(context, OrderStatusService::class.java).apply {
            putExtra("userType", userType)
            putExtra("userId", userId)
        }
        context.startService(intent)
    }
}
