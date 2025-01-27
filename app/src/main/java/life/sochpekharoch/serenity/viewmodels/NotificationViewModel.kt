package life.sochpekharoch.serenity.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Notification(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val timestamp: Long = 0L,
    val isRead: Boolean = false
)

class NotificationViewModel : ViewModel() {
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications = _notifications.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                Log.d("NotificationScreen", "Loading notifications for user: $userId")
                
                if (userId != null) {
                    val query = FirebaseFirestore.getInstance()
                        .collection("notifications")
                        .whereEqualTo("userId", userId)
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                    
                    Log.d("NotificationScreen", "Executing Firestore query: $query")
                    
                    val snapshot = query.get().await()
                    Log.d("NotificationScreen", "Raw Firestore response: ${snapshot.documents}")

                    _notifications.value = snapshot.documents.map { doc ->
                        Notification(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            body = doc.getString("body") ?: "",
                            timestamp = doc.getLong("timestamp") ?: 0L,
                            isRead = doc.getBoolean("isRead") ?: false
                        ).also {
                            Log.d("NotificationScreen", "Mapped notification: $it")
                        }
                    }
                } else {
                    Log.e("NotificationScreen", "Cannot load notifications - user not signed in")
                }
            } catch (e: Exception) {
                Log.e("NotificationScreen", "Error loading notifications", e)
                e.printStackTrace()
            }
        }
    }
} 