package life.sochpekharoch.serenity.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthManager {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun deleteUserAccount() {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("No user logged in")
            
            // Delete user document from Firestore
            firestore.collection("users")
                .document(userId)
                .delete()
                .await()
            
            // Delete user authentication
            auth.currentUser?.delete()?.await()
            
        } catch (e: Exception) {
            throw Exception("Failed to delete account: ${e.message}")
        }
    }
} 