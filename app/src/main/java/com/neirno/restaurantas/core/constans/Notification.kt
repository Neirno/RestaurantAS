package com.neirno.restaurantas.core.constans


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.neirno.restaurantas.R


class NotificationHelper(private val context: Context) {

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = Constants.CHANNEL_NAME
            val descriptionText = Constants.CHANNEL_DESCRIPTION
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(Constants.CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createNotification(content: String, groupId: Int? = null): Notification {
        val builder = NotificationCompat.Builder(context, Constants.CHANNEL_ID)
            .setContentTitle("Order Status Update")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_logo)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        groupId?.let {
            builder.setGroup(Constants.NOTIFICATION_GROUP_KEY + it)
        }

        return builder.build()
    }

}
