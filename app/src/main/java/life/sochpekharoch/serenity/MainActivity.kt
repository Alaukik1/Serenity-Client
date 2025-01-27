package life.sochpekharoch.serenity

// Android imports
import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import android.content.Intent
import life.sochpekharoch.serenity.services.ContentBotService

// AndroidX imports
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.viewModels

// Compose imports
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

// Navigation
import androidx.navigation.*
import androidx.navigation.compose.*

// Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

// Project imports
import life.sochpekharoch.serenity.components.*
import life.sochpekharoch.serenity.models.PostType
import life.sochpekharoch.serenity.screens.*
import life.sochpekharoch.serenity.ui.permissions.RequestPermissions
import life.sochpekharoch.serenity.ui.theme.SerenityTheme
import life.sochpekharoch.serenity.update.CustomUpdateManager
import life.sochpekharoch.serenity.utils.FCMTokenManager
import life.sochpekharoch.serenity.utils.NetworkUtils
import life.sochpekharoch.serenity.utils.PreferenceHelper
import life.sochpekharoch.serenity.viewmodels.*
import life.sochpekharoch.serenity.utils.PermissionHandler

// Third party
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

// Add this import statement if it's not already present
import life.sochpekharoch.serenity.screens.MedimeetScreen
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity

sealed class Screen(val route: String) {
    object Community : Screen("community")
    object SnapHelp : Screen("snaphelp")
    object MediMeet : Screen("medimeet")
    object Notification : Screen("notification")
    object Wallet : Screen("wallet")
    object Profile : Screen("profile")
    object Support : Screen("support")
    object PrivacyPolicy : Screen("privacy_policy")
    object TermsOfService : Screen("terms_of_service")
    object Security : Screen("security")
    object MyPosts : Screen("my_posts")
    object ImagePosts : Screen("image_posts")
}

@Composable
fun AppNavigation() {
    val auth = FirebaseAuth.getInstance()
    val authViewModel: AuthViewModel = viewModel()
    val navController = rememberNavController()
    
    // Add this to track initial auth check
    var isAuthChecked by remember { mutableStateOf(false) }
    val authState by authViewModel.authState.collectAsState()
    
    // Add back the navigation bar color
    val navigationBarColor = Color(0xFFFFEDED)
    
    // Add back the state variables
    var showWelcomeDialog by remember { mutableStateOf(false) }
    var showAvatarSelector by remember { mutableStateOf(false) }
    var showCommunityExplanation by remember { mutableStateOf(false) }
    var showSnapHelpExplanation by remember { mutableStateOf(false) }
    var showMedimeetExplanation by remember { mutableStateOf(false) }
    var showCreatePostDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    // Check auth state when composable is first created
    LaunchedEffect(Unit) {
        // If user is already logged in, update auth state
        if (auth.currentUser != null) {
            authViewModel.updateAuthState(true, false)
        }
        isAuthChecked = true
    }

    // Only show content after auth check is complete
    if (isAuthChecked) {
        if (!authState.isAuthenticated) {
            LoginScreen(
                navController = navController,
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Community.route) {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                }
            )
        } else {
            // Main app content
            val viewModel: CommunityViewModel = viewModel()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            
            val shouldShowBottomNav = remember(currentRoute) {
                when (currentRoute) {
                    Screen.SnapHelp.route, Screen.MediMeet.route -> false
                    else -> true
                }
            }

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.White,
                bottomBar = {
                    if (shouldShowBottomNav) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                                    .fillMaxWidth()
                                    .height(70.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(32.dp)),
                                    color = navigationBarColor,
                                    tonalElevation = 2.dp,
                                    shadowElevation = 2.dp,
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                                        val currentRoute = navBackStackEntry?.destination?.route

                                        NavigationIcon(
                                            screen = Screen.Community,
                                            currentRoute = currentRoute,
                                            navController = navController,
                                            isBottomNavigation = true
                                        )
                                        NavigationIcon(
                                            screen = Screen.SnapHelp,
                                            currentRoute = currentRoute,
                                            navController = navController,
                                            isBottomNavigation = true
                                        )

                                        // Center create button
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.width(56.dp)
                                        ) {
                                            CreateButton(
                                                icon = R.drawable.add_icon,
                                                description = "Create Post"
                                            ) {
                                                showCreatePostDialog = true
                                            }
                                        }

                                        NavigationIcon(
                                            screen = Screen.MediMeet,
                                            currentRoute = currentRoute,
                                            navController = navController,
                                            isBottomNavigation = true
                                        )
                                        NavigationIcon(
                                            screen = Screen.Notification,
                                            currentRoute = currentRoute,
                                            navController = navController,
                                            isBottomNavigation = true
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            ) { paddingValues ->
                NavHost(
                    navController = navController,
                    startDestination = Screen.Community.route,
                    modifier = Modifier.padding(paddingValues)
                ) {
                    composable(Screen.Community.route) { 
                        CommunityScreen(navController = navController)
                    }
                    composable(Screen.SnapHelp.route) { 
                        SnapHelpScreen(
                            viewModel = viewModel(),
                            navController = navController
                        )
                    }
                    composable(Screen.MediMeet.route) { 
                        MedimeetScreen(
                            navController = navController
                        )
                    }
                    composable(Screen.Notification.route) { 
                        NotificationScreen(navController = navController)
                    }
                    composable(Screen.Wallet.route) { 
                        val walletViewModel: WalletViewModel = viewModel()
                        WalletScreen(
                            navController = navController,
                            viewModel = walletViewModel
                        )
                    }
                    composable(Screen.Profile.route) {
                        ProfileScreen(
                            viewModel = authViewModel,
                            onSignOut = {
                                // Clear navigation stack and go back to login
                                navController.navigate("login") {
                                    popUpTo(0) {
                                        inclusive = true
                                    }
                                }
                            },
                            navController = navController
                        )
                    }
                    composable(Screen.Support.route) { 
                        SupportScreen(navController = navController)
                    }
                    composable(Screen.PrivacyPolicy.route) { 
                        PrivacyPolicyScreen(navController = navController)
                    }
                    composable(Screen.TermsOfService.route) { 
                        TermsOfServiceScreen(navController = navController)
                    }
                    composable("security") {
                        SecurityScreen(
                            navController = navController,
                            onEmailUpdated = {
                                authViewModel.getCurrentUserEmail()
                            }
                        )
                    }
                    composable(Screen.MyPosts.route) { 
                        MyPostsScreen(navController = navController)
                    }
                    composable(Screen.ImagePosts.route) { 
                        ImagePostsScreen(navController = navController)
                    }
                    composable(
                        route = "post_detail/{postId}",
                        arguments = listOf(navArgument("postId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        PostDetailScreen(
                            navController = navController,
                            postId = backStackEntry.arguments?.getString("postId") ?: ""
                        )
                    }
                }

                // Dialogs
                if (showWelcomeDialog) {
                    WelcomeDialog(
                        onDismiss = {
                            showWelcomeDialog = false
                            // Navigate to Community screen after dismissing
                            navController.navigate(Screen.Community.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onGetStarted = {
                            showWelcomeDialog = false
                            showAvatarSelector = true
                            // Navigate to Community screen
                            navController.navigate(Screen.Community.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }

                if (showAvatarSelector) {
                    AvatarSelector(
                        selectedAvatar = 1,
                        onAvatarSelected = { avatarId ->
                            auth.currentUser?.uid?.let { uid ->
                                FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(uid)
                                    .update("avatarId", avatarId)
                                    .addOnSuccessListener {
                                        showAvatarSelector = false
                                        PreferenceHelper.setFirstLoginComplete(context)
                                        // Navigate to Community screen
                                        navController.navigate(Screen.Community.route) {
                                            popUpTo(navController.graph.startDestinationId) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                        // Show community explanation
                                        showCommunityExplanation = true
                                    }
                            }
                        },
                        onDismiss = {
                            showAvatarSelector = false
                            PreferenceHelper.setFirstLoginComplete(context)
                            // Navigate to Community screen
                            navController.navigate(Screen.Community.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                            // Show community explanation
                            showCommunityExplanation = true
                        }
                    )
                }

                if (showCommunityExplanation) {
                    CommunityExplanationDialog(
                        onDismiss = {
                            showCommunityExplanation = false
                            // Navigate to SnapHelp and show its explanation
                            navController.navigate(Screen.SnapHelp.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                            showSnapHelpExplanation = true
                        }
                    )
                }

                if (showSnapHelpExplanation) {
                    SnapHelpExplanationDialog(
                        onDismiss = {
                            showSnapHelpExplanation = false
                            navController.navigate(Screen.MediMeet.route)
                            showMedimeetExplanation = true
                        }
                    )
                }

                if (showMedimeetExplanation) {
                    MedimeetExplanationDialog(
                        onDismiss = { 
                            showMedimeetExplanation = false
                            // Navigate to Community screen after all explanations
                            navController.navigate(Screen.Community.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                if (showCreatePostDialog) {
                    val context = LocalContext.current
                    CreatePostDialog(
                        onDismiss = { showCreatePostDialog = false },
                        onCreatePost = { text, postType, mediaUri ->
                            showCreatePostDialog = false
                            when (postType) {
                                PostType.POLL -> {
                                    // Create poll post
                                    viewModel.createPollPost(text)
                                }
                                else -> {
                                    // Create regular post
                                    viewModel.createPost(
                                        content = text,
                                        type = postType,
                                        mediaUri = mediaUri
                                    )
                                }
                            }
                            // Navigate to Community screen after creating any type of post
                            navController.navigate(Screen.Community.route) {
                                popUpTo(Screen.Community.route) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NavigationIcon(
    screen: Screen,
    currentRoute: String?,
    navController: NavController,
    isBottomNavigation: Boolean = false
) {
    var showMenu by remember { mutableStateOf(false) }
    val abeeZee = FontFamily(Font(R.font.abeezee_regular))
    val isSelected = currentRoute == screen.route
    
    // Define the theme color for selected state
    val selectedColor = Color(0xFFFF7171)  // Light red color matching your app's theme

    IconButton(
        onClick = {
            if (!isSelected) {
                navController.navigate(screen.route) {
                    popUpTo(0) // This will clear the entire back stack
                    launchSingleTop = true
                }
            }
        },
        modifier = Modifier.size(48.dp)
    ) {
        Icon(
            painter = painterResource(
                id = when (screen) {
                    Screen.Community -> R.drawable.ic_community
                    Screen.SnapHelp -> R.drawable.ic_snaphelp
                    Screen.MediMeet -> R.drawable.ic_medimeet
                    Screen.Notification -> R.drawable.ic_notification
                    else -> R.drawable.ic_community
                }
            ),
            contentDescription = screen.route,
            tint = if (isSelected) selectedColor else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun CreateButton(
    icon: Int,
    description: String,
    onClick: () -> Unit = {}
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier
            .height(45.dp)
            .width(56.dp)
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = description,
            modifier = Modifier.size(24.dp),
            tint = Color.White
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

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SerenityTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNavigation()
                }
            }
        }
    }
}