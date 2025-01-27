package life.sochpekharoch.serenity

import android.animation.Animator
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.*
import androidx.navigation.*
import life.sochpekharoch.serenity.screens.CommunityScreen
import life.sochpekharoch.serenity.screens.MedimeetScreen
import life.sochpekharoch.serenity.screens.SnaphelpScreen
import life.sochpekharoch.serenity.screens.WaitingForExpertScreen
import life.sochpekharoch.serenity.screens.WalletScreen
import life.sochpekharoch.serenity.screens.ProfileScreen
import life.sochpekharoch.serenity.screens.SupportScreen
import life.sochpekharoch.serenity.screens.PrivacyPolicyScreen
import life.sochpekharoch.serenity.screens.TermsOfServiceScreen
import life.sochpekharoch.serenity.screens.SecurityScreen
import life.sochpekharoch.serenity.screens.MyPostsScreen
import life.sochpekharoch.serenity.screens.ImagePostsScreen
import life.sochpekharoch.serenity.ui.theme.SerenityTheme
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import android.view.View
import android.animation.ObjectAnimator
import androidx.core.app.ActivityCompat
import android.Manifest
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import life.sochpekharoch.serenity.ui.permissions.RequestPermissions
import life.sochpekharoch.serenity.update.CustomUpdateManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.background
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.font.FontWeight
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import android.util.Log
import life.sochpekharoch.serenity.utils.FCMTokenManager
import life.sochpekharoch.serenity.utils.NetworkUtils
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import android.app.ActivityManager
import android.content.pm.PackageManager
import life.sochpekharoch.serenity.models.PostType
import life.sochpekharoch.serenity.components.CreatePostDialog
import androidx.lifecycle.viewmodel.compose.viewModel
import life.sochpekharoch.serenity.viewmodels.CommunityViewModel
import life.sochpekharoch.serenity.screens.LoginScreen
import life.sochpekharoch.serenity.viewmodels.AuthViewModel
import life.sochpekharoch.serenity.components.WelcomeDialog
import life.sochpekharoch.serenity.utils.PreferenceHelper
import com.google.firebase.firestore.FirebaseFirestore
import life.sochpekharoch.serenity.components.AvatarSelector
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material3.AlertDialog
import life.sochpekharoch.serenity.components.CommunityExplanationDialog
import life.sochpekharoch.serenity.components.SnapHelpExplanationDialog
import life.sochpekharoch.serenity.screens.NotificationScreen
import life.sochpekharoch.serenity.components.MedimeetExplanationDialog
import life.sochpekharoch.serenity.viewmodels.WalletViewModel
import life.sochpekharoch.serenity.screens.PostDetailScreen
import life.sochpekharoch.serenity.ui.theme.SerenityTheme

sealed class Screen(val route: String) {
    object Community : Screen("community")
    object Snaphelp : Screen("snaphelp")
    object Medimeet : Screen("medimeet")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialApp() {
    val auth = FirebaseAuth.getInstance()
    var isAuthenticated by remember { mutableStateOf(false) }
    val authViewModel: AuthViewModel = viewModel()
    val context = LocalContext.current
    val navController = rememberNavController()
    
    val authState by authViewModel.authState.collectAsState()
    var showWelcomeDialog by remember { mutableStateOf(false) }
    var showAvatarSelector by remember { mutableStateOf(false) }
    var showCommunityExplanation by remember { mutableStateOf(false) }
    var showSnapHelpExplanation by remember { mutableStateOf(false) }
    var showMedimeetExplanation by remember { mutableStateOf(false) }
    var showCreatePostDialog by remember { mutableStateOf(false) }

    // Add this effect to track state changes
    LaunchedEffect(Unit) {
        Log.d("MainDebug", "Initial auth state: $authState")
    }

    // Track auth state changes
    LaunchedEffect(authState) {
        Log.d("MainDebug", """
            Auth state changed:
            - isAuthenticated: ${authState.isAuthenticated}
            - isFirstLogin: ${authState.isFirstLogin}
            - showWelcomeDialog: $showWelcomeDialog
        """.trimIndent())

        if (authState.isAuthenticated) {
            if (authState.isFirstLogin) {
                Log.d("MainDebug", "Setting showWelcomeDialog to true")
                showWelcomeDialog = true
                // Navigate to Community screen immediately after authentication
                navController.navigate(Screen.Community.route) {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        }
    }

    if (!authState.isAuthenticated) {
        LoginScreen(
            onLoginSuccess = {
                Log.d("MainDebug", "Login success, isFirstLogin: ${authState.isFirstLogin}")
                if (authState.isFirstLogin) {
                    showWelcomeDialog = true
                    // Navigate to Community screen immediately after login
                    navController.navigate(Screen.Community.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            }
        )
    } else {
        // Main app content
        val viewModel: CommunityViewModel = viewModel()
        val navigationBarColor = Color(0xFFFFEDED)

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.White,
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    // Keep the navigation bar in its original position
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
                                    screen = Screen.Snaphelp,
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
                                    screen = Screen.Medimeet,
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
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screen.Community.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(Screen.Community.route) { 
                    CommunityScreen(navController = navController)
                }
                composable(Screen.Snaphelp.route) { 
                    val walletViewModel: WalletViewModel = viewModel()
                    SnaphelpScreen(
                        navController = navController,
                        viewModel = walletViewModel
                    )
                }
                composable(Screen.Medimeet.route) { 
                    MedimeetScreen(navController = navController)
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
                        navController = navController,
                        viewModel = authViewModel
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
                        navController.navigate(Screen.Snaphelp.route) {
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
                        navController.navigate(Screen.Medimeet.route)
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
                CreatePostDialog(
                    onDismiss = { showCreatePostDialog = false },
                    onCreatePost = { text, postType, imageUri ->
                        showCreatePostDialog = false
                        when (postType) {
                            PostType.POLL -> {
                                viewModel.createPollPost(text)
                            }
                            else -> {
                                viewModel.createPost(
                                    content = text,
                                    type = postType,
                                    imageUri = imageUri
                                )
                            }
                        }
                        // Navigate to Community screen after creating post
                        navController.navigate(Screen.Community.route) {
                            popUpTo(Screen.Community.route) { inclusive = true }
                        }
                    }
                )
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
                    Screen.Snaphelp -> R.drawable.ic_snaphelp
                    Screen.Medimeet -> R.drawable.ic_medimeet
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

class MainActivity : ComponentActivity() {
    private lateinit var fcmTokenManager: FCMTokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d("MainActivity", "About to print key hash...")
        try {
            KeyHashPrinter.printKeyHash(this)
            Log.d("MainActivity", "Key hash printed successfully")
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to print key hash", e)
        }
        
        try {
            // Initialize Firebase if not already initialized
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to initialize Firebase: ${e.message}")
        }

        // Initialize FCMTokenManager first
        fcmTokenManager = FCMTokenManager(this)
        
        // Subscribe to FCM topic for broadcast messages
        FirebaseMessaging.getInstance().subscribeToTopic("all_users")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FCM", "Subscribed to all_users topic")
                } else {
                    Log.e("FCM", "Failed to subscribe to all_users topic", task.exception)
                }
            }
        
        // Get FCM token in a coroutine
        lifecycleScope.launch {
            try {
                val token = fcmTokenManager.getFCMToken()
                if (token != null) {
                    Log.d("FCM", "FCM Token: $token")
                    Toast.makeText(this@MainActivity, "FCM Token: $token", Toast.LENGTH_SHORT).show()
                    
                    // Log additional info for debugging
                    Log.d("FCM", "Token length: ${token.length}")
                    Log.d("FCM", "Is app in foreground: ${isAppInForeground()}")
                    Log.d("FCM", "Notification permission granted: ${isNotificationPermissionGranted()}")
                }
            } catch (e: Exception) {
                Log.e("FCM", "Error getting FCM token", e)
                e.printStackTrace()
            }
        }

        val splashScreen = installSplashScreen()

        // Set status bar color to white
        window.statusBarColor = android.graphics.Color.WHITE
        // Set status bar icons/text to dark
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        splashScreen.setKeepOnScreenCondition { false }

        splashScreen.setOnExitAnimationListener { splashScreenView ->
            val alpha = ObjectAnimator.ofFloat(
                splashScreenView.view,
                View.ALPHA,
                1f,
                0f
            )
            alpha.duration = 1000L
            alpha.addListener(object : Animator.AnimatorListener {
                override fun onAnimationEnd(animation: Animator) {
                    splashScreenView.remove()
                }

                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
            alpha.start()
        }

        Checkout.preload(applicationContext)

        // Initialize update checker
        val updateManager = CustomUpdateManager(this)
        
        // Check for updates (in a coroutine)
        lifecycleScope.launch {
            updateManager.checkForUpdate()
        }

        setContent {
            SerenityTheme {
                MaterialApp()
            }
        }
    }

    private fun isAppInForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        val packageName = packageName
        for (appProcess in appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND 
                && appProcess.processName == packageName) {
                return true
            }
        }
        return false
    }

    private fun isNotificationPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun showTestNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "test_channel",
                "Test Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Build notification
        val notification = NotificationCompat.Builder(this, "test_channel")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Test Local Notification")
            .setContentText("This is a test notification")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // Show notification
        notificationManager.notify(1, notification)
        Log.d("MainActivity", "Test notification sent")
    }
}