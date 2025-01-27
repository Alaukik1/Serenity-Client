package life.sochpekharoch.serenity.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import life.sochpekharoch.serenity.models.Post
import java.util.UUID

class FirebaseRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val postsCollection = firestore.collection("posts")
    private val usersCollection = firestore.collection("users")

    suspend fun createPost(content: String, imageUri: Uri? = null) {
        val currentUser = auth.currentUser ?: throw Exception("User not logged in")
        
        val imageUrl = imageUri?.let { uploadImage(it) }
        
        val post = Post(
            id = "", // Firestore will generate this
            userId = currentUser.uid,
            content = content,
            imageUrl = imageUrl,
            timestamp = System.currentTimeMillis(),
            likes = listOf(),
            comments = listOf(),
            repostedBy = listOf(),
            pollOptions = listOf(),
            userAvatarId = 1, // Set default avatar or get from user profile
            username = currentUser.displayName ?: "Anonymous"
        )

        try {
            firestore.collection("posts")
                .add(post)
                .await()
        } catch (e: Exception) {
            throw e
        }
    }

    private suspend fun uploadImage(imageUri: Uri): String {
        try {
            val filename = "post_images/${UUID.randomUUID()}"
            val imageRef = storage.reference.child(filename)
            
            val uploadTask = imageRef.putFile(imageUri).await()
            
            return uploadTask.storage.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error uploading image: ${e.message}")
            throw e
        }
    }

    suspend fun getPosts(): List<Post> {
        val snapshot = postsCollection
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Post::class.java)
        }
    }

    suspend fun likePost(postId: String) {
        val currentUser = auth.currentUser ?: throw Exception("User not authenticated")
        val postRef = postsCollection.document(postId)
        
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(postRef)
            val currentLikes = snapshot.get("likes") as? List<String> ?: listOf()
            
            if (currentLikes.contains(currentUser.uid)) {
                // Unlike: Remove user from likes list
                transaction.update(postRef, "likes", currentLikes - currentUser.uid)
            } else {
                // Like: Add user to likes list
                transaction.update(postRef, "likes", currentLikes + currentUser.uid)
            }
        }.await()
    }

    suspend fun getCurrentUserName(): String {
        return auth.currentUser?.displayName ?: "Anonymous"
    }

    suspend fun getUserPosts(userId: String): List<Post> {
        return try {
            Log.d("FirebaseDebug", "Starting getUserPosts query")
            
            val snapshot = postsCollection
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Post::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            Log.e("FirebaseDebug", "Error in getUserPosts", e)
            throw e
        }
    }

    suspend fun deletePost(postId: String) {
        try {
            postsCollection.document(postId).delete().await()
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error deleting post", e)
            throw e
        }
    }

    suspend fun repostPost(originalPost: Post, userId: String): String {
        return try {
            // Create a new post as a repost
            val repost = Post(
                userId = userId,
                content = originalPost.content,
                imageUrl = originalPost.imageUrl,
                timestamp = System.currentTimeMillis(),
                type = originalPost.type,
                likes = listOf(),
                comments = listOf(),
                repostedBy = listOf(),
                pollOptions = originalPost.pollOptions,
                userAvatarId = originalPost.userAvatarId,
                username = auth.currentUser?.displayName ?: "Anonymous"
            )

            // Add the repost to Firestore
            val repostRef = postsCollection.add(repost).await()

            // Update the original post's repostedBy list
            val originalPostRef = postsCollection.document(originalPost.id)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(originalPostRef)
                val currentRepostedBy = snapshot.get("repostedBy") as? List<String> ?: listOf()
                
                transaction.update(originalPostRef, 
                    "repostedBy", currentRepostedBy + userId
                )
            }.await()

            repostRef.id
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error reposting", e)
            throw e
        }
    }

    suspend fun unrepostPost(originalPostId: String, repostId: String, userId: String) {
        try {
            // Delete the repost
            postsCollection.document(repostId).delete().await()

            // Update the original post
            val originalPostRef = postsCollection.document(originalPostId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(originalPostRef)
                val currentReposts = snapshot.getLong("repostsCount") ?: 0
                val currentRepostedBy = snapshot.get("repostedBy") as? List<String> ?: listOf()
                
                transaction.update(originalPostRef, mapOf(
                    "repostsCount" to maxOf(0, currentReposts - 1),
                    "repostedBy" to currentRepostedBy.filter { it != userId }
                ))
            }.await()
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error unreposting", e)
            throw e
        }
    }
} 