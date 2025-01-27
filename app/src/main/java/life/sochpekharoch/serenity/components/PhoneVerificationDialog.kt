package life.sochpekharoch.serenity.components

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.saveable.rememberSaveable
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.FirebaseException
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import life.sochpekharoch.serenity.R
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneVerificationDialog(
    phoneNumber: String,
    onVerificationCompleted: () -> Unit,
    onDismiss: () -> Unit
) {
    var verificationCode by rememberSaveable { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    var verificationId by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        // Start phone verification when dialog is shown
        val auth = FirebaseAuth.getInstance()
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Auto-retrieval or instant verification completed
                isLoading = false
                onVerificationCompleted()
            }

            override fun onVerificationFailed(e: FirebaseException) {
                isLoading = false
                errorMessage = "Verification failed: ${e.message}"
            }

            override fun onCodeSent(
                vId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                verificationId = vId
                isLoading = false
                Toast.makeText(context, "Verification code sent", Toast.LENGTH_SHORT).show()
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(context as Activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        isLoading = true
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Verify Phone Number",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily(Font(R.font.josefin_sans))
                )
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = verificationCode,
                    onValueChange = { verificationCode = it },
                    label = { Text("Enter verification code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(19.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Gray
                    )
                )

                errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = Color.Red,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isLoading = true
                    errorMessage = null
                    try {
                        val credential = PhoneAuthProvider.getCredential(
                            verificationId, verificationCode
                        )
                        FirebaseAuth.getInstance().currentUser?.linkWithCredential(credential)
                            ?.addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) {
                                    onVerificationCompleted()
                                } else {
                                    errorMessage = "Invalid verification code"
                                }
                            }
                    } catch (e: Exception) {
                        isLoading = false
                        errorMessage = "Invalid verification code"
                    }
                },
                enabled = verificationCode.length == 6 && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFE1E1),
                    contentColor = Color.Black
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.Black
                    )
                } else {
                    Text("Verify")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.Black
                )
            ) {
                Text("Cancel")
            }
        }
    )
} 