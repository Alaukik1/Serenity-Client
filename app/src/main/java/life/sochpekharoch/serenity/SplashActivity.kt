package life.sochpekharoch.serenity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.delay
import life.sochpekharoch.serenity.viewmodels.CommunityViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.supervisorScope

class SplashActivity : ComponentActivity() {
    private val viewModel: CommunityViewModel by viewModels()
    private val db = FirebaseFirestore.getInstance()

    private suspend fun migratePostLikes() {
        try {
            Log.d("SplashActivity", "Starting likes migration...")
            val postsRef = db.collection("posts")
            val snapshot = postsRef.get().await()
            
            var migratedCount = 0
            snapshot.documents.forEach { doc ->
                val likes = doc.get("likes")
                if (likes is Long) {
                    Log.d("SplashActivity", "Migrating post ${doc.id}, likes: $likes")
                    doc.reference.update("likes", listOf<String>()).await()
                    migratedCount++
                }
            }
            Log.d("SplashActivity", "Migration completed. Migrated $migratedCount posts")
        } catch (e: Exception) {
            Log.e("SplashActivity", "Error migrating likes", e)
            // Don't throw the error - just log it and continue
        }
    }

    private suspend fun migrateBotAvatars() = withContext(Dispatchers.IO) {
        try {
            Log.d("SplashActivity", "Starting bot avatar migration...")
            Log.d("SplashActivity", "Bot avatar resource ID: ${R.drawable.bot_avatar}")
            
            // Query only bot posts directly instead of all posts
            val botPosts = db.collection("posts")
                .whereGreaterThanOrEqualTo("userId", "bot_")
                .whereLessThanOrEqualTo("userId", "bot_\uf8ff")
                .get()
                .await()
            
            Log.d("SplashActivity", "Found ${botPosts.size()} bot posts")
            var migratedCount = 0
            
            // Process in smaller chunks to avoid batch limits
            botPosts.documents.chunked(30).forEachIndexed { index, chunk ->
                try {
                    val batch = db.batch()
                    var batchCount = 0
                    
                    chunk.forEach { doc ->
                        val userId = doc.getString("userId") ?: ""
                        Log.d("SplashActivity", "Updating bot post ${doc.id} for user $userId")
                        
                        // Always update bot avatars to ensure consistency
                        batch.update(doc.reference, mapOf(
                            "avatar" to "bot_avatar",
                            "userAvatarId" to R.drawable.bot_avatar,
                            "isBot" to true
                        ))
                        batchCount++
                    }
                    
                    if (batchCount > 0) {
                        batch.commit().await()
                        migratedCount += batchCount
                        Log.d("SplashActivity", "Committed batch $index: updated $batchCount posts")
                    }
                } catch (e: Exception) {
                    Log.e("SplashActivity", "Error processing batch $index", e)
                    // Continue with next batch even if this one fails
                }
            }
            
            Log.d("SplashActivity", "Bot avatar migration completed. Updated $migratedCount posts")
        } catch (e: Exception) {
            Log.e("SplashActivity", "Error during bot avatar migration", e)
            throw e
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Add this check
        try {
            val resourceId = R.drawable.bot_avatar
            Log.d("SplashActivity", "Bot avatar resource ID: $resourceId")
            
            // Add these lines to verify the resource
            val resourceName = resources.getResourceName(resourceId)
            val resourceType = resources.getResourceTypeName(resourceId)
            val resourceEntryName = resources.getResourceEntryName(resourceId)
            
            Log.d("SplashActivity", """
                Bot Avatar Resource Details:
                - Full Name: $resourceName
                - Type: $resourceType
                - Entry Name: $resourceEntryName
            """.trimIndent())
            
            getDrawable(resourceId)?.let {
                Log.d("SplashActivity", "Successfully loaded bot avatar drawable")
            } ?: Log.e("SplashActivity", "Failed to load bot avatar drawable")
        } catch (e: Exception) {
            Log.e("SplashActivity", "Error checking bot avatar resource", e)
        }
        
        // Get FCM token
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.d("FCM", "Current FCM Token: $token")
                    
                    // Store token in Firestore for later use
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    if (userId != null) {
                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(userId)
                            .set(
                                mapOf("fcmToken" to token),
                                SetOptions.merge()  // This will merge with existing data
                            )
                            .addOnSuccessListener {
                                Log.d("FCM", "Token stored in Firestore")
                            }
                            .addOnFailureListener { e ->
                                Log.e("FCM", "Failed to store token", e)
                            }
                    }
                } else {
                    Log.e("FCM", "Failed to get FCM token", task.exception)
                }
            }
        
        FirebaseMessaging.getInstance().subscribeToTopic("all_users")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FCM", "Subscribed to all_users topic")
                } else {
                    Log.e("FCM", "Failed to subscribe to topic", task.exception)
                }
            }
        
        val currentUser = FirebaseAuth.getInstance().currentUser
        Log.d("Auth", "Current user in SplashActivity: ${currentUser?.uid}")
        
        setContent {
            SplashScreen()
        }

        lifecycleScope.launch {
            try {
                Log.d("SplashActivity", "Starting app initialization...")
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                
                supervisorScope { // Use supervisorScope to prevent child coroutine failures from cancelling parent
                    try {
                        Log.d("SplashActivity", "Starting migrations...")
                        migratePostLikes()
                        migrateBotAvatars()
                        
                        Log.d("SplashActivity", "Starting app initialization...")
                        viewModel.initializeApp()
                        
                        // Wait for posts to be loaded
                        withTimeout(20000) {
                            while (!viewModel.postsLoaded.value) {
                                Log.d("SplashActivity", "Waiting for posts to load...")
                                delay(100)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("SplashActivity", "Error during initialization steps", e)
                        // Don't rethrow - continue with app startup
                    }
                }
                
                Log.d("SplashActivity", "Initialization completed successfully")
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
            } catch (e: Exception) {
                Log.e("SplashActivity", "Fatal initialization error", e)
                // Show error to user
                Toast.makeText(this@SplashActivity, 
                    "Error initializing app. Please try again.", 
                    Toast.LENGTH_LONG).show()
            }
        }
    }
}

@Composable
private fun SplashScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.splash_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Progress Indicator
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 150.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(36.dp),
                color = Color.Black,
                strokeWidth = 5.dp,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
} 