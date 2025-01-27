package life.sochpekharoch.serenity.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.*
import life.sochpekharoch.serenity.bots.ContentBot
import life.sochpekharoch.serenity.bots.SupportBot
import java.util.concurrent.TimeUnit
import android.util.Log
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.os.Build
import androidx.core.app.NotificationCompat
import life.sochpekharoch.serenity.R
import android.content.pm.ServiceInfo
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import life.sochpekharoch.serenity.utils.Constants.BOT_AVATAR_RESOURCE
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ContentBotService : Service() {
    companion object {
        const val BOT_AVATAR_ID = -1
        const val POST_INTERVAL = 30L
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "bot_service_channel"
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val mutex = Mutex()
    private val bots = listOf(
        SupportBot(
            botId = "support_bot",
            botName = "Serenity Support",
            botAvatarId = BOT_AVATAR_RESOURCE
        ).also {
            Log.d("ContentBotService", "Support bot avatar: ${BOT_AVATAR_RESOURCE}")
        },
        ContentBot(
            botId = "mindful_bot",
            botName = "Serenebot Mindful",
            botAvatarId = BOT_AVATAR_RESOURCE,
            contentList = ContentBot.DAILY_PRACTICES
        ).also {
            Log.d("ContentBotService", "Mindful bot avatar: ${BOT_AVATAR_RESOURCE}")
        },
        ContentBot(
            botId = "motivation_bot",
            botName = "Serenebot Motivation",
            botAvatarId = BOT_AVATAR_RESOURCE,
            contentList = ContentBot.MOTIVATION_QUOTES
        ).also {
            Log.d("ContentBotService", "Motivation bot avatar: ${BOT_AVATAR_RESOURCE}")
        },
        ContentBot(
            botId = "wellness_bot",
            botName = "Serenebot Wellness",
            botAvatarId = BOT_AVATAR_RESOURCE,
            contentList = ContentBot.WELLNESS_TIPS
        ).also {
            Log.d("ContentBotService", "Wellness bot avatar: ${BOT_AVATAR_RESOURCE}")
        }
    )

    private val firestore = FirebaseFirestore.getInstance()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID, 
                createNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, createNotification())
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startBotService()
        return START_STICKY
    }

    private fun startBotService() {
        scope.launch {
            while (isActive) {
                try {
                    mutex.withLock {
                        var anyBotPosted = false
                        
                        // Stagger bot posts
                        bots.forEachIndexed { index, bot ->
                            try {
                                // Add delay between each bot check
                                delay(TimeUnit.MINUTES.toMillis(2L * index))
                                
                                val botDoc = firestore.collection("bots")
                                    .document(when(bot) {
                                        is ContentBot -> bot.botId
                                        is SupportBot -> bot.botId
                                        else -> ""
                                    })
                                    .get()
                                    .await()

                                val lastPostTime = botDoc.getLong("lastPostTime") ?: 0L
                                val currentTime = System.currentTimeMillis()
                                val timeSinceLastPost = currentTime - lastPostTime

                                if (timeSinceLastPost >= TimeUnit.MINUTES.toMillis(POST_INTERVAL)) {
                                    when (bot) {
                                        is ContentBot -> bot.shareContent()
                                        is SupportBot -> bot.shareContent()
                                    }
                                    anyBotPosted = true
                                    Log.d("ContentBotService", "Bot ${bot.botId} posted after ${TimeUnit.MILLISECONDS.toMinutes(timeSinceLastPost)} minutes")
                                } else {
                                    Log.d("ContentBotService", "Bot ${bot.botId} waiting, last post was ${TimeUnit.MILLISECONDS.toMinutes(timeSinceLastPost)} minutes ago")
                                }
                            } catch (e: Exception) {
                                Log.e("ContentBotService", "Error with bot post", e)
                            }
                        }
                        
                        if (!anyBotPosted) {
                            Log.d("ContentBotService", "No bots posted content")
                        }
                    }

                    delay(TimeUnit.MINUTES.toMillis(POST_INTERVAL))
                } catch (e: Exception) {
                    Log.e("ContentBotService", "Error in bot service", e)
                    delay(TimeUnit.MINUTES.toMillis(5))
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Bot Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): android.app.Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Serenity Bot Service")
            .setContentText("Managing wellness content")
            .setSmallIcon(R.drawable.ic_notification)
            .setSilent(true)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
} 