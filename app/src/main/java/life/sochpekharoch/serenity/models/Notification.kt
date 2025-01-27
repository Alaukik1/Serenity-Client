package life.sochpekharoch.serenity.models
import com.google.firebase.Timestamp
data class Notification(
    val title: String = "",
    val message: String = "",
    val timestamp: Timestamp? = null,
    val userId: String = ""
)