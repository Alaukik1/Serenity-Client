package life.sochpekharoch.serenity.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import life.sochpekharoch.serenity.R
import life.sochpekharoch.serenity.components.PostCard
import life.sochpekharoch.serenity.models.PostType
import life.sochpekharoch.serenity.viewmodels.MyPostsViewModel
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.ArrowBack
import life.sochpekharoch.serenity.models.Post
import life.sochpekharoch.serenity.components.RepostDialog

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MyPostsScreen(
    navController: NavController,
    viewModel: MyPostsViewModel = viewModel()
) {
    val josefinSans = FontFamily(Font(R.font.josefin_sans))
    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val selectedPostType by viewModel.selectedPostType.collectAsState()
    
    // Calculate counts
    val textPostsCount = posts.count { it.type == PostType.TEXT }
    val imagePostsCount = posts.count { it.type == PostType.IMAGE }
    val pollPostsCount = posts.count { it.type == PostType.POLL }

    // Add debug logging for state changes
    LaunchedEffect(Unit) {
        Log.d("MyPostsScreen", """
            Screen initialized:
            - Current user ID: $currentUserId
            - Initial posts count: ${posts.size}
            - Is loading: $isLoading
            - Has error: ${error != null}
        """.trimIndent())
    }

    // Add debug logging for posts changes
    LaunchedEffect(posts) {
        Log.d("MyPostsScreen", """
            Posts updated:
            - New count: ${posts.size}
            - First post ID: ${posts.firstOrNull()?.id}
            - First post content: ${posts.firstOrNull()?.content?.take(50)}
        """.trimIndent())
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = { viewModel.refreshPosts() }
    )

    var showRepostDialog by remember { mutableStateOf(false) }
    var selectedPost: Post? by remember { mutableStateOf(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Add top bar with back button and title
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
                    text = "My Posts",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = josefinSans
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 48.dp),  // Balance the layout with back button width
                    textAlign = TextAlign.Center
                )
            }

            // Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Default.AllInclusive,
                    count = posts.size.toString(),
                    isSelected = selectedPostType == null,
                    onClick = { viewModel.setPostTypeFilter(null) }
                )
                StatItem(
                    icon = Icons.Default.TextFields,
                    count = textPostsCount.toString(),
                    isSelected = selectedPostType == PostType.TEXT,
                    onClick = { viewModel.setPostTypeFilter(PostType.TEXT) }
                )
                StatItem(
                    icon = Icons.Default.Image,
                    count = imagePostsCount.toString(),
                    isSelected = selectedPostType == PostType.IMAGE,
                    onClick = { viewModel.setPostTypeFilter(PostType.IMAGE) }
                )
                StatItem(
                    icon = Icons.Default.Poll,
                    count = pollPostsCount.toString(),
                    isSelected = selectedPostType == PostType.POLL,
                    onClick = { viewModel.setPostTypeFilter(PostType.POLL) }
                )
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            
            if (error != null) {
                Text(
                    text = error ?: "Unknown error",
                    color = Color.Red,
                    modifier = Modifier.padding(8.dp)
                )
            }
            
            if (posts.isEmpty() && !isLoading) {
                Text(
                    text = when(selectedPostType) {
                        null -> "No posts yet"
                        PostType.TEXT -> "No text posts yet"
                        PostType.IMAGE -> "No image posts yet"
                        PostType.POLL -> "No polls yet"
                        else -> "No posts yet"
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }

            LazyColumn {
                items(posts) { post ->
                    PostCard(
                        post = post,
                        onPostClick = { navController.navigate("post_detail/${post.id}") },
                        onLikeClick = { /* No-op */ },
                        onDeleteClick = { viewModel.deletePost(post.id) },
                        onEditClick = { /* No-op */ },
                        onCommentClick = { /* No-op */ },
                        onRepostClick = { 
                            selectedPost = post
                            showRepostDialog = true
                        },
                        currentUserId = currentUserId,
                        onVoteClick = { _, _ -> /* No-op */ },
                        isAdmin = false,
                        isDetailView = false,
                        isReposted = post.repostedBy.contains(currentUserId)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        PullRefreshIndicator(
            refreshing = isLoading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }

    if (showRepostDialog) {
        RepostDialog(
            onDismiss = { showRepostDialog = false },
            onConfirm = {
                selectedPost?.let { post ->
                    viewModel.repostPost(post)
                }
                showRepostDialog = false
            },
            isLoading = viewModel.isReposting.collectAsState().value
        )
    }
}

@Composable
private fun StatItem(
    icon: ImageVector? = null,
    painter: Painter? = null,
    count: String,
    useCustomIcon: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clickable { onClick() }
            .background(
                if (isSelected) Color(0xFFFFF1F1) else Color.Transparent,
                RoundedCornerShape(16.dp)
            )
            .padding(8.dp)
    ) {
        if (useCustomIcon && painter != null) {
            Icon(
                painter = painter,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = count,
            style = TextStyle(
                fontSize = 16.sp,
                color = Color.Black
            ),
            modifier = Modifier.padding(start = 4.dp)
        )
    }
} 