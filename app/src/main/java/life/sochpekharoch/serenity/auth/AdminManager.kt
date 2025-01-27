package life.sochpekharoch.serenity.auth

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AdminManager {
    private val firestore = FirebaseFirestore.getInstance()
    
    suspend fun isUserAdmin(userId: String): Boolean {
        return try {
            val docSnapshot = firestore.collection("admins")
                .document(userId)
                .get()
                .await()
            
            docSnapshot.exists()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun makeUserAdmin(userId: String) {
        try {
            firestore.collection("users")
                .document(userId)
                .update("isAdmin", true)
                .await()
        } catch (e: Exception) {
            throw Exception("Failed to make user admin: ${e.message}")
        }
    }
} 