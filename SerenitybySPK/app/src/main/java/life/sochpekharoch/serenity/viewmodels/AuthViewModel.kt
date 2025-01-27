package life.sochpekharoch.serenity.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthSettings
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import life.sochpekharoch.serenity.models.User

data class AuthState(
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val verificationEmailSent: Boolean = false,
    val verificationCode: String? = null,
    val isEmailVerified: Boolean = false,
    val isSignupSuccess: Boolean = false,
    val isFirstLogin: Boolean = false
)

class AuthViewModel(private val app: Application) : AndroidViewModel(app) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile

    private val _currentUserName = MutableStateFlow<String?>(null)
    val currentUserName: StateFlow<String?> = _currentUserName

    private val _currentUserEmail = MutableStateFlow<String?>(null)
    val currentUserEmail: StateFlow<String?> = _currentUserEmail

    init {
        try {
            Log.d("AuthViewModel", "Initializing AuthViewModel")
            val firebaseAuthSettings: FirebaseAuthSettings = auth.firebaseAuthSettings
            firebaseAuthSettings.setAppVerificationDisabledForTesting(true)
            
            // Check if user is already signed in
            auth.currentUser?.let { user ->
                if (user.isEmailVerified) {
                    viewModelScope.launch {
                        try {
                            val userDoc = FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(user.uid)
                                .get()
                                .await()

                            val isFirstLogin = userDoc.exists() && (userDoc.getBoolean("isFirstLogin") ?: true)
                            Log.d("AuthDebug", "Init - isFirstLogin value: $isFirstLogin")

                            _authState.value = AuthState(
                                isAuthenticated = true,
                                isFirstLogin = isFirstLogin
                            )
                            Log.d("AuthDebug", "Init - Updated auth state with isFirstLogin")
                        } catch (e: Exception) {
                            Log.e("AuthDebug", "Error checking first login status in init", e)
                        }
                    }
                } else {
                    auth.signOut()
                    _authState.value = AuthState(
                        error = "Please verify your email before logging in",
                        isEmailVerified = false
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error initializing AuthViewModel", e)
        }

        updateUserInfo()
    }

    private fun updateUserInfo() {
        auth.currentUser?.let { user ->
            _currentUserName.value = user.displayName
            _currentUserEmail.value = user.email
        }
    }

    fun signUp(email: String, password: String, fullName: String, country: String, phoneNumber: String) {
        if (email.isBlank() || password.isBlank() || fullName.isBlank() || country.isBlank() || phoneNumber.isBlank()) {
            _authState.value = _authState.value.copy(error = "All fields are required")
            return
        }

        viewModelScope.launch {
            try {
                // First create the user
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                
                result.user?.let { user ->
                    try {
                        // Send verification email
                        user.sendEmailVerification().await()
                        
                        // Create user profile
                        val userProfile = hashMapOf(
                            "uid" to user.uid,
                            "fullName" to fullName,
                            "email" to email,
                            "country" to country,
                            "phoneNumber" to phoneNumber,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "isFirstLogin" to true,
                            "avatarId" to 1
                        )
                        
                        Log.d("AuthDebug", "Creating new user profile with data: $userProfile")
                        
                        // Save to Firestore
                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(user.uid)
                            .set(userProfile)
                            .await()

                        // Sign out the user
                        auth.signOut()
                        
                        // Update state to show success dialog
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            verificationEmailSent = true,
                            isSignupSuccess = true,
                            error = null
                        )
                    } catch (e: Exception) {
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            error = "Failed to complete signup: ${e.message}",
                            verificationEmailSent = false,
                            isSignupSuccess = false
                        )
                    }
                }
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = when {
                        e.message?.contains("email address is already") == true -> "Email already in use"
                        e.message?.contains("badly formatted") == true -> "Invalid email format"
                        else -> "Sign up failed: ${e.message}"
                    },
                    verificationEmailSent = false,
                    isSignupSuccess = false
                )
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = _authState.value.copy(isLoading = true)
                Log.d("AuthDebug", "Starting login process")
                
                val result = auth.signInWithEmailAndPassword(email, password).await()
                
                if (result.user?.isEmailVerified == true) {
                    result.user?.uid?.let { uid ->
                        try {
                            Log.d("AuthDebug", "User verified, checking first login status for uid: $uid")
                            
                            // First check first login status
                            val userDoc = FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(uid)
                                .get()
                                .await()

                            val isFirstLogin = userDoc.exists() && (userDoc.getBoolean("isFirstLogin") ?: true)
                            Log.d("AuthDebug", "isFirstLogin value from Firestore: $isFirstLogin")

                            // Then update auth state with both authenticated and first login status
                            _authState.value = _authState.value.copy(
                                isAuthenticated = true,
                                isLoading = false,
                                isFirstLogin = isFirstLogin,
                                error = null
                            )
                            Log.d("AuthDebug", "Updated AuthState - isAuthenticated: true, isFirstLogin: $isFirstLogin")

                            // If this is first login, update the user document
                            if (isFirstLogin) {
                                Log.d("AuthDebug", "Updating isFirstLogin to false in Firestore")
                                try {
                                    FirebaseFirestore.getInstance()
                                        .collection("users")
                                        .document(uid)
                                        .update("isFirstLogin", false)
                                        .await()
                                    Log.d("AuthDebug", "Successfully updated isFirstLogin in Firestore")
                                } catch (e: Exception) {
                                    Log.e("AuthDebug", "Error updating isFirstLogin in Firestore", e)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("AuthDebug", "Error during first login check", e)
                            _authState.value = _authState.value.copy(
                                isLoading = false,
                                error = "Error checking login status: ${e.message}"
                            )
                        }
                    }
                } else {
                    Log.d("AuthDebug", "User not verified")
                    auth.signOut()
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = "Please verify your email before logging in",
                        isEmailVerified = false
                    )
                }
            } catch (e: Exception) {
                Log.e("AuthDebug", "Login error", e)
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun clearState() {
        _authState.value = AuthState()
    }

    fun setError(error: String) {
        _authState.value = _authState.value.copy(error = error)
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState()
        _currentUserName.value = null
        _currentUserEmail.value = null
        Log.d("AuthDebug", "User signed out, auth state reset")
    }

    fun resendVerificationEmail() {
        val user = auth.currentUser
        if (user != null && !user.isEmailVerified) {
            viewModelScope.launch {
                try {
                    _authState.value = _authState.value.copy(isLoading = true)
                    user.sendEmailVerification().await()
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = "Verification email sent. Please check your inbox."
                    )
                } catch (e: Exception) {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = "Failed to send verification email: ${e.message}"
                    )
                }
            }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            try {
                _authState.value = _authState.value.copy(isLoading = true)
                auth.sendPasswordResetEmail(email).await()
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = "Password reset email sent. Please check your inbox."
                )
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = when {
                        e.message?.contains("no user record") == true -> "No account found with this email"
                        e.message?.contains("badly formatted") == true -> "Invalid email format"
                        else -> "Failed to send reset email: ${e.message}"
                    }
                )
            }
        }
    }

    fun verifyAccount(email: String, code: String) {
        viewModelScope.launch {
            try {
                _authState.value = _authState.value.copy(isLoading = true)
                
                val userDoc = FirebaseFirestore.getInstance()
                    .collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .await()
                    .documents
                    .firstOrNull()

                if (userDoc != null) {
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(userDoc.id)
                        .update(
                            mapOf(
                                "isEmailVerified" to true,
                                "accountStatus" to "active"
                            )
                        )
                        .await()
                    
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        isEmailVerified = true,
                        error = "Email verified successfully"
                    )
                } else {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = "User not found"
                    )
                }
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = "Verification failed: ${e.message}"
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Remove auth state listener when ViewModel is cleared
        auth.removeAuthStateListener { }
    }

    private fun updateExistingUsers() {
        viewModelScope.launch {
            try {
                val firestore = FirebaseFirestore.getInstance()
                
                // Get all users who don't have an avatarId
                val snapshot = firestore.collection("users")
                    .whereEqualTo("avatarId", null)
                    .get()
                    .await()

                if (!snapshot.isEmpty) {
                    val batch = firestore.batch()
                    
                    snapshot.documents.forEach { doc ->
                        batch.update(doc.reference, "avatarId", 1)
                    }

                    batch.commit().await()
                    Log.d("UserUpdate", "Updated ${snapshot.size()} users with default avatarId")
                }
            } catch (e: Exception) {
                Log.e("UserUpdate", "Error updating existing users: ${e.message}")
            }
        }
    }

    fun refreshUserProfile() {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currentUser.uid)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            // Update your user state flow with new data
                            _userProfile.value = document.toObject(User::class.java)
                        }
                    }
            }
        }
    }

    fun getCurrentUserEmail() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            _currentUserEmail.value = currentUser.email
            // Also refresh the Firestore data
            firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        document.getString("email")?.let { email ->
                            _currentUserEmail.value = email
                        }
                    }
                }
        }
    }
} 