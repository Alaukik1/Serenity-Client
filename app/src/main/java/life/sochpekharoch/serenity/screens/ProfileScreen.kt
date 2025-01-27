package life.sochpekharoch.serenity.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import life.sochpekharoch.serenity.R
import life.sochpekharoch.serenity.Screen
import life.sochpekharoch.serenity.viewmodels.AuthViewModel
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Image
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.lifecycle.viewmodel.compose.viewModel
import android.util.Log
import life.sochpekharoch.serenity.components.AvatarSelector
import android.widget.Toast
import com.google.firebase.auth.EmailAuthProvider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: AuthViewModel,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val josefinSans = FontFamily(Font(R.font.josefin_sans))
    val robotoLight = FontFamily(Font(R.font.roboto_light))
    val abeeZee = FontFamily(Font(R.font.abeezee_regular))

    // Collect the email state
    val currentUserEmail by viewModel.currentUserEmail.collectAsState()

    // Add this to refresh the email when the screen is shown
    LaunchedEffect(Unit) {
        viewModel.getCurrentUserEmail()
    }

    var showMenu by remember { mutableStateOf(false) }
    var showAvatarSelector by remember { mutableStateOf(false) }
    var selectedAvatar by remember { mutableStateOf(1) } // Default avatar
    var userName by remember { mutableStateOf("") }
    var showSecurityDialog by remember { mutableStateOf(false) }
    var showEmailChangeDialog by remember { mutableStateOf(false) }
    var showPasswordChangeDialog by remember { mutableStateOf(false) }
    var emailChangeError by remember { mutableStateOf<String?>(null) }
    var passwordChangeError by remember { mutableStateOf<String?>(null) }

    val currentUser = FirebaseAuth.getInstance().currentUser

    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { uid ->
            userName = uid.take(6)  // Take first 6 characters of UID
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        selectedAvatar = document.getLong("avatarId")?.toInt() ?: 1
                    }
                }
        }
    }

    fun handleSignOut() {
        viewModel.signOut()
        onSignOut()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.navigateUp() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Go back",
                    tint = Color.Black
                )
            }

            Text(
                text = "My Profile",
                style = TextStyle(
                    fontFamily = josefinSans,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Normal
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 48.dp),  // Balance the layout with back button width
                textAlign = TextAlign.Center
            )
        }

        // Profile Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFFFFF1F1)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Replace Image with AnimatedAvatar
                Image(
                    painter = painterResource(
                        id = when (selectedAvatar) {
                            1 -> R.drawable.avatar_1
                            2 -> R.drawable.avatar_2
                            3 -> R.drawable.avatar_3
                            4 -> R.drawable.avatar_4
                            5 -> R.drawable.avatar_5
                            6 -> R.drawable.avatar_6
                            7 -> R.drawable.avatar_7
                            8 -> R.drawable.avatar_8
                            9 -> R.drawable.avatar_9
                            10 -> R.drawable.avatar_10
                            11 -> R.drawable.avatar_11
                            else -> R.drawable.avatar_12
                        }
                    ),
                    contentDescription = "Profile Avatar",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .clickable { showAvatarSelector = true }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Add username text
                Box(
                    modifier = Modifier
                        .background(
                            color = Color(0xFF4CAF50),  // Material Green 500
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = userName,
                        style = TextStyle(
                            fontFamily = josefinSans,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White  // Changed to white for better contrast on green
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Email Box with white background
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = currentUserEmail ?: "Loading...",
                            style = TextStyle(
                                fontFamily = robotoLight,
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                        )
                    }
                }
            }
        }

        // Options Grid
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ProfileGridItem(
                icon = painterResource(id = R.drawable.security_icon),
                text = "Security",
                isImageVector = false,
                onClick = { showSecurityDialog = true }
            )
            ProfileGridItem(
                icon = Icons.Default.Article,
                text = "My Posts",
                onClick = { navController.navigate(Screen.MyPosts.route) }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ProfileGridItem(
                icon = painterResource(id = R.drawable.app_permissions),
                text = "App\nPermissions",
                isImageVector = false,
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", "life.sochpekharoch.serenity", null)
                    }
                    context.startActivity(intent)
                }
            )
            ProfileGridItem(
                icon = painterResource(id = R.drawable.contact_support),
                text = "Support",
                isImageVector = false,
                onClick = { navController.navigate(Screen.Support.route) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { handleSignOut() },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE53935)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .height(56.dp),
            shape = RoundedCornerShape(25.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            Text(
                text = "Sign Out",
                color = Color.White,
                fontFamily = robotoLight,
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Menu Dialog
    if (showMenu) {
        Dialog(onDismissRequest = { showMenu = false }) {
            Surface(
                modifier = Modifier
                    .padding(16.dp)
                    .wrapContentSize(),
                shape = RoundedCornerShape(28.dp),
                color = Color(0xFFFFF1F1)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MenuButton(
                        text = "My profile",
                        fontFamily = abeeZee
                    ) {
                        showMenu = false
                        navController.navigate(Screen.Profile.route)
                    }
                    MenuButton(
                        text = "Support",
                        fontFamily = abeeZee
                    ) {
                        showMenu = false
                        navController.navigate(Screen.Support.route)
                    }
                    MenuButton(
                        text = "Privacy Policy",
                        fontFamily = abeeZee
                    ) {
                        showMenu = false
                        navController.navigate(Screen.PrivacyPolicy.route)
                    }
                    MenuButton(
                        text = "Terms of Service",
                        fontFamily = abeeZee
                    ) {
                        showMenu = false
                        navController.navigate(Screen.TermsOfService.route)
                    }
                }
            }
        }
    }

    if (showAvatarSelector) {
        AvatarSelector(
            selectedAvatar = selectedAvatar,
            onAvatarSelected = { avatarId ->
                selectedAvatar = avatarId
                currentUser?.uid?.let { uid ->
                    Log.d("AvatarDebug", "Saving avatarId: $avatarId for user: $uid")

                    // Update user document
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(uid)
                        .update("avatarId", avatarId)
                        .addOnSuccessListener {
                            Log.d("AvatarDebug", "Successfully saved avatarId to user document")

                            // Update all posts by this user
                            FirebaseFirestore.getInstance()
                                .collection("posts")
                                .whereEqualTo("userId", uid)
                                .get()
                                .addOnSuccessListener { snapshot ->
                                    val batch = FirebaseFirestore.getInstance().batch()

                                    snapshot.documents.forEach { doc ->
                                        batch.update(doc.reference, "userAvatarId", avatarId)
                                    }

                                    batch.commit()
                                        .addOnSuccessListener {
                                            Log.d("AvatarDebug", "Successfully updated avatarId in all posts")
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("AvatarDebug", "Failed to update posts: ${e.message}")
                                        }
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.e("AvatarDebug", "Failed to save avatarId: ${e.message}")
                        }
                }
            },
            onDismiss = { showAvatarSelector = false }
        )
    }

    if (showSecurityDialog) {
        AlertDialog(
            onDismissRequest = { showSecurityDialog = false },
            title = {
                Text(
                    text = "Security",
                    style = TextStyle(
                        fontFamily = josefinSans,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            containerColor = Color(0xFFFFF1F1),
            shape = RoundedCornerShape(28.dp),
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Email Change Button
                    Surface(
                        onClick = {
                            showSecurityDialog = false
                            showEmailChangeDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email",
                                tint = Color.Black
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Change Email ID",
                                    style = TextStyle(
                                        fontFamily = robotoLight,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Update your email address",
                                    style = TextStyle(
                                        fontFamily = robotoLight,
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                )
                            }
                        }
                    }

                    // Password Change Button
                    Surface(
                        onClick = {
                            showSecurityDialog = false
                            showPasswordChangeDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Password",
                                tint = Color.Black
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Change Password",
                                    style = TextStyle(
                                        fontFamily = robotoLight,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Update your password",
                                    style = TextStyle(
                                        fontFamily = robotoLight,
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},  // No confirm button needed
            dismissButton = {
                TextButton(onClick = { showSecurityDialog = false }) {
                    Text("Close", color = Color.Black)
                }
            }
        )
    }

    // Email Change Dialog
    if (showEmailChangeDialog) {
        EmailChangeDialog(
            onDismiss = { showEmailChangeDialog = false },
            onError = { error -> emailChangeError = error },
            onEmailUpdated = {
                showEmailChangeDialog = false
            }
        )
    }

    // Password Change Dialog
    if (showPasswordChangeDialog) {
        ChangePasswordDialog(
            onDismiss = { showPasswordChangeDialog = false },
            onError = { error -> passwordChangeError = error }
        )
    }
}

@Composable
private fun ProfileGridItem(
    icon: Any,
    text: String,
    isImageVector: Boolean = true,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(120.dp)
            .padding(8.dp)
            .clickable { onClick() }
    ) {
        if (isImageVector) {
            Icon(
                imageVector = icon as ImageVector,
                contentDescription = text,
                modifier = Modifier.size(32.dp)
            )
        } else {
            Icon(
                painter = icon as Painter,
                contentDescription = text,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            style = TextStyle(
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        )
    }
}

@Composable
private fun MenuButton(
    text: String,
    fontFamily: FontFamily,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 24.dp)
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = fontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black
            )
        )
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
                        val credential = EmailAuthProvider.getCredential(user.email!!, password)
                        user.reauthenticate(credential)
                            .addOnSuccessListener {
                                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                                    isLoading = false
                                    errorMessage = "Please enter a valid email address"
                                    return@addOnSuccessListener
                                }

                                user.verifyBeforeUpdateEmail(newEmail)
                                    .addOnSuccessListener {
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
                        val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
                        user.reauthenticate(credential)
                            .addOnSuccessListener {
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