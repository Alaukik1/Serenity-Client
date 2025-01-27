package life.sochpekharoch.serenity.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import life.sochpekharoch.serenity.R
import androidx.navigation.NavController
import life.sochpekharoch.serenity.navigation.Screen
import life.sochpekharoch.serenity.models.Post
import life.sochpekharoch.serenity.viewmodels.CommunityViewModel
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import com.google.firebase.firestore.Query
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.roundToInt
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.window.PopupProperties
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import android.util.Log
import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.foundation.Image
import life.sochpekharoch.serenity.models.PostType
import life.sochpekharoch.serenity.components.CommentDialog
import life.sochpekharoch.serenity.components.PostCard

@OptIn(
    ExperimentalMaterial3Api::class, 
    ExperimentalMaterialApi::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class
)
@Composable
fun CommunityScreen(navController: NavController) {
    val josefinSans = FontFamily(Font(R.font.josefin_sans))
    val abeeZee = FontFamily(Font(R.font.abeezee_regular))
    
    var showMenu by remember { mutableStateOf(false) }
    val viewModel: CommunityViewModel = viewModel()
    val auth = FirebaseAuth.getInstance()
    
    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showCreatePost by remember { mutableStateOf(false) }

    var refreshing by remember { mutableStateOf(false) }
    
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = {
            refreshing = true
            viewModel.refreshPosts()
            refreshing = false
        }
    )

    var showPostOptions by remember { mutableStateOf(false) }
    var selectedPost by remember { mutableStateOf<Post?>(null) }

    var showEditDialog by remember { mutableStateOf(false) }
    var editingContent by remember { mutableStateOf("") }
    
    val context = LocalContext.current

    var showDeleteConfirmation by remember { mutableStateOf(false) }

    var showCommentDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp)
            .offset(y = (-8).dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextButton(
            onClick = { showMenu = true },
            colors = ButtonDefaults.textButtonColors(contentColor = Color.Black)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Community",
                    style = TextStyle(
                        fontFamily = josefinSans,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Menu",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(top = 16.dp),
                        color = Color.Black
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(posts) { post ->
                        PostCard(
                            post = post,
                            onLikeClick = { viewModel.likePost(post.id) },
                            onDeleteClick = { 
                                selectedPost = post
                                showDeleteConfirmation = true 
                            },
                            onEditClick = {
                                selectedPost = post
                                editingContent = post.content
                                showEditDialog = true
                            },
                            onCommentClick = {
                                selectedPost = post
                                showCommentDialog = true
                            },
                            currentUserId = auth.currentUser?.uid,
                            onVoteClick = { postId, optionIndex -> 
                                viewModel.voteOnPoll(postId, optionIndex)
                            },
                            onPostClick = {
                                navController.navigate("post_detail/${post.id}")
                            }
                        )
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = refreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }

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
                    MenuButton(text = "My profile", fontFamily = abeeZee) {
                        showMenu = false
                        navController.navigate(Screen.Profile.route)
                    }
                    MenuButton(text = "Wallet", fontFamily = abeeZee) {
                        showMenu = false
                        navController.navigate(Screen.Wallet.route)
                    }
                    MenuButton(text = "Support", fontFamily = abeeZee) {
                        showMenu = false
                        navController.navigate(Screen.Support.route)
                    }
                    MenuButton(text = "Privacy Policy", fontFamily = abeeZee) {
                        showMenu = false
                        navController.navigate(Screen.PrivacyPolicy.route)
                    }
                    MenuButton(text = "Terms of Service", fontFamily = abeeZee) {
                        showMenu = false
                        navController.navigate(Screen.TermsOfService.route)
                    }
                }
            }
        }
    }

    if (showCreatePost) {
        CreatePostDialog(
            onDismiss = { showCreatePost = false },
            viewModel = viewModel,
            josefinSans = josefinSans,
            abeeZee = abeeZee
        )
    }

    if (showPostOptions && selectedPost != null) {
        Dialog(
            onDismissRequest = { 
                showPostOptions = false
                selectedPost = null
            }
        ) {
            Surface(
                modifier = Modifier
                    .width(260.dp)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(32.dp),
                shadowElevation = 4.dp,
                color = Color(0xFFFFF1F1)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 28.dp)
                ) {
                    if (selectedPost?.userId == auth.currentUser?.uid) {
                        TextButton(
                            onClick = {
                                showPostOptions = false
                                editingContent = selectedPost?.content ?: ""
                                showEditDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Edit Post",
                                style = TextStyle(
                                    fontFamily = abeeZee,
                                    fontSize = 20.sp,
                                    color = Color.Black
                                ),
                                modifier = Modifier.padding(vertical = 18.dp)
                            )
                        }
                        
                        Divider(color = Color.LightGray.copy(alpha = 0.3f))
                        
                        TextButton(
                            onClick = {
                                showPostOptions = false
                                showDeleteConfirmation = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Delete Post",
                                style = TextStyle(
                                    fontFamily = abeeZee,
                                    fontSize = 20.sp,
                                    color = Color.Red
                                ),
                                modifier = Modifier.padding(vertical = 20.dp)
                            )
                        }
                        
                        Divider(color = Color.LightGray.copy(alpha = 0.3f))
                    }
                    
                    TextButton(
                        onClick = {
                            selectedPost?.let { post ->
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, viewModel.getShareableLink(post.id))
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Post"))
                            }
                            showPostOptions = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Share Post",
                            style = TextStyle(
                                fontFamily = abeeZee,
                                fontSize = 20.sp,
                                color = Color.Black
                            ),
                            modifier = Modifier.padding(vertical = 20.dp)
                        )
                    }
                }
            }
        }
    }

    if (showEditDialog && selectedPost != null) {
        Dialog(onDismissRequest = { showEditDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFFF1F1)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Edit Post",
                        style = TextStyle(
                            fontFamily = josefinSans,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = editingContent,
                        onValueChange = { editingContent = it },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 5,
                        textStyle = TextStyle(fontFamily = abeeZee)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showEditDialog = false }
                        ) {
                            Text("Cancel", fontFamily = abeeZee)
                        }
                        
                        Button(
                            onClick = {
                                selectedPost?.let { post ->
                                    viewModel.editPost(post.id, editingContent)
                                }
                                showEditDialog = false
                            },
                            enabled = editingContent.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFC8C8),
                                contentColor = Color.Red
                            )
                        ) {
                            Text(
                                text = "Save", 
                                fontFamily = abeeZee
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirmation && selectedPost != null) {
        Dialog(
            onDismissRequest = { 
                showDeleteConfirmation = false
                selectedPost = null
            }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                shape = RoundedCornerShape(32.dp),
                color = Color(0xFFFFF1F1)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Delete Post?",
                        style = TextStyle(
                            fontFamily = josefinSans,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        ),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Are you sure you want to delete this post? Confirming this action will completely remove the post from Serenity and this action cannot be undone.",
                        style = TextStyle(
                            fontFamily = abeeZee,
                            fontSize = 16.sp,
                            color = Color.Black
                        ),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TextButton(
                            onClick = { 
                                showDeleteConfirmation = false
                                selectedPost = null
                            }
                        ) {
                            Text(
                                text = "Cancel",
                                style = TextStyle(
                                    fontFamily = abeeZee,
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                            )
                        }
                        
                        Button(
                            onClick = { 
                                selectedPost?.let { post ->
                                    viewModel.deletePost(post.id)
                                    viewModel.refreshPosts()
                                }
                                showDeleteConfirmation = false
                                selectedPost = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFC8C8),
                                contentColor = Color.Red
                            )
                        ) {
                            Text(
                                text = "Delete",
                                style = TextStyle(
                                    fontFamily = abeeZee,
                                    fontSize = 16.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCommentDialog && selectedPost != null) {
        CommentDialog(
            post = selectedPost!!,
            onDismiss = { 
                showCommentDialog = false
                selectedPost = null
            },
            viewModel = viewModel
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

@Composable
fun CreatePostDialog(
    onDismiss: () -> Unit,
    viewModel: CommunityViewModel,
    josefinSans: FontFamily,
    abeeZee: FontFamily
) {
    var content by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isPosting by remember { mutableStateOf(false) }
    
    val context = LocalContext.current

    // Declare imagePicker first
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        Log.d("CreatePostDialog", "Selected image URI: $uri")
    }
    
    // Then declare permissionLauncher that uses imagePicker
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            imagePicker.launch("image/*")
        } else {
            Toast.makeText(context, "Permission required to select image", Toast.LENGTH_SHORT).show()
        }
    }

    // Function that uses both launchers
    fun checkAndRequestPermission() {
        when {
            ContextCompat.checkSelfPermission(
                context,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_IMAGES
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }
            ) == PackageManager.PERMISSION_GRANTED -> {
                imagePicker.launch("image/*")
            }
            else -> {
                permissionLauncher.launch(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Manifest.permission.READ_MEDIA_IMAGES
                    } else {
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    }
                )
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFFF1F1)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Create Post",
                    style = TextStyle(
                        fontFamily = josefinSans,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("What's on your mind?", fontFamily = abeeZee) },
                    maxLines = 5,
                    textStyle = TextStyle(fontFamily = abeeZee)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = { checkAndRequestPermission() }
                    ) {
                        Text("Add Image", fontFamily = abeeZee)
                    }
                    
                    selectedImageUri?.let {
                        Text("Image selected", color = Color.Green, fontFamily = abeeZee)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("Cancel", fontFamily = abeeZee)
                    }
                    
                    Button(
                        onClick = {
                            isPosting = true
                            Log.d("CreatePostDialog", "Creating post with content: $content and image: $selectedImageUri")
                            viewModel.createPost(
                                content = content,
                                type = if (selectedImageUri != null) PostType.IMAGE else PostType.TEXT,
                                imageUri = selectedImageUri
                            )
                            onDismiss()
                        },
                        enabled = content.isNotBlank() && !isPosting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White,
                            disabledContainerColor = Color.Black.copy(alpha = 0.5f),
                            disabledContentColor = Color.White.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        if (isPosting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "Post",
                                fontFamily = abeeZee,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}