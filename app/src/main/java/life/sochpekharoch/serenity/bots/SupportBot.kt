package life.sochpekharoch.serenity.bots

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import life.sochpekharoch.serenity.models.Post
import life.sochpekharoch.serenity.models.PostType
import life.sochpekharoch.serenity.utils.Constants.BOT_AVATAR_RESOURCE
import java.util.*
import java.util.concurrent.TimeUnit

class SupportBot(
    override val botId: String,
    override val botName: String,
    override val botAvatarId: Int
) : Bot {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val postedMessages = mutableSetOf<String>()

    private val supportMessages = listOf(
        Pair(
            "Remember, our community is here for you 24/7.",
            "Serenity Community Guidelines"
        ),
        Pair(
            "Don't hesitate to reach out for help when needed.",
            "Mental Health Support Network"
        ),
        Pair(
            "Your feelings are valid and important.",
            "Mental Health Foundation"
        ),
        Pair(
            "Taking care of your mental health is a sign of strength.",
            "World Health Organization"
        ),
        Pair(
            "You're part of a caring community that understands.",
            "Serenity Support Team"
        )
    )

    private suspend fun signInBot() {
        try {
            auth.signInAnonymously().await()
            Log.d("SupportBot", "Bot signed in successfully")
        } catch (e: Exception) {
            Log.e("SupportBot", "Failed to sign in bot: ${e.message}")
            throw e
        }
    }

    override suspend fun shareContent() {
        withContext(Dispatchers.IO) {
            try {
                Log.d("SupportBot", "Starting shareContent. Auth status: ${auth.currentUser != null}")
                
                try {
                    if (auth.currentUser == null) {
                        Log.d("SupportBot", "Attempting to sign in bot...")
                        signInBot()
                    }
                } catch (e: Exception) {
                    Log.e("SupportBot", "Failed to sign in bot: ${e.message}", e)
                }

                val botDocRef = firestore.collection("bots").document(botId)
                val botDoc = botDocRef.get().await()
                
                Log.d("SupportBot", "Bot document exists: ${botDoc.exists()}")
                
                val lastPostTime = if (!botDoc.exists()) {
                    val oneHourAgo = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)
                    Log.d("SupportBot", "Creating new bot document with lastPostTime: ${Date(oneHourAgo)}")
                    botDocRef.set(mapOf("lastPostTime" to oneHourAgo)).await()
                    oneHourAgo
                } else {
                    val time = botDoc.getLong("lastPostTime") ?: 0L
                    Log.d("SupportBot", "Retrieved lastPostTime: ${Date(time)}")
                    time
                }

                val currentTime = System.currentTimeMillis()
                val timeSinceLastPost = currentTime - lastPostTime
                
                Log.d("SupportBot", """
                    Detailed timing check:
                    Current time: ${Date(currentTime)}
                    Last post time: ${Date(lastPostTime)}
                    Minutes since last post: ${TimeUnit.MILLISECONDS.toMinutes(timeSinceLastPost)}
                    Hours since last post: ${TimeUnit.MILLISECONDS.toHours(timeSinceLastPost)}
                    Should post: ${timeSinceLastPost >= TimeUnit.MINUTES.toMillis(30)}
                    Posted messages count: ${postedMessages.size}
                """.trimIndent())
                
                if (timeSinceLastPost < TimeUnit.MINUTES.toMillis(30)) {
                    Log.d("SupportBot", "Not enough time passed since last post")
                    return@withContext
                }

                // Check if bot has already posted in this run
                val lastRunTime = botDoc.getLong("lastRunTime") ?: 0L
                if (lastRunTime == currentTime) {
                    Log.d("SupportBot", "Bot already posted in this run")
                    return@withContext
                }

                val availableMessages = supportMessages.filterNot { postedMessages.contains(it.first) }
                if (availableMessages.isEmpty()) {
                    postedMessages.clear() // Reset if all messages have been used
                    Log.d("SupportBot", "Reset posted messages list")
                    return@withContext
                }

                val (message, source) = availableMessages.random()
                postedMessages.add(message)

                val post = Post(
                    id = "",
                    userId = "bot_$botId",
                    content = "$message\n\n> $source",
                    type = PostType.TEXT,
                    imageUrl = null,
                    timestamp = currentTime,
                    likes = listOf(),
                    comments = listOf(),
                    repostedBy = listOf(),
                    pollOptions = listOf(),
                    userAvatarId = botAvatarId,
                    username = botName,
                    isRepost = false,
                    repostUserId = null,
                    repostUsername = null,
                    repostUserAvatarId = null,
                    repostComment = null,
                    originalUsername = null,
                    originalUserAvatarId = null,
                    originalUserId = null
                )

                firestore.runTransaction { transaction ->
                    val postMap = post.toMap() + mapOf("avatar" to "bot_avatar")
                    
                    transaction.set(firestore.collection("posts").document(), postMap)
                    transaction.set(
                        firestore.collection("bots").document(botId),
                        mapOf(
                            "lastPostTime" to currentTime,
                            "lastRunTime" to currentTime
                        ),
                        SetOptions.merge()
                    )
                }.await()
                
                Log.d("SupportBot", "Successfully posted support message")
            } catch (e: Exception) {
                Log.e("SupportBot", "Error in shareContent", e)
                throw e
            }
        }
    }

    fun resetPostedContent() {
        postedMessages.clear()
    }

    private fun Post.toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "userId" to userId,
        "content" to content,
        "type" to type,
        "imageUrl" to imageUrl,
        "timestamp" to timestamp,
        "likes" to likes,
        "comments" to comments,
        "repostedBy" to repostedBy,
        "pollOptions" to pollOptions,
        "userAvatarId" to userAvatarId,
        "username" to username,
        "isRepost" to isRepost,
        "repostUserId" to repostUserId,
        "repostUsername" to repostUsername,
        "repostUserAvatarId" to repostUserAvatarId,
        "repostComment" to repostComment,
        "originalUsername" to originalUsername,
        "originalUserAvatarId" to originalUserAvatarId,
        "originalUserId" to originalUserId
    )
} 