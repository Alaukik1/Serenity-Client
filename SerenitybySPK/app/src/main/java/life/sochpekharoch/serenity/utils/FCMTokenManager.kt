package life.sochpekharoch.serenity.utils

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class FCMTokenManager(private val context: Context) {
    companion object {
        private const val TAG = "FCMTokenManager"
        private const val PREF_NAME = "fcm_prefs"
        private const val KEY_FCM_TOKEN = "fcm_token"
    }

    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    suspend fun getFCMToken(): String? {
        // First try to get cached token
        var token = prefs.getString(KEY_FCM_TOKEN, null)
        
        if (token == null) {
            try {
                // If no cached token, get new token from Firebase
                token = FirebaseMessaging.getInstance().token.await()
                Log.d(TAG, "Token length: ${token?.length}")
                Log.d(TAG, "Token first 10 chars: ${token?.take(10)}...")
                // Cache the token
                saveFCMToken(token)
                Log.d(TAG, "New FCM token generated: $token")
            } catch (e: Exception) {
                Log.e(TAG, "Error getting FCM token", e)
                e.printStackTrace()
                return null
            }
        }
        
        return token
    }

    private fun saveFCMToken(token: String) {
        prefs.edit().putString(KEY_FCM_TOKEN, token).apply()
        Log.d(TAG, "FCM token saved to preferences")
    }

    fun clearFCMToken() {
        prefs.edit().remove(KEY_FCM_TOKEN).apply()
        Log.d(TAG, "FCM token cleared from preferences")
    }
} 