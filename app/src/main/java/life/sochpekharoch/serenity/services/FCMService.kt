package life.sochpekharoch.serenity.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import life.sochpekharoch.serenity.utils.FCMTokenManager

class FCMService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCMService", "Received new FCM token")
        FCMTokenManager(this).saveFCMToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCMService", "Received FCM message: ${message.data}")
        // Handle the incoming message
    }
} 