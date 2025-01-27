package life.sochpekharoch.serenity

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SerenityApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            Log.d("Firebase", "Starting Firebase initialization")
            if (FirebaseApp.getApps(this).isEmpty()) {
                val app = FirebaseApp.initializeApp(this)
                
                // Add these lines to verify Firebase Auth configuration
                val auth = FirebaseAuth.getInstance()
                Log.d("Firebase", "Default auth instance: ${auth.app.name}")
                
                // Test if we can get the current user
                val currentUser = auth.currentUser
                Log.d("Firebase", "Current user: ${currentUser?.uid ?: "No user"}")
                
                // Verify Firestore access
                val db = FirebaseFirestore.getInstance()
                Log.d("Firebase", "Firestore instance initialized")
                
                // Test Firestore access
                db.collection("users")
                    .limit(1)
                    .get()
                    .addOnSuccessListener {
                        Log.d("Firebase", "Successfully accessed Firestore")
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firebase", "Failed to access Firestore", e)
                    }
            }
        } catch (e: Exception) {
            Log.e("Firebase", "Failed to initialize Firebase", e)
            e.printStackTrace()
        }
    }
}