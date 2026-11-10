package com.nova.ai.device.notifications

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class NotificationIntercept(
    val packageName: String,
    val title: String,
    val content: String
)

class NotificationListenerService : NotificationListenerService() {

    companion object {
        private val _notifications = MutableSharedFlow<NotificationIntercept>(extraBufferCapacity = 10)
        val notifications: SharedFlow<NotificationIntercept> = _notifications.asSharedFlow()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn?.notification?.extras?.let { extras ->
            val title = extras.getString("android.title") ?: ""
            val text = extras.getCharSequence("android.text")?.toString() ?: ""
            val pkg = sbn.packageName ?: ""

            if (title.isNotBlank() || text.isNotBlank()) {
                _notifications.tryEmit(NotificationIntercept(packageName = pkg, title = title, content = text))
            }
        }
    }
}
