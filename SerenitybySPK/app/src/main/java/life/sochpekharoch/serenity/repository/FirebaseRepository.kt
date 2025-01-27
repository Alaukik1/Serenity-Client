package life.sochpekharoch.serenity.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
            likes = 0,
            commentsCount = 0
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
            val currentLikes = snapshot.getLong("likes") ?: 0
            transaction.update(postRef, "likes", currentLikes + 1)
        }.await()
    }

    suspend fun getCurrentUserName(): String {
        return auth.currentUser?.displayName ?: "Anonymous"
    }
} 