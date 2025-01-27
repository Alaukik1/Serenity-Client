package life.sochpekharoch.serenity.components

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import life.sochpekharoch.serenity.R
import life.sochpekharoch.serenity.models.Post
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.material.ripple.rememberRipple

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostCard(
    post: Post,
    onLikeClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    onCommentClick: () -> Unit,
    currentUserId: String?,
    onVoteClick: (String, Int) -> Unit,
    onPostClick: (() -> Unit)? = null
) {
    val abeeZee = FontFamily(Font(R.font.abeezee_regular))

    Log.d("POST_DEBUG", """
        Post loaded:
        - Content: ${post.content}
        - UserAvatarId: ${post.userAvatarId}
        - UserId: ${post.userId}
    """.trimIndent())

    var showMenu by remember { mutableStateOf(false) }
    var menuOffset by remember { mutableStateOf(Offset.Zero) }

    Log.e("AvatarDebug", "Post details - id: ${post.id}, userAvatarId: ${post.userAvatarId}, userId: ${post.userId}")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(
                    bounded = true,
                    color = Color.Gray.copy(alpha = 0.2f)
                ),
                onClick = { onPostClick?.invoke() },
                onLongClick = { 
                    if (post.userId == currentUserId) {
                        showMenu = true
                    }
                }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // Header with Avatar and Username
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar and Username in a Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Avatar
                    val resourceId = when (post.userAvatarId) {
                        1 -> {
                            Log.d("POSTCARD_DEBUG", "Loading avatar_1")
                            R.drawable.avatar_1
                        }
                        2 -> {
                            Log.d("POSTCARD_DEBUG", "Loading avatar_2")
                            R.drawable.avatar_2
                        }
                        3 -> {
                            Log.d("POSTCARD_DEBUG", "Loading avatar_3")
                            R.drawable.avatar_3
                        }
                        4 -> {
                            Log.d("POSTCARD_DEBUG", "Loading avatar_4")
                            R.drawable.avatar_4
                        }
                        5 -> {
                            Log.d("POSTCARD_DEBUG", "Loading avatar_5")
                            R.drawable.avatar_5
                        }
                        6 -> {
                            Log.d("POSTCARD_DEBUG", "Loading avatar_6")
                            R.drawable.avatar_6
                        }
                        7 -> {
                            Log.d("POSTCARD_DEBUG", "Loading avatar_7")
                            R.drawable.avatar_7
                        }
                        8 -> {
                            Log.d("POSTCARD_DEBUG", "Loading avatar_8")
                            R.drawable.avatar_8
                        }
                        9 -> {
                            Log.d("POSTCARD_DEBUG", "Loading avatar_9")
                            R.drawable.avatar_9
                        }
                        10 -> {
                            Log.d("POSTCARD_DEBUG", "Loading avatar_10")
                            R.drawable.avatar_10
                        }
                        11 -> {
                            Log.d("POSTCARD_DEBUG", "Loading avatar_11")
                            R.drawable.avatar_11
                        }
                        else -> {
                            Log.d("POSTCARD_DEBUG", "Loading default avatar_12")
                            R.drawable.avatar_12
                        }
                    }

                    Log.d("POSTCARD_DEBUG", "Final resourceId: $resourceId")

                    Image(
                        painter = painterResource(id = resourceId),
                        contentDescription = "User Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Username
                    Text(
                        text = post.userId.take(6),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Post Content
            Text(
                text = post.content,
                modifier = Modifier.padding(vertical = 12.dp),
                style = MaterialTheme.typography.bodyMedium
            )

            // Add Poll Options if post is a poll
            if (post.type == "POLL") {
                Spacer(modifier = Modifier.height(8.dp))
                post.options?.forEachIndexed { index, option ->
                    val text = option["text"] as? String ?: ""
                    val votes = option["votes"] as? Long ?: 0
                    val voters = option["voters"] as? List<String> ?: listOf()
                    val isVoted = currentUserId != null && voters.contains(currentUserId)

                    Button(
                        onClick = { onVoteClick(post.id, index) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isVoted) Color(0xFFFFE4E4) else Color.White,
                            contentColor = Color.Black
                        ),
                        border = BorderStroke(1.dp, Color.LightGray),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = text,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$votes votes",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Like Button, Comment Button and Counts
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like Button
                IconButton(
                    onClick = onLikeClick,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = if (currentUserId != null && post.likedBy.contains(currentUserId))
                            Icons.Default.Favorite
                        else
                            Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (currentUserId != null && post.likedBy.contains(currentUserId))
                            Color.Red
                        else
                            Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${post.likes}",
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Comment Button
                IconButton(
                    onClick = onCommentClick,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_notes),
                        contentDescription = "Comment",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${post.commentsCount}",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            // Timestamp
            Text(
                text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                    .format(Date(post.timestamp)),
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }

    if (showMenu) {
        Dialog(onDismissRequest = { showMenu = false }) {
            Surface(
                modifier = Modifier
                    .width(280.dp)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(28.dp),
                color = Color(0xFFFFF1F1)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    DialogOption(
                        text = "Edit post",
                        onClick = {
                            onEditClick()
                            showMenu = false
                        },
                        fontFamily = abeeZee
                    )
                    DialogOption(
                        text = "Share post",
                        onClick = {
                            // TODO: Implement share functionality
                            showMenu = false
                        },
                        fontFamily = abeeZee
                    )
                    DialogOption(
                        text = "Delete post",
                        onClick = {
                            onDeleteClick()
                            showMenu = false
                        },
                        textColor = Color.Red,
                        fontFamily = abeeZee
                    )
                }
            }
        }
    }
}

@Composable
private fun DialogOption(
    text: String,
    onClick: () -> Unit,
    textColor: Color = Color.Black,
    fontFamily: FontFamily
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = fontFamily
            ),
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
} 