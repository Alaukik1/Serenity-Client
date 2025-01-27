package life.sochpekharoch.serenity.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import life.sochpekharoch.serenity.R
import androidx.compose.runtime.*
import androidx.compose.material3.OutlinedTextField
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.google.firebase.auth.EmailAuthProvider
import android.content.Context
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SecurityScreen(
    navController: NavController,
    onEmailUpdated: () -> Unit = {}
) {
    var showEmailChangeDialog by remember { mutableStateOf(false) }
    var showPasswordChangeDialog by remember { mutableStateOf(false) }
    var emailChangeError by remember { mutableStateOf<String?>(null) }
    var passwordChangeError by remember { mutableStateOf<String?>(null) }
    val josefinSans = FontFamily(Font(R.font.josefin_sans))
    val robotoLight = FontFamily(Font(R.font.roboto_light))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.popBackStack() }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text = "Security",
                style = TextStyle(
                    fontFamily = josefinSans,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            // Empty box for alignment
            Box(modifier = Modifier.size(48.dp))
        }

        // Security Options
        SecurityOption(
            icon = Icons.Default.Email,
            title = "Change Email ID",
            subtitle = "Update your email address",
            onClick = { showEmailChangeDialog = true }
        )

        SecurityOption(
            icon = Icons.Default.Lock,
            title = "Change Password",
            subtitle = "Update your password",
            onClick = { showPasswordChangeDialog = true }
        )
    }

    // Add the dialog
    if (showEmailChangeDialog) {
        EmailChangeDialog(
            onDismiss = { showEmailChangeDialog = false },
            onError = { error -> emailChangeError = error },
            onEmailUpdated = {
                onEmailUpdated()
                showEmailChangeDialog = false
            }
        )
    }

    // Add password change dialog
    if (showPasswordChangeDialog) {
        ChangePasswordDialog(
            onDismiss = { showPasswordChangeDialog = false },
            onError = { error -> passwordChangeError = error }
        )
    }
}

@Composable
private fun SecurityOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFFF1F1)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.roboto_light)),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = subtitle,
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.roboto_light)),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Navigate",
                tint = Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmailChangeDialog(
    onDismiss: () -> Unit,
    onError: (String) -> Unit,
    onEmailUpdated: () -> Unit
) {
    var newEmail by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Change Email",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily(Font(R.font.josefin_sans))
                )
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = newEmail,
                    onValueChange = { newEmail = it },
                    label = { Text("New Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(19.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Gray
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Current Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
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
                    currentUser?.let { user ->
                        // First verify the current password
                        val credential = EmailAuthProvider.getCredential(user.email!!, password)
                        user.reauthenticate(credential)
                            .addOnSuccessListener {
                                // After successful reauthentication, verify the new email format
                                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                                    isLoading = false
                                    errorMessage = "Please enter a valid email address"
                                    return@addOnSuccessListener
                                }
                                
                                // Then update the email
                                user.verifyBeforeUpdateEmail(newEmail)
                                    .addOnSuccessListener {
                                        // Update email in Firestore
                                        val firestore = FirebaseFirestore.getInstance()
                                        firestore.collection("users")
                                            .document(user.uid)
                                            .update("email", newEmail)
                                            .addOnSuccessListener {
                                                isLoading = false
                                                Toast.makeText(
                                                    context, 
                                                    "Verification email sent to $newEmail. Please verify to complete the change.", 
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                onEmailUpdated()
                                            }
                                            .addOnFailureListener { e ->
                                                isLoading = false
                                                errorMessage = "Failed to update profile: ${e.message}"
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false
                                        errorMessage = "Failed to update email: ${e.message}"
                                    }
                            }
                            .addOnFailureListener { e ->
                                isLoading = false
                                errorMessage = "Incorrect password"
                            }
                    }
                },
                enabled = newEmail.isNotBlank() && password.isNotBlank() && !isLoading,
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
                    Text("Update")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onError: (String) -> Unit
) {
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Change Password",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily(Font(R.font.josefin_sans))
                )
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Current Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(19.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Gray
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(19.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
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
                    if (newPassword != confirmPassword) {
                        errorMessage = "New passwords don't match"
                        return@Button
                    }
                    if (newPassword.length < 6) {
                        errorMessage = "Password must be at least 6 characters"
                        return@Button
                    }
                    
                    isLoading = true
                    errorMessage = null
                    currentUser?.let { user ->
                        // Re-authenticate user
                        val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
                        user.reauthenticate(credential)
                            .addOnSuccessListener {
                                // Update password
                                user.updatePassword(newPassword)
                                    .addOnSuccessListener {
                                        isLoading = false
                                        Toast.makeText(
                                            context,
                                            "Password updated successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        onDismiss()
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false
                                        errorMessage = "Failed to update password: ${e.message}"
                                    }
                            }
                            .addOnFailureListener {
                                isLoading = false
                                errorMessage = "Current password is incorrect"
                            }
                    }
                },
                enabled = currentPassword.isNotBlank() && 
                         newPassword.isNotBlank() && 
                         confirmPassword.isNotBlank() && 
                         !isLoading,
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
                    Text("Update")
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