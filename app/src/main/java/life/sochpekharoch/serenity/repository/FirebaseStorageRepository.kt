package life.sochpekharoch.serenity.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseStorageRepository {
    private val storage = FirebaseStorage.getInstance()

    suspend fun uploadImage(imageUri: Uri): String {
        val filename = "posts/images/${UUID.randomUUID()}"
        val imageRef = storage.reference.child(filename)
        
        return try {
            // Upload the file
            imageRef.putFile(imageUri).await()
            // Get and return the download URL
            imageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            throw Exception("Failed to upload image: ${e.message}")
        }
    }

    suspend fun deleteImage(path: String) {
        storage.reference.child(path).delete().await()
    }
} 