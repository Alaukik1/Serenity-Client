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
import life.sochpekharoch.serenity.models.Comment
import android.util.Log
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import life.sochpekharoch.serenity.models.PostType
import kotlinx.coroutines.flow.asStateFlow
import life.sochpekharoch.serenity.models.Notification
import life.sochpekharoch.serenity.models.Post
import java.util.UUID
import android.graphics.BitmapFactory
import android.webkit.MimeTypeMap
import android.media.MediaMetadataRetriever
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import com.google.firebase.firestore.ListenerRegistration
import life.sochpekharoch.serenity.auth.AdminManager
import life.sochpekharoch.serenity.models.PollOption
import kotlinx.coroutines.delay
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.async
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.CancellableContinuation
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.google.firebase.Timestamp

// Add this data class at the top level
data class PostsState(
    val posts: List<Post> = emptyList()
)

class CommunityViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    
    // Change the state holder
    private val _postsState = MutableStateFlow(PostsState())
    val postsState: StateFlow<PostsState> = _postsState.asStateFlow()

    // Add a helper function to update posts
    private fun updatePosts(transform: (List<Post>) -> List<Post>) {
        val currentPosts = _postsState.value.posts
        val updatedPosts = transform(currentPosts)
        _postsState.value = PostsState(updatedPosts)
    }

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

    // Add real-time listener
    private var postsListener: ListenerRegistration? = null

    // Add these properties
    private val _notifications = MutableLiveData<List<Notification>>(emptyList())
    val notifications: LiveData<List<Notification>> = _notifications

    private val adminManager = AdminManager()
    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin

    private val _isReposting = MutableStateFlow(false)
    val isReposting: StateFlow<Boolean> = _isReposting

    // Add new state
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Add this state
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    // Add this property near other state declarations
    private val _postsLoaded = MutableStateFlow(false)
    val postsLoaded: StateFlow<Boolean> = _postsLoaded.asStateFlow()

    init {
        // Initialize any necessary properties here if needed
    }

    private suspend fun setupPostsListener() = suspendCancellableCoroutine<Unit> { continuation ->
        try {
            Log.d("CommunityViewModel", "Setting up posts listener...")
            postsListener?.remove()
            
            postsListener = firestore.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("CommunityViewModel", "Listen failed", e)
                        _error.value = "Failed to load posts: ${e.message}"
                        _isLoading.value = false
                        if (!continuation.isCompleted) {
                            continuation.resumeWithException(e)
                        }
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val posts = snapshot.documents.mapNotNull { doc ->
                            try {
                                Post(
                                    id = doc.id,
                                    userId = if (doc.getBoolean("isRepost") == true) {
                                        doc.getString("originalUserId") ?: ""
                                    } else {
                                        doc.getString("userId") ?: ""
                                    },
                                    userAvatarId = if (doc.getBoolean("isRepost") == true) {
                                        doc.getLong("originalUserAvatarId")?.toInt() ?: 1
                                    } else {
                                        doc.getLong("userAvatarId")?.toInt() ?: 1
                                    },
                                    content = if (doc.getBoolean("isRepost") == true) {
                                        doc.getString("originalContent") ?: ""
                                    } else {
                                        doc.getString("content") ?: ""
                                    },
                                    imageUrl = if (doc.getBoolean("isRepost") == true) {
                                        doc.getString("originalImageUrl")
                                    } else {
                                        doc.getString("imageUrl")
                                    },
                                    timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                    likes = (doc.get("likes") as? List<String>) ?: listOf(),
                                    comments = generateDummyComments(doc.getLong("commentsCount")?.toInt() ?: 0),
                                    repostedBy = (doc.get("repostedBy") as? List<String>) ?: listOf(),
                                    type = try {
                                        PostType.valueOf(doc.getString("type") ?: "TEXT")
                                    } catch (e: Exception) {
                                        PostType.TEXT
                                    },
                                    pollOptions = (doc.get("pollOptions") as? List<Map<String, Any>>)?.map { option ->
                                        PollOption(
                                            id = option["id"] as? String ?: "",
                                            text = option["text"] as? String ?: "",
                                            votes = (option["votes"] as? List<String>) ?: listOf()
                                        )
                                    } ?: listOf(),
                                    username = if (doc.getBoolean("isRepost") == true) {
                                        doc.getString("originalUsername") ?: "Anonymous"
                                    } else {
                                        doc.getString("username") ?: "Anonymous"
                                    },
                                    isRepost = doc.getBoolean("isRepost") ?: false,
                                    repostUserId = doc.getString("userId"),
                                    repostUsername = doc.getString("username"),
                                    repostUserAvatarId = doc.getLong("userAvatarId")?.toInt(),
                                    originalUserId = if (doc.getBoolean("isRepost") == true) {
                                        doc.getString("originalUserId")
                                    } else {
                                        null
                                    },
                                    originalUsername = if (doc.getBoolean("isRepost") == true) {
                                        doc.getString("originalUsername")
                                    } else {
                                        null
                                    },
                                    originalUserAvatarId = if (doc.getBoolean("isRepost") == true) {
                                        doc.getLong("originalUserAvatarId")?.toInt()
                                    } else {
                                        null
                                    }
                                )
                            } catch (e: Exception) {
                                Log.e("PostDebug", "Error mapping post: ${e.message}")
                                null
                            }
                        }
                        
                        updatePosts { it }
                        _isLoading.value = false
                        _postsLoaded.value = true
                        
                        if (!continuation.isCompleted) {
                            continuation.resume(Unit)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("CommunityViewModel", "Error in setupPostsListener", e)
            if (!continuation.isCompleted) {
                continuation.resumeWithException(e)
            }
        }
    }

    // Helper function to generate dummy comment placeholders
    private fun generateDummyComments(count: Int): List<String> {
        Log.d("CommentDebug", "Generating dummy comments list with count: $count")
        return List(count) { "" }
    }

    private suspend fun convertImageToBase64(croppedUri: Uri): String {
        return withContext(Dispatchers.IO) {
            try {
                val contentResolver = storage.app.applicationContext.contentResolver
                contentResolver.openInputStream(croppedUri)?.use { inputStream ->
                    val bytes = inputStream.readBytes()
                    Log.d("ImageDebug", "Image size in bytes: ${bytes.size}")
                    val base64String = Base64.encodeToString(bytes, Base64.DEFAULT)
                    Log.d("ImageDebug", "Base64 string length: ${base64String.length}")
                    base64String
                } ?: throw Exception("Failed to read image")
            } catch (e: Exception) {
                Log.e("CommunityViewModel", "Error converting image to Base64: ${e.message}")
                throw e
            }
        }
    }

    fun createPost(content: String, type: PostType, mediaUri: Uri?) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    _error.value = "Please sign in to create a post"
                    return@launch
                }

                Log.d("PostDebug", """
                    Creating post:
                    - UserID: ${currentUser.uid}
                    - Content: ${content.take(50)}...
                    - Type: $type
                    - Has Media: ${mediaUri != null}
                """.trimIndent())

                _isLoading.value = true

                // Get user's avatarId from their profile
                val userDoc = firestore.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()

                val userAvatarId = userDoc.getLong("avatarId")?.toInt() ?: 1

                // Convert image to Base64 if it's an image post
                var imageUrl: String? = null
                if (mediaUri != null && type == PostType.IMAGE) {
                    Log.d("ImageDebug", "Converting image to Base64")
                    imageUrl = convertImageToBase64(mediaUri)
                    Log.d("ImageDebug", "Image converted successfully: ${imageUrl?.take(100)}...")
                }

                val post = hashMapOf(
                    "userId" to currentUser.uid,
                    "userAvatarId" to userAvatarId,
                    "content" to content,
                    "imageUrl" to imageUrl,
                    "type" to type.name,
                    "timestamp" to System.currentTimeMillis(),
                    "likes" to listOf<String>(),
                    "comments" to listOf<String>(),
                    "repostedBy" to listOf<String>(),
                    "pollOptions" to listOf<Map<String, Any>>(),
                    "username" to currentUser.uid,
                    "commentsCount" to 0L
                )

                firestore.collection("posts")
                    .add(post)
                    .await()

                refreshPosts()
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e("PostDebug", "Error creating post", e)
                _error.value = "Failed to create post: ${e.message}"
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
                
                val newPosts = snapshot.documents.mapNotNull { doc ->
                    try {
                        Post(
                            id = doc.id,
                            userId = if (doc.getBoolean("isRepost") == true) {
                                doc.getString("originalUserId") ?: ""
                            } else {
                                doc.getString("userId") ?: ""
                            },
                            userAvatarId = if (doc.getBoolean("isRepost") == true) {
                                doc.getLong("originalUserAvatarId")?.toInt() ?: 1
                            } else {
                                doc.getLong("userAvatarId")?.toInt() ?: 1
                            },
                            content = if (doc.getBoolean("isRepost") == true) {
                                doc.getString("originalContent") ?: ""
                            } else {
                                doc.getString("content") ?: ""
                            },
                            imageUrl = if (doc.getBoolean("isRepost") == true) {
                                doc.getString("originalImageUrl")
                            } else {
                                doc.getString("imageUrl")
                            },
                            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                            likes = (doc.get("likes") as? List<String>) ?: listOf(),
                            comments = generateDummyComments(doc.getLong("commentsCount")?.toInt() ?: 0),
                            repostedBy = (doc.get("repostedBy") as? List<String>) ?: listOf(),
                            type = try {
                                PostType.valueOf(doc.getString("type") ?: "TEXT")
                            } catch (e: Exception) {
                                PostType.TEXT
                            },
                            pollOptions = (doc.get("pollOptions") as? List<Map<String, Any>>)?.map { option ->
                                PollOption(
                                    id = option["id"] as? String ?: "",
                                    text = option["text"] as? String ?: "",
                                    votes = (option["votes"] as? List<String>) ?: listOf()
                                )
                            } ?: listOf(),
                            username = if (doc.getBoolean("isRepost") == true) {
                                doc.getString("originalUsername") ?: "Anonymous"
                            } else {
                                doc.getString("username") ?: "Anonymous"
                            },
                            isRepost = doc.getBoolean("isRepost") ?: false,
                            repostUserId = doc.getString("userId"),
                            repostUsername = doc.getString("username"),
                            repostUserAvatarId = doc.getLong("userAvatarId")?.toInt(),
                            originalUserId = if (doc.getBoolean("isRepost") == true) {
                                doc.getString("originalUserId")
                            } else {
                                null
                            },
                            originalUsername = if (doc.getBoolean("isRepost") == true) {
                                doc.getString("originalUsername")
                            } else {
                                null
                            },
                            originalUserAvatarId = if (doc.getBoolean("isRepost") == true) {
                                doc.getLong("originalUserAvatarId")?.toInt()
                            } else {
                                null
                            }
                        )
                    } catch (e: Exception) {
                        Log.e("PostDebug", "Error mapping post: ${e.message}")
                        null
                    }
                }
                
                updatePosts { newPosts }
            } catch (e: Exception) {
                Log.e("CommunityViewModel", "Error refreshing posts", e)
                _error.value = "Failed to refresh posts: ${e.message}"
                updatePosts { emptyList() }
            } finally {
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
                updatePosts { it.filter { it.id != postId } }
                
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
        postsListener?.remove()
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

                val userDoc = firestore.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()

                val userAvatarId = userDoc.getLong("avatarId")?.toInt() ?: 1

                val parts = pollData.split("|")
                
                if (parts.size >= 3) {
                    val question = parts[0]
                    // Create poll options with the new structure
                    val pollOptions = parts.drop(1).map { option ->
                        mapOf(
                            "id" to UUID.randomUUID().toString(),
                            "text" to option,
                            "votes" to listOf<String>()
                        )
                    }

                    val post = hashMapOf(
                        "userId" to currentUser.uid,
                        "userAvatarId" to userAvatarId,
                        "type" to "POLL",
                        "content" to question,
                        "pollOptions" to pollOptions,
                        "timestamp" to System.currentTimeMillis(),
                        "likes" to listOf<String>(),
                        "comments" to listOf<String>(),
                        "repostedBy" to listOf<String>(),
                        "username" to currentUser.uid
                    )

                    firestore.collection("posts")
                        .add(post)
                        .await()

                    refreshPosts()
                } else {
                    _error.value = "Invalid poll data format"
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Failed to create poll: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    private suspend fun updateExistingPosts() {
        Log.d("CommunityViewModel", "Updating existing posts...")
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
                
                refreshPosts()
            }
        } catch (e: Exception) {
            Log.e("CommunityViewModel", "Error updating existing posts", e)
            throw e
        }
    }

    // Add function to load comments for a post
    fun loadComments(postId: String) {
        viewModelScope.launch {
            try {
                // Add real-time listener
                firestore.collection("posts")
                    .document(postId)
                    .collection("comments")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            _error.value = "Failed to load comments: ${e.message}"
                            return@addSnapshotListener
                        }

                        if (snapshot != null) {
                            val commentsList = snapshot.documents.mapNotNull { doc ->
                                try {
                                    Comment(
                                        id = doc.id,
                                        userId = doc.getString("userId") ?: "",
                                        userAvatarId = doc.getLong("userAvatarId")?.toInt() ?: 1,
                                        content = doc.getString("content") ?: "",
                                        timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                        likes = doc.getLong("likes")?.toInt() ?: 0,
                                        likedBy = doc.get("likedBy") as? List<String> ?: listOf()
                                    )
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            _comments.value = commentsList
                        }
                    }
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

                // Update UI immediately (optimistic update)
                updatePosts { currentPosts ->
                    currentPosts.map { post ->
                        if (post.id == postId) {
                            // Create a new dummy comment list with one more item
                            val newComments = generateDummyComments((post.comments.size + 1))
                            post.copy(comments = newComments)
                        } else {
                            post
                        }
                    }
                }

                // Rest of the comment addition logic...
                val userDoc = firestore.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()

                val userAvatarId = userDoc.getLong("avatarId")?.toInt() ?: 1

                val comment = hashMapOf(
                    "userId" to currentUser.uid,
                    "userAvatarId" to userAvatarId,
                    "content" to content,
                    "timestamp" to System.currentTimeMillis()
                )

                // Server update
                val commentRef = firestore.collection("posts")
                    .document(postId)
                    .collection("comments")
                    .add(comment)
                    .await()

                firestore.collection("posts")
                    .document(postId)
                    .update("commentsCount", FieldValue.increment(1))
                    .await()

                // Create notification and other operations...
                // Note: We don't need to call refreshPosts() here since we already updated the UI
                loadComments(postId)

            } catch (e: Exception) {
                Log.e("CommentDebug", "Error adding comment", e)
                _error.value = "Failed to add comment: ${e.message}"
                // Revert the optimistic update on error
                refreshPosts()
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

                Log.d("VoteDebug", "Attempting to vote - User: ${currentUser.uid}, Post: $postId, Option: $optionIndex")

                firestore.runTransaction { transaction ->
                    val postRef = firestore.collection("posts").document(postId)
                    val snapshot = transaction.get(postRef)
                    
                    if (!snapshot.exists()) {
                        Log.e("VoteDebug", "Post not found")
                        return@runTransaction
                    }

                    @Suppress("UNCHECKED_CAST")
                    val options = (snapshot.get("options") as? ArrayList<HashMap<String, Any>>)?.toMutableList()
                    if (options == null) {
                        Log.e("VoteDebug", "Options not found or invalid")
                        return@runTransaction
                    }
                    
                    if (optionIndex < 0 || optionIndex >= options.size) {
                        Log.e("VoteDebug", "Invalid option index")
                        return@runTransaction
                    }
                    
                    // Check if user has already voted
                    val previousVoteIndex = options.indexOfFirst { option ->
                        val voters = option["voters"] as? List<String> ?: listOf()
                        voters.contains(currentUser.uid)
                    }

                    Log.d("VoteDebug", "Previous vote index: $previousVoteIndex")

                    // Remove previous vote if exists
                    if (previousVoteIndex != -1 && previousVoteIndex != optionIndex) {
                        val previousOption = HashMap<String, Any>(options[previousVoteIndex])
                        val previousVotes = (previousOption["votes"] as? Long ?: 0L)
                        val previousVoters = (previousOption["voters"] as? List<String> ?: listOf()).toMutableList()
                        
                        previousVoters.remove(currentUser.uid)
                        previousOption["votes"] = maxOf(0L, previousVotes - 1L)
                        previousOption["voters"] = previousVoters
                        
                        options[previousVoteIndex] = previousOption
                        Log.d("VoteDebug", "Removed previous vote")
                    }

                    // Add new vote
                    if (previousVoteIndex != optionIndex) {
                        val selectedOption = HashMap<String, Any>(options[optionIndex])
                        val currentVotes = (selectedOption["votes"] as? Long ?: 0L)
                        val currentVoters = (selectedOption["voters"] as? List<String> ?: listOf()).toMutableList()
                        
                        if (!currentVoters.contains(currentUser.uid)) {
                            currentVoters.add(currentUser.uid)
                            selectedOption["votes"] = currentVotes + 1L
                            selectedOption["voters"] = currentVoters
                            options[optionIndex] = selectedOption
                            Log.d("VoteDebug", "Added new vote")
                        }
                    }

                    // Update the document with the new options
                    transaction.update(postRef, "options", options)
                    Log.d("VoteDebug", "Transaction completed successfully")
                }.await()

                // Create notification for poll creator
                val post = _postsState.value.posts.find { it.id == postId }
                post?.let {
                    val optionText = post.pollOptions.getOrNull(optionIndex)?.text ?: return@let
                    createNotification(
                        recipientId = it.userId,
                        postId = postId,
                        content = "voted '${optionText}' on your poll"
                    )
                }
            } catch (e: Exception) {
                Log.e("VoteDebug", "Error voting on poll: ${e.message}", e)
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

    fun likeComment(postId: String, commentId: String) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    _error.value = "Please sign in to like comments"
                    return@launch
                }

                val commentRef = firestore.collection("posts")
                    .document(postId)
                    .collection("comments")
                    .document(commentId)

                firestore.runTransaction { transaction ->
                    val snapshot = transaction.get(commentRef)
                    val likedBy = snapshot.get("likedBy") as? List<String> ?: listOf()
                    
                    if (currentUser.uid in likedBy) {
                        // Unlike
                        transaction.update(commentRef, 
                            mapOf(
                                "likedBy" to likedBy - currentUser.uid,
                                "likes" to (likedBy.size - 1)
                            )
                        )
                    } else {
                        // Like
                        transaction.update(commentRef, 
                            mapOf(
                                "likedBy" to likedBy + currentUser.uid,
                                "likes" to (likedBy.size + 1)
                            )
                        )
                    }
                }.await()

                // Reload comments to reflect changes
                loadComments(postId)
            } catch (e: Exception) {
                _error.value = "Failed to like comment: ${e.message}"
            }
        }
    }

    fun likePost(postId: String) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser?.uid ?: return@launch
                val postRef = firestore.collection("posts").document(postId)
                
                firestore.runTransaction { transaction ->
                    val snapshot = transaction.get(postRef)
                    val currentLikes = snapshot.get("likes") as? List<String> ?: listOf()
                    
                    val newLikes = if (currentLikes.contains(currentUser)) {
                        currentLikes - currentUser  // Remove user's like
                    } else {
                        currentLikes + currentUser  // Add user's like
                    }
                    
                    transaction.update(postRef, "likes", newLikes)
                }.await()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    // Add this function to create notifications
    private suspend fun createNotification(
        recipientId: String,
        postId: String,
        content: String
    ) {
        try {
            val currentUser = auth.currentUser ?: return
            
            val notification = hashMapOf(
                "title" to "New Activity",
                "message" to content,
                "timestamp" to Timestamp.now(),
                "userId" to recipientId
            )

            firestore.collection("notifications")
                .add(notification)
                .await()
        } catch (e: Exception) {
            Log.e("NotificationDebug", "Error creating notification", e)
        }
    }

    // Add function to load user's notifications
    private suspend fun loadNotifications() {
        try {
            val currentUser = auth.currentUser ?: run {
                Log.e("NotificationDebug", "No current user found")
                return
            }
            Log.d("NotificationDebug", "Loading notifications for user: ${currentUser.uid}")

            val snapshot = firestore.collection("notifications")
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .await()

            Log.d("NotificationDebug", "Raw snapshot size: ${snapshot.size()}")
            Log.d("NotificationDebug", "Documents: ${snapshot.documents.map { it.data }}")

            val notificationsList = snapshot.documents.mapNotNull { doc ->
                try {
                    Log.d("NotificationDebug", "Processing document: ${doc.id}")
                    Log.d("NotificationDebug", "Document data: ${doc.data}")
                    
                    // More robust timestamp handling with logging
                    val timestamp = try {
                        val timestampValue = doc.get("timestamp")
                        Log.d("NotificationDebug", "Timestamp value type: ${timestampValue?.javaClass}")
                        Log.d("NotificationDebug", "Timestamp raw value: $timestampValue")
                        
                        when (val value = timestampValue) {
                            is Timestamp -> {
                                Log.d("NotificationDebug", "Using Timestamp directly")
                                value
                            }
                            is com.google.firebase.Timestamp -> {
                                Log.d("NotificationDebug", "Converting from Firebase Timestamp")
                                Timestamp(value.seconds, value.nanoseconds)
                            }
                            is Long -> {
                                Log.d("NotificationDebug", "Converting from Long")
                                Timestamp(value / 1000, 0)
                            }
                            is Map<*, *> -> {
                                Log.d("NotificationDebug", "Converting from Map")
                                val seconds = (value["seconds"] as? Long) ?: 0L
                                val nanoseconds = (value["nanoseconds"] as? Int) ?: 0
                                Timestamp(seconds, nanoseconds)
                            }
                            else -> {
                                Log.w("NotificationDebug", "Unknown timestamp type: ${timestampValue?.javaClass}")
                                Timestamp.now()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("NotificationDebug", "Error converting timestamp", e)
                        Timestamp.now()
                    }

                    val notification = Notification(
                        title = doc.getString("title") ?: "",
                        message = doc.getString("message") ?: "",
                        timestamp = timestamp,
                        userId = doc.getString("userId") ?: ""
                    )
                    Log.d("NotificationDebug", "Created notification: $notification")
                    notification
                } catch (e: Exception) {
                    Log.e("NotificationDebug", "Error mapping notification: ${e.message}", e)
                    null
                }
            }.sortedByDescending { it.timestamp?.seconds ?: 0 }

            Log.d("NotificationDebug", "Final notifications list size: ${notificationsList.size}")
            
            withContext(Dispatchers.Main) {
                _notifications.value = notificationsList
                Log.d("NotificationDebug", "Updated LiveData with notifications")
            }
        } catch (e: Exception) {
            Log.e("NotificationDebug", "Error loading notifications", e)
            _error.value = "Failed to load notifications: ${e.message}"
        }
    }

    // Update the refreshNotifications function
    fun refreshNotifications() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    loadNotifications()
                }
            } catch (e: Exception) {
                Log.e("NotificationDebug", "Error refreshing notifications", e)
                _error.value = "Failed to load notifications: ${e.message}"
            }
        }
    }

    // Update the initializeApp function
    suspend fun initializeApp() {
        try {
            _isLoading.value = true
            Log.d("CommunityViewModel", "Starting initialization...")
            
            coroutineScope {
                // First set up the posts listener
                setupPostsListener()
                
                // Then update posts
                updateExistingPosts()
                
                // Finally load notifications
                loadNotifications()
            }
            
            _isInitialized.value = true
        } catch (e: Exception) {
            Log.e("CommunityViewModel", "Initialization failed", e)
            throw e
        } finally {
            _isLoading.value = false
        }
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    _error.value = "Please sign in to like posts"
                    return@launch
                }

                // Get current posts list
                val currentPosts = _postsState.value.posts.toMutableList()
                val postIndex = currentPosts.indexOfFirst { it.id == postId }

                if (postIndex != -1) {
                    val post = currentPosts[postIndex]
                    val newLikes = if (post.likes.contains(currentUser.uid)) {
                        post.likes - currentUser.uid
                    } else {
                        post.likes + currentUser.uid
                    }

                    // Update the post with new likes
                    currentPosts[postIndex] = post.copy(likes = newLikes)
                    
                    // Update UI immediately
                    _postsState.value = PostsState(posts = currentPosts)

                    // Server update
                    try {
                        val postRef = firestore.collection("posts").document(postId)
                        if (newLikes.contains(currentUser.uid)) {
                            postRef.update("likes", FieldValue.arrayUnion(currentUser.uid))
                            createLikeNotification(postId)
                        } else {
                            postRef.update("likes", FieldValue.arrayRemove(currentUser.uid))
                        }
                    } catch (e: Exception) {
                        Log.e("LikeDebug", "Error updating server", e)
                        // Revert the optimistic update on error
                        refreshPosts()
                    }
                }
            } catch (e: Exception) {
                Log.e("LikeDebug", "Error toggling like", e)
                _error.value = "Failed to update like: ${e.message}"
                refreshPosts()
            }
        }
    }

    private fun createLikeNotification(postId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentUser = auth.currentUser ?: return@launch
                val post = _postsState.value.posts.find { it.id == postId } ?: return@launch
                
                if (post.userId != currentUser.uid) {
                    val notification = hashMapOf(
                        "title" to "New Like",
                        "message" to "${currentUser.uid} liked your post",
                        "timestamp" to com.google.firebase.Timestamp.now(),
                        "userId" to post.userId
                    )

                    firestore.collection("notifications")
                        .add(notification)
                        .await()
                }
            } catch (e: Exception) {
                Log.e("NotificationDebug", "Error creating notification", e)
            }
        }
    }

    // Add function to mark notification as read
    fun markNotificationAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                firestore.collection("notifications")
                    .document(notificationId)
                    .update("isRead", true)
                    .await()
            } catch (e: Exception) {
                _error.value = "Failed to mark notification as read: ${e.message}"
            }
        }
    }

    fun checkAdminStatus(userId: String) {
        viewModelScope.launch {
            _isAdmin.value = adminManager.isUserAdmin(userId)
        }
    }

    fun repostPost(post: Post, comment: String = "") {
        viewModelScope.launch {
            try {
                _isReposting.value = true
                Log.d("RepostDebug", "Starting repost process")
                
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    // Add debug logging for repostedBy list
                    Log.d("RepostDebug", "Current repostedBy list: ${post.repostedBy}")
                    Log.d("RepostDebug", "Current user ID: ${currentUser.uid}")
                    
                    if (post.repostedBy.contains(currentUser.uid)) {
                        Log.d("RepostDebug", "User has already reposted this post")
                        // Instead of throwing an exception, let's unrepost
                        updatePosts { currentPosts ->
                            currentPosts.mapNotNull { currentPost ->
                                if (currentPost.isRepost && 
                                    currentPost.userId == post.userId && 
                                    currentPost.repostUserId == currentUser.uid 
                                ) {
                                    null  // Filter out this post by returning null
                                } else {
                                    currentPost  // Keep other posts
                                }
                            }
                        }
                        return@launch
                    }

                    // Create repost
                    val repost = hashMapOf(
                        "userId" to currentUser.uid,
                        "content" to post.content,
                        "imageUrl" to post.imageUrl,
                        "timestamp" to System.currentTimeMillis(),
                        "type" to post.type.name,
                        "likes" to listOf<String>(),
                        "comments" to listOf<String>(),
                        "repostedBy" to listOf<String>(),
                        "pollOptions" to post.pollOptions,
                        "userAvatarId" to post.userAvatarId,
                        "username" to currentUser.uid,
                        "isRepost" to true,
                        "originalUserId" to post.userId,
                        "originalContent" to post.content,
                        "originalImageUrl" to post.imageUrl,
                        "originalUserAvatarId" to post.userAvatarId,
                        "originalUsername" to post.username,
                        "repostComment" to comment
                    )

                    // Add the repost
                    firestore.collection("posts")
                        .add(repost)
                        .await()

                    // Update original post's repostedBy list
                    firestore.collection("posts")
                        .document(post.id)
                        .update(
                            "repostedBy", FieldValue.arrayUnion(currentUser.uid)
                        ).await()

                    refreshPosts()
                }
            } catch (e: Exception) {
                Log.e("RepostDebug", "Error during repost", e)
                _error.value = e.message
            } finally {
                _isReposting.value = false
            }
        }
    }

    fun unrepostPost(originalPostId: String, repostId: String) {
        viewModelScope.launch {
            try {
                _isReposting.value = true
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    Log.d("RepostDebug", "Starting unrepost process")
                    
                    // Delete repost first
                    firestore.collection("posts")
                        .document(repostId)
                        .delete()
                        .await()
                    
                    Log.d("RepostDebug", "Deleted repost document")

                    // Update the original post's repostedBy list
                    firestore.collection("posts")
                        .document(originalPostId)
                        .update(
                            "repostedBy", FieldValue.arrayRemove(currentUser.uid)
                        ).await()
                    
                    Log.d("RepostDebug", "Updated original post's repostedBy list")

                    // Refresh posts after all operations are complete
                    refreshPosts()
                }
            } catch (e: Exception) {
                Log.e("RepostDebug", "Error during unrepost", e)
                _error.value = e.message
            } finally {
                _isReposting.value = false
            }
        }
    }
} 