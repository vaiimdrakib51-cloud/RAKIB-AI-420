package com.nova.ai.voice.service

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.nova.ai.NovaApplication
import com.nova.ai.R
import com.nova.ai.voice.speechinput.VoiceRecognitionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ContinuousVoiceService : Service() {

    @Inject
    lateinit var voiceRecognitionManager: VoiceRecognitionManager

    override fun onCreate() {
        super.onCreate()
        voiceRecognitionManager.initialize()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification: Notification = NotificationCompat.Builder(this, NovaApplication.VOICE_CHANNEL_ID)
            .setContentTitle("NOVA Active Listening")
            .setContentText("Continuously listening for authorized voice commands...")
            .setSmallIcon(R.drawable.ic_mic)
            .setOngoing(true)
            .build()

        startForeground(1001, notification)
        voiceRecognitionManager.startListening()

        return START_STICKY
    }

    override fun onDestroy() {
        voiceRecognitionManager.stopListening()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
