package life.sochpekharoch.serenity.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import life.sochpekharoch.serenity.MainActivity
import life.sochpekharoch.serenity.R

class MyFirebaseMessagingService : FirebaseMessagingService() {
    
    override fun onCreate() {
        super.onCreate()
        Log.d("FCM", "FirebaseMessagingService created")
    }
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d("FCM", "=== Message Received ===")
        Log.d("FCM", "From: ${remoteMessage.from}")
        Log.d("FCM", "Data: ${remoteMessage.data}")
        Log.d("FCM", "Notification Title: ${remoteMessage.notification?.title}")
        Log.d("FCM", "Notification Body: ${remoteMessage.notification?.body}")
        
        try {
            // Create notification channel for Android O and above
            createNotificationChannel()

            // Get message data
            val title = remoteMessage.notification?.title ?: "New Notification"
            val message = remoteMessage.notification?.body ?: ""

            // Show notification
            showNotification(title, message)
            Log.d("FCM", "Notification displayed successfully")
        } catch (e: Exception) {
            Log.e("FCM", "Error showing notification", e)
            e.printStackTrace()
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "=== New Token Generated ===")
        Log.d("FCM", "Token: $token")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Serenity Notifications"
            val descriptionText = "Notifications from Serenity"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(title: String, message: String) {
        // Create intent for notification tap action
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // Show notification
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        private const val CHANNEL_ID = "serenity_notifications"
    }
} 