package life.sochpekharoch.serenity.bots

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import life.sochpekharoch.serenity.data.Post
import life.sochpekharoch.serenity.models.PostType
import life.sochpekharoch.serenity.utils.Constants.BOT_AVATAR_RESOURCE
import java.util.*
import java.util.concurrent.TimeUnit

class ContentBot(
    override val botId: String,
    override val botName: String,
    override val botAvatarId: Int,
    private val contentList: List<Pair<String, String>>
) : Bot {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val postedContent = mutableSetOf<String>()

    private suspend fun signInBot() {
        try {
            // Sign in anonymously for bot operations
            auth.signInAnonymously().await()
            Log.d("ContentBot", "Bot $botName signed in successfully")
        } catch (e: FirebaseAuthException) {
            Log.e("ContentBot", "Failed to sign in bot $botName: ${e.message}")
            throw e
        }
    }

    override suspend fun shareContent() {
        withContext(Dispatchers.IO) {
            try {
                // Try to sign in if not already signed in
                try {
                    if (auth.currentUser == null) {
                        signInBot()
                        Log.d("ContentBot", "Bot $botName signed in successfully with ID: ${auth.currentUser?.uid}")
                    }
                } catch (e: Exception) {
                    Log.e("ContentBot", "Failed to sign in bot $botName: ${e.message}")
                }

                // Get or create bot document
                val botDocRef = firestore.collection("bots").document(botId)
                val botDoc = botDocRef.get().await()
                
                val lastPostTime = if (!botDoc.exists()) {
                    val oneHourAgo = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)
                    botDocRef.set(mapOf("lastPostTime" to oneHourAgo)).await()
                    oneHourAgo
                } else {
                    botDoc.getLong("lastPostTime") ?: 0L
                }

                val currentTime = System.currentTimeMillis()
                val timeSinceLastPost = currentTime - lastPostTime
                
                Log.d("ContentBot", """
                    Bot: $botName
                    Last post: ${Date(lastPostTime)}
                    Minutes since last post: ${TimeUnit.MILLISECONDS.toMinutes(timeSinceLastPost)}
                    Should post: ${timeSinceLastPost >= TimeUnit.MINUTES.toMillis(30)}
                """.trimIndent())
                
                if (timeSinceLastPost < TimeUnit.MINUTES.toMillis(30)) {
                    Log.d("ContentBot", "Not enough time passed since last post for $botName")
                    return@withContext
                }

                // Check if bot has already posted in this run
                val lastRunTime = botDoc.getLong("lastRunTime") ?: 0L
                if (lastRunTime == currentTime) {
                    Log.d("ContentBot", "Bot $botName already posted in this run")
                    return@withContext
                }

                // Get unposted content
                val availableContent = contentList.filterNot { postedContent.contains(it.first) }

                if (availableContent.isEmpty()) {
                    Log.d("ContentBot", "Bot $botName has no new content to share")
                    return@withContext
                }

                // Get random unposted content
                val (content, source) = availableContent.random()
                postedContent.add(content)

                val post = Post(
                    userId = "bot_$botId",
                    userAvatarId = botAvatarId,
                    type = PostType.TEXT,
                    content = "$content\n\n> $source",
                    timestamp = currentTime,
                    likes = 0,
                    likedBy = listOf(),
                    commentsCount = 0,
                    options = null,
                    username = botName,
                    avatar = "bot_avatar",
                    imageUrl = null
                ).also {
                    Log.e("BOT_DEBUG", """
                        Creating bot post:
                        - Bot Name: $botName
                        - User ID: bot_$botId
                        - Username in post: ${it.username}
                        - Avatar ID: $botAvatarId
                        - Content: ${it.content.take(50)}...
                    """.trimIndent())
                }

                // Create post and update last post time and run time
                firestore.runTransaction { transaction ->
                    transaction.set(firestore.collection("posts").document(), post)
                    transaction.set(
                        firestore.collection("bots").document(botId),
                        mapOf(
                            "lastPostTime" to currentTime,
                            "lastRunTime" to currentTime
                        ),
                        SetOptions.merge()
                    )
                }.await()
                
                Log.d("ContentBot", "Bot $botName successfully posted content")
            } catch (e: Exception) {
                Log.e("ContentBot", "Error in shareContent", e)
                throw e
            }
        }
    }

    fun resetPostedContent() {
        postedContent.clear()  // Clear the set of posted content
    }

    companion object {
        val WELLNESS_TIPS = listOf(
            Pair(
                "Remember to take deep breaths throughout your day. Deep breathing can help reduce stress and anxiety. Try the 4-7-8 technique: inhale for 4 seconds, hold for 7, exhale for 8. 🫁 #SelfCare #MentalHealth",
                "Source: Dr. Andrew Weil, University of Arizona Center for Integrative Medicine"
            ),
            
            Pair(
                "Start your day with positive affirmations! Look in the mirror and say three things you like about yourself. You are worthy, you are capable, you are enough. ✨ #Positivity #SelfLove",
                "Source: National Alliance on Mental Illness (NAMI)"
            ),
            
            Pair(
                "Mindfulness Tip: When feeling overwhelmed, try the 5-4-3-2-1 grounding technique. Name 5 things you can see, 4 you can touch, 3 you can hear, 2 you can smell, and 1 you can taste. 🧘‍♀️ #Mindfulness",
                "Source: Mayo Clinic - Anxiety Management Techniques"
            ),
            
            Pair(
                "Regular exercise can help reduce anxiety and depression. Even a 30-minute walk can boost your mood by releasing endorphins, your body's natural mood lifters. 🚶‍♂️ #MentalHealth #Exercise",
                "Source: CDC (Centers for Disease Control and Prevention)"
            ),
            
            Pair(
                "Create a bedtime routine: Try to go to bed and wake up at the same time every day. This helps regulate your body's natural sleep-wake cycle. 😴 #SleepHygiene",
                "Source: NHS (National Health Service, UK)"
            ),
            
            Pair(
                "Practice self-compassion: Talk to yourself as you would to a friend. Being kind to yourself during difficult times reduces stress and improves emotional resilience. 💝 #SelfCompassion",
                "Source: Psychology Today"
            )
        )

        val MOTIVATION_QUOTES = listOf(
            Pair(
                "\"Your mental health is a priority. Your happiness is essential. Your self-care is a necessity.\" Take time for yourself today. 🌟",
                "Source: World Health Organization (WHO)"
            ),
            
            Pair(
                "Small progress is still progress. Celebrate your tiny victories - they add up to big changes! 🌱 #Growth #Progress",
                "Source: American Psychological Association (APA)"
            ),
            
            Pair(
                "It's okay to not be okay. Reaching out for help is a sign of strength, not weakness. 💪 #MentalHealthAwareness",
                "Source: Mental Health Foundation"
            ),
            
            Pair(
                "Recovery is not linear. Some days will be easier, some days will be harder. What matters is that you keep moving forward. 🌱 #Recovery #Hope",
                "Source: Beyond Blue (Australian Mental Health Organization)"
            ),
            
            Pair(
                "Your mental health is just as important as your physical health. Taking care of your mind is not a luxury, it's a necessity. 🧠 #MentalHealthMatters",
                "Source: National Institute of Mental Health (NIMH)"
            ),
            
            Pair(
                "Every small step counts. Building healthy habits takes time, but each positive choice you make contributes to your overall wellbeing. ✨ #Progress",
                "Source: CAMH (Centre for Addiction and Mental Health)"
            )
        )

        val DAILY_PRACTICES = listOf(
            Pair(
                "Today's Self-Care Challenge: Take a 10-minute walk outside. Nature has healing properties for our mental well-being. 🌿 #SelfCare",
                "Source: Harvard Medical School - Mind & Mood"
            ),
            
            Pair(
                "Journal Prompt: Write down three things you're grateful for today. Practicing gratitude can boost mood and reduce anxiety. 📝 #Gratitude",
                "Source: Positive Psychology Institute"
            ),
            
            Pair(
                "Evening Routine Tip: Try disconnecting from screens 1 hour before bed. Read a book, meditate, or practice gentle stretching instead. 😴 #WellnessTips",
                "Source: National Sleep Foundation"
            ),
            
            Pair(
                "Try the 3-3-3 rule for anxiety: Name 3 things you can see, 3 things you can hear, and move 3 parts of your body. This simple exercise can help bring you back to the present moment. 🌟 #AnxietyTips",
                "Source: Psychology Today"
            ),
            
            Pair(
                "Schedule 'worry time': Set aside 15 minutes each day to write down your worries. When worries come up outside this time, note them down for later. This helps contain anxiety and makes it more manageable. 📝 #AnxietyManagement",
                "Source: NHS (National Health Service, UK)"
            ),
            
            Pair(
                "Practice the PLEASE skill: treat PhysicaL illness, Eat healthy, Avoid mood-altering substances, Sleep well, and Exercise. Taking care of your body helps maintain emotional balance. 💪 #SelfCare",
                "Source: CAMH (Centre for Addiction and Mental Health)"
            ),
            
            Pair(
                "Create a 'comfort box' with items that engage your senses: a soft blanket, calming music, favorite photos, scented candle, and comforting snacks. Use it during difficult moments. 📦 #CopingSkills",
                "Source: Beyond Blue (Australian Mental Health Organization)"
            ),
            
            Pair(
                "Set SMART goals for your mental health: Specific, Measurable, Achievable, Relevant, and Time-bound. Example: 'I will meditate for 5 minutes every morning this week.' 🎯 #MentalHealthGoals",
                "Source: CDC (Centers for Disease Control and Prevention)"
            ),
            
            Pair(
                "Practice active listening in your relationships: Focus fully on the speaker, avoid interrupting, and reflect back what you've heard. This builds stronger connections and support systems. 👥 #HealthyRelationships",
                "Source: National Institute of Mental Health (NIMH)"
            )
        )
    }
} 