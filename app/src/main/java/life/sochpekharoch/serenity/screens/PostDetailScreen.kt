package life.sochpekharoch.serenity.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import life.sochpekharoch.serenity.components.PostCard
import life.sochpekharoch.serenity.viewmodels.CommunityViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import java.text.SimpleDateFormat
import java.util.*
import life.sochpekharoch.serenity.models.Comment
import androidx.compose.foundation.shape.RoundedCornerShape
import life.sochpekharoch.serenity.R
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import com.google.firebase.auth.FirebaseAuth
import life.sochpekharoch.serenity.auth.AdminManager

@Composable
fun PostDetailScreen(
    navController: NavController,
    postId: String
) {
    val viewModel: CommunityViewModel = viewModel()
    val post by remember { viewModel.getPostRealtime(postId) }.collectAsState(null)
    val comments by viewModel.comments.collectAsState()
    val auth = FirebaseAuth.getInstance()
    val adminManager = remember { AdminManager() }
    var isUserAdmin by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        if (currentRoute == "community") {
            navController.popBackStack()
        }
    }

    LaunchedEffect(postId) {
        viewModel.loadComments(postId)
    }

    LaunchedEffect(auth.currentUser?.uid) {
        isUserAdmin = auth.currentUser?.uid?.let { userId ->
            adminManager.isUserAdmin(userId)
        } ?: false
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp)
            .padding(top = 24.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Show the post
        item {
            post?.let { currentPost ->
                PostCard(
                    post = currentPost,
                    onLikeClick = { viewModel.likePost(currentPost.id) },
                    onDeleteClick = { 
                        viewModel.deletePost(currentPost.id)
                        navController.navigateUp()
                    },
                    onEditClick = { /* Handle edit */ },
                    onCommentClick = { /* Already in detail view */ },
                    onRepostClick = { comment -> 
                        viewModel.repostPost(currentPost, comment)
                    },
                    currentUserId = auth.currentUser?.uid,
                    onVoteClick = { postId, optionIndex -> 
                        viewModel.voteOnPoll(postId, optionIndex)
                    },
                    isAdmin = isUserAdmin,
                    isDetailView = true,
                    isReposted = currentPost.repostedBy.contains(auth.currentUser?.uid)
                )
            }
        }

        // Add Discussion header
        item {
            Text(
                text = "Discussion",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily(Font(R.font.josefin_sans))
                ),
                modifier = Modifier
                    .offset(y = (-8).dp)
                    .padding(start = 16.dp)
            )
        }

        // Show comments
        items(comments) { comment ->
            CommentItem(comment = comment)
        }
    }
}

@Composable
private fun CommentItem(comment: Comment) {
    val abeeZee = FontFamily(Font(R.font.abeezee_regular))
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Divider(
            color = Color.LightGray.copy(alpha = 0.3f),
            thickness = 0.2.dp
        )
        
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)
        ) {
            // User ID and Avatar row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                // Avatar
                Image(
                    painter = painterResource(
                        id = when (comment.userAvatarId) {
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
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // User ID
                Text(
                    text = comment.userId.take(6),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = abeeZee
                    )
                )
            }
            
            // Comment content
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = abeeZee
                ),
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
        
        Divider(
            color = Color.LightGray.copy(alpha = 0.3f),
            thickness = 0.2.dp
        )
    }
} 