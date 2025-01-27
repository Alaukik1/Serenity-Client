package life.sochpekharoch.serenity.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthState(
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isFirstLogin: Boolean = false,
    val verificationEmailSent: Boolean = false,
    val isEmailVerified: Boolean = false,
    val isSignupSuccess: Boolean = false
)

class AuthViewModel(private val app: Application) : AndroidViewModel(app) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState

    private val _currentUserEmail = MutableStateFlow<String?>(null)
    val currentUserEmail: StateFlow<String?> = _currentUserEmail

    init {
        auth.currentUser?.let { user ->
            if (user.isEmailVerified) {
                viewModelScope.launch {
                    try {
                        val userDoc = firestore.collection("users")
                            .document(user.uid)
                            .get()
                            .await()

                        val isFirstLogin = userDoc.getBoolean("isFirstLogin") ?: false
                        
                        if (isFirstLogin) {
                            // Update isFirstLogin to false in Firestore
                            firestore.collection("users")
                                .document(user.uid)
                                .update("isFirstLogin", false)
                        }

                        _authState.value = AuthState(
                            isAuthenticated = true,
                            isFirstLogin = isFirstLogin,
                            isEmailVerified = true
                        )
                    } catch (e: Exception) {
                        Log.e("AuthDebug", "Error checking first login status", e)
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
    }

    fun signUp(email: String, password: String, phoneNumber: String = "") {
        viewModelScope.launch {
            try {
                _authState.value = _authState.value.copy(isLoading = true)
                
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener { result ->
                        result.user?.let { user ->
                            firestore.collection("users")
                                .document(user.uid)
                                .set(
                                    mapOf(
                                        "id" to user.uid,
                                        "email" to email,
                                        "phoneNumber" to phoneNumber,
                                        "isEmailVerified" to false,
                                        "accountStatus" to "pending",
                                        "isFirstLogin" to true,
                                        "createdAt" to System.currentTimeMillis()
                                    )
                                )
                                .addOnSuccessListener {
                                    user.sendEmailVerification()
                                    _authState.value = _authState.value.copy(
                                        isSignupSuccess = true,
                                        verificationEmailSent = true,
                                        isLoading = false
                                    )
                                }
                                .addOnFailureListener { e ->
                                    _authState.value = _authState.value.copy(
                                        isLoading = false,
                                        error = e.message
                                    )
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            error = e.message
                        )
                    }
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = _authState.value.copy(isLoading = true)
                
                val result = auth.signInWithEmailAndPassword(email, password).await()
                
                if (result.user?.isEmailVerified == true) {
                    val userDoc = firestore.collection("users")
                        .document(result.user!!.uid)
                        .get()
                        .await()

                    val isFirstLogin = userDoc.exists() && (userDoc.getBoolean("isFirstLogin") ?: true)
                    
                    _authState.value = _authState.value.copy(
                        isAuthenticated = true,
                        isLoading = false,
                        isFirstLogin = isFirstLogin,
                        error = null
                    )
                } else {
                    auth.signOut()
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = "Please verify your email before logging in"
                    )
                }
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun getCurrentUserEmail() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            _currentUserEmail.value = currentUser.email
        }
    }

    fun refreshUserProfile() {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                _currentUserEmail.value = currentUser.email
            }
        }
    }

    fun signOut() {
        try {
            auth.signOut()
            _authState.value = AuthState()
            _currentUserEmail.value = null
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error signing out", e)
            _authState.value = _authState.value.copy(
                error = "Failed to sign out: ${e.message}"
            )
        }
    }

    fun clearState() {
        _authState.value = AuthState()
    }

    fun setError(error: String) {
        _authState.value = _authState.value.copy(error = error)
    }

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            try {
                _authState.value = _authState.value.copy(isLoading = true)
                auth.sendPasswordResetEmail(email).await()
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun updateAuthState(isAuthenticated: Boolean, isFirstLogin: Boolean) {
        _authState.value = AuthState(isAuthenticated = isAuthenticated, isFirstLogin = isFirstLogin)
    }
} 