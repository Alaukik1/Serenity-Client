package life.sochpekharoch.serenity.repository

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.firestore.FirebaseFirestore
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.tasks.await
import java.util.UUID
import life.sochpekharoch.serenity.models.PostType
import life.sochpekharoch.serenity.data.Post

class PostRepository {
    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val imagesRef = storage.reference.child("images")
    private val postsCollection = firestore.collection("posts")

    suspend fun uploadImage(uri: Uri): String? {
        try {
            Log.d("UploadDebug", "Starting image upload for URI: $uri")
            
            // Generate a unique filename
            val filename = "${UUID.randomUUID()}.jpg"
            val imageRef = imagesRef.child(filename)
            
            // Upload the file
            val uploadTask = imageRef.putFile(uri).await()
            
            // Get the download URL
            val downloadUrl = imageRef.downloadUrl.await().toString()
            
            Log.d("UploadDebug", "Upload successful, download URL: $downloadUrl")
            return downloadUrl
            
        } catch (e: Exception) {
            Log.e("UploadDebug", "Error uploading image", e)
            return null
        }
    }

    suspend fun createPost(post: Post) {
        try {
            if (post.type == PostType.IMAGE) {
                Log.d("PostDebug", """
                    Creating image post:
                    - Has imageUrl: ${post.imageUrl != null}
                    - ImageUrl length: ${post.imageUrl?.length ?: 0}
                    - First 100 chars: ${post.imageUrl?.take(100)}
                """.trimIndent())
            }
            
            Log.d("PostDebug", """
                Creating post:
                - Type: ${post.type}
                - Image URL: ${post.imageUrl}
                - Content: ${post.content}
            """.trimIndent())
            
            if (post.type == PostType.IMAGE && post.imageUrl.isNullOrEmpty()) {
                Log.e("PostDebug", "Attempted to create image post without URL")
                return
            }

            // Convert post to map
            val postMap = mapOf(
                "userId" to post.userId,
                "userAvatarId" to post.userAvatarId,
                "type" to post.type,
                "content" to post.content,
                "imageUrl" to post.imageUrl,
                "timestamp" to post.timestamp,
                "options" to post.options,
                "likedBy" to post.likedBy,
                "likes" to post.likes,
                "commentsCount" to post.commentsCount
            )

            // Add to Firestore
            postsCollection.add(postMap).await()
            Log.d("PostDebug", "Post created successfully")

        } catch (e: Exception) {
            Log.e("PostDebug", "Error creating post", e)
            throw e
        }
    }
} 