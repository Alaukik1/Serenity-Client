package life.sochpekharoch.serenity.update

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import life.sochpekharoch.serenity.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class CustomUpdateManager(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                UpdateConfig.NOTIFICATION_CHANNEL_ID,
                "App Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for app updates"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    suspend fun checkForUpdate() {
        try {
            val updateInfo = fetchUpdateInfo()
            if (updateInfo.versionCode > context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode.toInt()) {
                showUpdateNotification(updateInfo)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun fetchUpdateInfo(): UpdateInfo = withContext(Dispatchers.IO) {
        val response = URL(UpdateConfig.UPDATE_CHECK_URL).readText()
        val json = JSONObject(response)
        
        UpdateInfo(
            versionCode = json.getInt("versionCode"),
            versionName = json.getString("versionName"),
            updateUrl = json.getString("updateUrl"),
            updateMessage = json.getString("updateMessage")
        )
    }

    private fun showUpdateNotification(updateInfo: UpdateInfo) {
        val intent = Intent(context, UpdateDialogActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("updateUrl", updateInfo.updateUrl)
            putExtra("updateMessage", updateInfo.updateMessage)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, UpdateConfig.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Update Available")
            .setContentText("A new version (${updateInfo.versionName}) is available")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(0, "Update Now", pendingIntent)
            .build()

        notificationManager.notify(UpdateConfig.NOTIFICATION_ID, notification)
    }
} 