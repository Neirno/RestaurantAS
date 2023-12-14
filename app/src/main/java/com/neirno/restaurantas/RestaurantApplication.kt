package com.neirno.restaurantas

import android.app.Application
import com.neirno.restaurantas.core.constans.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RestaurantApplication: Application() {

    private val notificationHelper: NotificationHelper = NotificationHelper(this)

    override fun onCreate() {
        super.onCreate()
        notificationHelper.createNotificationChannel()
    }
}