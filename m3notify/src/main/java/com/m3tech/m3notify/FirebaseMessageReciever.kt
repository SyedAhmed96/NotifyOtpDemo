package com.m3tech.m3notify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessageReceiver : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.notification != null) {
            sendNotification(
                remoteMessage.notification?.title.toString(),
                remoteMessage.notification?.body.toString()
            )
            AppSingleton.updateData(remoteMessage.notification?.body.toString())
        }

        Log.d(
            "FCMtoken",
            "remoteMessage.getNotification() title: ${remoteMessage.notification?.title} body: ${remoteMessage.notification?.body} notification: ${remoteMessage.notification}"
        )

    }

    private fun sendNotification(title: String, message: String) {
        val notificationBuilder = NotificationCompat.Builder(this, packageName)
            .setSmallIcon(android.R.drawable.ic_notification_overlay)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(packageName, title, NotificationManager.IMPORTANCE_HIGH).apply {
                    description = message
                }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }

    override fun onNewToken(token: String) {
        // Handle the new token if needed
        Log.i("FCMtoken", "Refreshed token: $token")
    }
}
