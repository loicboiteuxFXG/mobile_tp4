package com.example.mapssages

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        // On s'assure que l'API est suffisamment récente
        // pour supporter les canaux de notification

        val channel1 = NotificationChannel(
            CHANNEL_1_ID,
            "Channel 1",
            NotificationManager.IMPORTANCE_HIGH
        )
        channel1.description = "This is Channel 1"

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel1)


        val channel2 = NotificationChannel(
            CHANNEL_2_ID,
            "Channel 2",
            NotificationManager.IMPORTANCE_MIN
        )
        channel2.description = "This is Channel 2"
        val manager = getSystemService(
            NotificationManager::class.java
        )
        val notificationManager2: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager2.createNotificationChannel(channel2)

    }

    companion object {
        const val CHANNEL_1_ID = "channel1"
        const val CHANNEL_2_ID = "channel2"
    }
}
