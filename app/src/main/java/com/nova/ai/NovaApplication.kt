package com.nova.ai

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NovaApplication : Application() {

    companion object {
        const val VOICE_CHANNEL_ID = "nova_continuous_voice_channel"
        const val OVERLAY_CHANNEL_ID = "nova_overlay_channel"
        const val TASK_CHANNEL_ID = "nova_task_execution_channel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            val voiceChannel = NotificationChannel(
                VOICE_CHANNEL_ID,
                getString(R.string.notification_channel_voice_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_voice_desc)
                setShowBadge(false)
            }

            val overlayChannel = NotificationChannel(
                OVERLAY_CHANNEL_ID,
                getString(R.string.notification_channel_overlay_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_overlay_desc)
                setShowBadge(false)
            }

            val taskChannel = NotificationChannel(
                TASK_CHANNEL_ID,
                getString(R.string.notification_channel_task_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.notification_channel_task_desc)
            }

            notificationManager.createNotificationChannels(
                listOf(voiceChannel, overlayChannel, taskChannel)
            )
        }
    }
}
