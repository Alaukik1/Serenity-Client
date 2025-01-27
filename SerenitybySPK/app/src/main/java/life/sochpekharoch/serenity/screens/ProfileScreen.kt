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

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: AuthViewModel
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

    val currentUser = FirebaseAuth.getInstance().currentUser
    var userPhoneNumber by remember { mutableStateOf("") }

    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { uid ->
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        userPhoneNumber = document.getString("phoneNumber") ?: "No phone number"
                        selectedAvatar = document.getLong("avatarId")?.toInt() ?: 1
                    }
                }
        }
    }

    fun handleSignOut() {
        viewModel.signOut()
        // The LaunchedEffect in MainActivity will handle the navigation
        // when authState.isAuthenticated becomes false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "My Profile",
            style = TextStyle(
                fontFamily = josefinSans,
                fontSize = 24.sp,
                fontWeight = FontWeight.Normal
            ),
            modifier = Modifier.padding(bottom = 24.dp)
        )

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
                
                Spacer(modifier = Modifier.height(16.dp))
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
                            text = currentUserEmail ?: "Loading...",  // Show loading if email is null
                            style = TextStyle(
                                fontFamily = robotoLight,
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
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
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Phone",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = userPhoneNumber,
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
                onClick = { navController.navigate(Screen.Security.route) }
            )
            ProfileGridItem(
                icon = painterResource(id = R.drawable.myposts_icon),
                text = "My Posts",
                isImageVector = false,
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