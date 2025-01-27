package life.sochpekharoch.serenity.update

data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val updateUrl: String,
    val updateMessage: String
)

object UpdateConfig {
    // Replace these with your actual update server details
    const val UPDATE_CHECK_URL = "YOUR_UPDATE_CHECK_URL"
    const val NOTIFICATION_CHANNEL_ID = "app_update_channel"
    const val NOTIFICATION_ID = 1001
} 