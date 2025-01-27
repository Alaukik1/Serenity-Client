package life.sochpekharoch.serenity.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import life.sochpekharoch.serenity.models.Post
import life.sochpekharoch.serenity.models.Comment
import java.util.UUID
import android.util.Log
import android.graphics.BitmapFactory
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import life.sochpekharoch.serenity.models.PostType
import kotlinx.coroutines.flow.asStateFlow

class CommunityViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // For editing posts
    private val _showEditDialog = MutableStateFlow(false)
    val showEditDialog: StateFlow<Boolean> = _showEditDialog
    
    // Add these properties for comments
    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    init {
        // Initial load of posts
        refreshPosts()
        // Update existing posts
        updateExistingPosts()
    }

    private suspend fun convertImageToBase64(imageUri: Uri): String {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = storage.app.applicationContext.contentResolver.openInputStream(imageUri)
                val bytes = inputStream?.readBytes() ?: throw Exception("Failed to read image")
                inputStream.close()
                Base64.encodeToString(bytes, Base64.DEFAULT)
            } catch (e: Exception) {
                Log.e("CommunityViewModel", "Error converting image", e)
                throw e
            }
        }
    }

    fun createPost(content: String, type: PostType, imageUri: Uri?) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    _error.value = "Please sign in to create a post"
                    return@launch
                }

                _isLoading.value = true

                // Get user's avatarId from their profile
                val userDoc = firestore.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()

                val userAvatarId = userDoc.getLong("avatarId")?.toInt() ?: 1

                Log.d("PostDebug", """
                    Creating post with:
                    - UserAvatarId: $userAvatarId
                    - UserId: ${currentUser.uid}
                """.trimIndent())

                val post = hashMapOf(
                    "userId" to currentUser.uid,
                    "userAvatarId" to userAvatarId,
                    "content" to content,
                    "imageUrl" to (if (imageUri != null) convertImageToBase64(imageUri) else null),
                    "timestamp" to System.currentTimeMillis(),
                    "likes" to 0,
                    "likedBy" to listOf<String>(),
                    "commentsCount" to 0
                )

                val docRef = firestore.collection("posts")
                    .add(post)
                    .await()

                refreshPosts()
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Failed to create post: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun likePost(postId: String) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    _error.value = "Please sign in to like posts"
                    return@launch
                }

                // Find the post in current posts list
                val currentPosts = _posts.value.toMutableList()
                val postIndex = currentPosts.indexOfFirst { it.id == postId }
                
                if (postIndex != -1) {
                    val post = currentPosts[postIndex]
                    val likedBy = post.likedBy.toMutableList()
                    
                    // Update like status locally first
                    if (currentUser.uid in likedBy) {
                        likedBy.remove(currentUser.uid)
                    } else {
                        likedBy.add(currentUser.uid)
                    }
                    
                    // Update local state immediately
                    currentPosts[postIndex] = post.copy(
                        likes = likedBy.size,
                        likedBy = likedBy
                    )
                    _posts.value = currentPosts

                    // Then update Firestore
                    val postRef = firestore.collection("posts").document(postId)
                    postRef.update(
                        mapOf(
                            "likes" to likedBy.size,
                            "likedBy" to likedBy
                        )
                    ).await()
                }
            } catch (e: Exception) {
                _error.value = "Failed to like post: ${e.message}"
            }
        }
    }

    fun refreshPosts() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val snapshot = firestore.collection("posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()
                
                val posts = snapshot.documents.mapNotNull { doc ->
                    try {
                        Log.d("PollDebug", """
                            Post data:
                            - ID: ${doc.id}
                            - Type: ${doc.getString("type")}
                            - Options: ${doc.get("options")}
                        """.trimIndent())

                        Post(
                            id = doc.id,
                            userId = doc.getString("userId") ?: "",
                            userAvatarId = doc.getLong("userAvatarId")?.toInt() ?: 1,
                            content = doc.getString("content") ?: "",
                            imageUrl = doc.getString("imageUrl"),
                            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                            likes = doc.getLong("likes")?.toInt() ?: 0,
                            likedBy = doc.get("likedBy") as? List<String> ?: listOf(),
                            commentsCount = doc.getLong("commentsCount")?.toInt() ?: 0,
                            type = doc.getString("type") ?: "TEXT",
                            options = doc.get("options") as? List<Map<String, Any>>
                        )
                    } catch (e: Exception) {
                        Log.e("PollDebug", "Error mapping post: ${e.message}")
                        null
                    }
                }
                
                _posts.value = posts
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Failed to refresh posts: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    _error.value = "Please sign in to delete posts"
                    return@launch
                }

                // Delete the post
                firestore.collection("posts")
                    .document(postId)
                    .delete()
                    .await()

                // Update local state immediately
                _posts.value = _posts.value.filter { it.id != postId }
                
                // Then refresh from server
                refreshPosts()
            } catch (e: Exception) {
                _error.value = "Failed to delete post: ${e.message}"
            }
        }
    }

    fun editPost(postId: String, newContent: String) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    _error.value = "Please sign in to edit posts"
                    return@launch
                }

                firestore.collection("posts")
                    .document(postId)
                    .update("content", newContent)
                    .await()

                refreshPosts()
            } catch (e: Exception) {
                _error.value = "Failed to edit post: ${e.message}"
            }
        }
    }

    // For sharing posts
    fun getShareableLink(postId: String): String {
        // Replace with your actual domain
        return "https://yourapp.com/posts/$postId"
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up any listeners if needed
    }

    fun createPollPost(pollData: String) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    _error.value = "Please sign in to create a poll"
                    return@launch
                }

                _isLoading.value = true

                // Get user's avatarId
                val userDoc = firestore.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()

                val userAvatarId = userDoc.getLong("avatarId")?.toInt() ?: 1

                // Parse poll data (format: "question|option1|option2|...")
                val parts = pollData.split("|")
                Log.d("PollDebug", "Poll data parts: $parts")
                
                if (parts.size >= 3) { // Question + at least 2 options
                    val question = parts[0]
                    val options = parts.drop(1).map { option ->
                        mapOf(
                            "text" to option,
                            "votes" to 0,
                            "voters" to listOf<String>()
                        )
                    }

                    Log.d("PollDebug", """
                        Creating poll post:
                        Question: $question
                        Options: $options
                    """.trimIndent())

                    val post = hashMapOf(
                        "userId" to currentUser.uid,
                        "userAvatarId" to userAvatarId,
                        "type" to "POLL",
                        "content" to question,
                        "options" to options,
                        "timestamp" to System.currentTimeMillis(),
                        "likes" to 0,
                        "likedBy" to listOf<String>(),
                        "commentsCount" to 0
                    )

                    val docRef = firestore.collection("posts")
                        .add(post)
                        .await()

                    Log.d("PollDebug", "Poll post created with ID: ${docRef.id}")
                    refreshPosts()
                } else {
                    Log.e("PollDebug", "Invalid poll data format. Parts size: ${parts.size}")
                    _error.value = "Invalid poll data format"
                }
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e("PollDebug", "Failed to create poll: ${e.message}", e)
                _error.value = "Failed to create poll: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    private fun updateExistingPosts() {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("posts")
                    .whereEqualTo("userAvatarId", null)
                    .get()
                    .await()

                if (!snapshot.isEmpty) {
                    val batch = firestore.batch()
                    
                    snapshot.documents.forEach { doc ->
                        // Get the user's avatarId
                        val userId = doc.getString("userId")
                        if (userId != null) {
                            val userDoc = firestore.collection("users")
                                .document(userId)
                                .get()
                                .await()
                            
                            val userAvatarId = userDoc.getLong("avatarId")?.toInt() ?: 1
                            batch.update(doc.reference, "userAvatarId", userAvatarId)
                        } else {
                            // If no userId, set default avatarId
                            batch.update(doc.reference, "userAvatarId", 1)
                        }
                    }

                    batch.commit().await()
                    Log.d("PostDebug", "Updated ${snapshot.size()} posts with avatarId")
                    
                    // Refresh posts after update
                    refreshPosts()
                }
            } catch (e: Exception) {
                Log.e("PostDebug", "Error updating existing posts: ${e.message}")
            }
        }
    }

    // Add function to load comments for a post
    fun loadComments(postId: String) {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("posts")
                    .document(postId)
                    .collection("comments")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val commentsList = snapshot.documents.mapNotNull { doc ->
                    try {
                        Comment(
                            id = doc.id,
                            userId = doc.getString("userId") ?: "",
                            content = doc.getString("content") ?: "",
                            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                _comments.value = commentsList
            } catch (e: Exception) {
                _error.value = "Failed to load comments: ${e.message}"
            }
        }
    }

    // Add function to add a new comment
    fun addComment(postId: String, content: String) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    _error.value = "Please sign in to comment"
                    return@launch
                }

                val comment = hashMapOf(
                    "userId" to currentUser.uid,
                    "content" to content,
                    "timestamp" to System.currentTimeMillis()
                )

                // Add comment to Firestore
                firestore.collection("posts")
                    .document(postId)
                    .collection("comments")
                    .add(comment)
                    .await()

                // Increment comments count
                firestore.collection("posts")
                    .document(postId)
                    .update("commentsCount", FieldValue.increment(1))
                    .await()

                // Reload comments
                loadComments(postId)
                
                // Refresh posts to update comment count
                refreshPosts()
            } catch (e: Exception) {
                _error.value = "Failed to add comment: ${e.message}"
            }
        }
    }

    fun voteOnPoll(postId: String, optionIndex: Int) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    _error.value = "Please sign in to vote"
                    return@launch
                }

                // Get current post
                val currentPost = _posts.value.find { it.id == postId } ?: return@launch
                val currentOptions = currentPost.options?.toMutableList() ?: return@launch

                // Update the selected option
                val selectedOption = currentOptions[optionIndex]
                val currentVotes = (selectedOption["votes"] as? Long ?: 0).toInt()
                val currentVoters = selectedOption["voters"] as? List<String> ?: listOf()

                // Check if user has already voted on any option
                val previousVoteIndex = currentOptions.indexOfFirst { option ->
                    (option["voters"] as? List<String>)?.contains(currentUser.uid) == true
                }

                // Update the options
                if (previousVoteIndex != -1) {
                    // Remove vote from previous option
                    val previousOption = currentOptions[previousVoteIndex]
                    val previousVotes = (previousOption["votes"] as? Long ?: 0).toInt()
                    val previousVoters = previousOption["voters"] as? List<String> ?: listOf()
                    
                    currentOptions[previousVoteIndex] = mapOf(
                        "text" to (previousOption["text"] as String),
                        "votes" to (previousVotes - 1),
                        "voters" to previousVoters.filter { it != currentUser.uid }
                    )
                }

                // Add vote to new option
                if (previousVoteIndex != optionIndex) {
                    currentOptions[optionIndex] = mapOf(
                        "text" to (selectedOption["text"] as String),
                        "votes" to (currentVotes + 1),
                        "voters" to (currentVoters + currentUser.uid)
                    )
                }

                // Update local state immediately
                _posts.value = _posts.value.map { post ->
                    if (post.id == postId) {
                        post.copy(options = currentOptions)
                    } else post
                }

                // Update Firestore
                firestore.collection("posts")
                    .document(postId)
                    .update("options", currentOptions)
                    .await()

            } catch (e: Exception) {
                _error.value = "Failed to vote: ${e.message}"
            }
        }
    }

    fun getPost(postId: String): StateFlow<Post?> {
        val _post = MutableStateFlow<Post?>(null)
        viewModelScope.launch {
            try {
                val doc = firestore.collection("posts")
                    .document(postId)
                    .get()
                    .await()

                _post.value = doc.toObject(Post::class.java)?.copy(id = doc.id)
            } catch (e: Exception) {
                _error.value = "Failed to load post: ${e.message}"
            }
        }
        return _post.asStateFlow()
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun getPostRealtime(postId: String): StateFlow<Post?> {
        val _post = MutableStateFlow<Post?>(null)
        viewModelScope.launch {
            try {
                // Listen to real-time updates
                firestore.collection("posts")
                    .document(postId)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            _error.value = "Error loading post: ${e.message}"
                            return@addSnapshotListener
                        }

                        if (snapshot != null && snapshot.exists()) {
                            _post.value = snapshot.toObject(Post::class.java)?.copy(id = snapshot.id)
                        }
                    }
            } catch (e: Exception) {
                _error.value = "Failed to load post: ${e.message}"
            }
        }
        return _post.asStateFlow()
    }
} 