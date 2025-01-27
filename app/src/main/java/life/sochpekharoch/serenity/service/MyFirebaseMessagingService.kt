package life.sochpekharoch.serenity.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import life.sochpekharoch.serenity.MainActivity
import life.sochpekharoch.serenity.R

class MyFirebaseMessagingService : FirebaseMessagingService() {
    
    override fun onCreate() {
        super.onCreate()
        Log.d("FCM", "FirebaseMessagingService created")
        createNotificationChannel()
    }
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM", "=== Message Received ===")
        Log.d("FCM", "From: ${remoteMessage.from}")
        Log.d("FCM", "Message data: ${remoteMessage.data}")
        Log.d("FCM", "Raw notification object: ${remoteMessage.notification}")
        Log.d("FCM", "Message priority: ${remoteMessage.priority}")
        Log.d("FCM", "Original Priority: ${remoteMessage.originalPriority}")
        
        try {
            createNotificationChannel()
            
            // Extract notification data
            val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "New Message"
            val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: ""
            
            Log.d("FCM", "Processing notification - Title: $title, Body: $body")
            
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            Log.d("FCM", "Current user ID when message received: $userId")

            // Show system notification
            showNotification(title, body)

            // Store in Firestore
            if (userId != null) {
                val notification = hashMapOf(
                    "userId" to userId,
                    "title" to title,
                    "body" to body,
                    "timestamp" to System.currentTimeMillis(),
                    "isRead" to false
                )
                
                Log.d("FCM", "Attempting to store notification: $notification")

                FirebaseFirestore.getInstance()
                    .collection("notifications")
                    .add(notification)
                    .addOnSuccessListener {
                        Log.d("FCM", "Successfully stored notification in Firestore with ID: ${it.id}")
                    }
                    .addOnFailureListener { e ->
                        Log.e("FCM", "Failed to store notification in Firestore", e)
                        e.printStackTrace()
                    }
            } else {
                Log.e("FCM", "Cannot store notification - user not signed in")
            }
        } catch (e: Exception) {
            Log.e("FCM", "Error in onMessageReceived", e)
            e.printStackTrace()
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "=== New Token Generated ===")
        Log.d("FCM", "Token: $token")
    }

    private fun createNotificationChannel() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "Serenity Notifications"
                val descriptionText = "Notifications from Serenity"
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
                Log.d("FCM", "Notification channel created: $CHANNEL_ID")
            }
        } catch (e: Exception) {
            Log.e("FCM", "Error creating notification channel", e)
            e.printStackTrace()
        }
    }

    private fun showNotification(title: String, message: String) {
        try {
            Log.d("FCM", "Showing notification - Title: $title, Message: $message")
            
            // Create intent for notification tap action
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE
            )

            // Build notification
            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                
            Log.d("FCM", "Built notification with channel: $CHANNEL_ID")

            // Show notification
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
            Log.d("FCM", "Notification displayed successfully")
            
            // Log available channels
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channels = notificationManager.notificationChannels
                Log.d("FCM", "Available notification channels: ${channels.map { it.id }}")
            }
        } catch (e: Exception) {
            Log.e("FCM", "Error showing notification", e)
            e.printStackTrace()
        }
    }

    companion object {
        private const val CHANNEL_ID = "serenity_notifications"
    }
} 