package life.sochpekharoch.serenity

import android.app.Application
import android.content.Intent
import android.os.Build
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import life.sochpekharoch.serenity.services.ContentBotService

class SerenityApplication : Application() {
    private lateinit var contentBotService: ContentBotService

    override fun onCreate() {
        super.onCreate()
        try {
            Log.d("Firebase", "Starting Firebase initialization")
            if (FirebaseApp.getApps(this).isEmpty()) {
                val app = FirebaseApp.initializeApp(this)
                Log.d("Firebase", "Firebase initialized with name: ${app?.name}")
            } else {
                Log.d("Firebase", "Firebase already initialized")
            }
            
            // Test Firebase Auth
            val auth = FirebaseAuth.getInstance()
            Log.d("Firebase", "Firebase Auth instance obtained: ${auth != null}")
        } catch (e: Exception) {
            Log.e("Firebase", "Failed to initialize Firebase", e)
            e.printStackTrace()
        }

        // Start the bot service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, ContentBotService::class.java))
        } else {
            startService(Intent(this, ContentBotService::class.java))
        }
    }
}