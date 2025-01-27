package life.sochpekharoch.serenity.bots

import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class WellnessBot {
    private val firestore = FirebaseFirestore.getInstance()
    private val wellnessTips = listOf(
        "Remember to drink water throughout the day.",
        "Take short breaks to practice deep breathing.",
        "A 10-minute walk can boost your mood.",
        "Practice gratitude by noting three good things daily.",
        "Maintain a consistent sleep schedule for better mental health."
        // Add more tips as needed
    )

    fun createPost() {
        val tip = wellnessTips.random()
        val post = hashMapOf(
            "content" to tip,
            "type" to "BOT",
            "authorId" to "wellness_bot",
            "authorName" to "Wellness Bot",
            "timestamp" to Date(),
            "likes" to 0
        )

        firestore.collection("posts").add(post)
    }
} 