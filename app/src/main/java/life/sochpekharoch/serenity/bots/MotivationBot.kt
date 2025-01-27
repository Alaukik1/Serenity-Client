package life.sochpekharoch.serenity.bots

import com.google.firebase.firestore.FirebaseFirestore
import life.sochpekharoch.serenity.utils.Constants.BOT_AVATAR_RESOURCE
import java.util.*

class MotivationBot(
    private val botId: String = "motivation_bot",
    internal val botName: String = "Serenebot Motivation",
    private val botAvatarId: Int = BOT_AVATAR_RESOURCE
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val motivationalQuotes = listOf(
        "Every day is a new beginning.",
        "You are stronger than you know.",
        "Your mental health matters.",
        "It's okay to take things one step at a time.",
        "You are not alone in this journey."
        // Add more quotes as needed
    )

    fun createPost() {
        val quote = motivationalQuotes.random()
        val post = hashMapOf(
            "content" to quote,
            "type" to "BOT",
            "userId" to "bot_$botId",
            "username" to botName,
            "avatar" to "bot_avatar",
            "userAvatarId" to botAvatarId,
            "timestamp" to Date(),
            "likes" to 0,
            "likedBy" to listOf<String>(),
            "commentsCount" to 0
        )

        firestore.collection("posts").add(post)
    }
} 