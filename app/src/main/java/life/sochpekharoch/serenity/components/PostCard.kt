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
import life.sochpekharoch.serenity.models.PostType
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.ui.graphics.asImageBitmap
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalContext
import coil.request.ImageRequest
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.outlined.Repeat

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostCard(
    post: Post,
    onLikeClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    onCommentClick: () -> Unit,
    onRepostClick: (String) -> Unit,
    currentUserId: String?,
    onVoteClick: (String, Int) -> Unit,
    onPostClick: (() -> Unit)? = null,
    isAdmin: Boolean,
    isDetailView: Boolean = false,
    isReposted: Boolean = false
) {
    val abeeZee = FontFamily(Font(R.font.abeezee_regular))
    val context = LocalContext.current

    Log.d("POST_DEBUG", """
        Post loaded:
        - Content: ${post.content}
        - UserAvatarId: ${post.userAvatarId}
        - UserId: ${post.userId}
    """.trimIndent())

    var showMenu by remember { mutableStateOf(false) }
    var menuOffset by remember { mutableStateOf(Offset.Zero) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRepostDialog by remember { mutableStateOf(false) }

    Log.e("AvatarDebug", "Post details - id: ${post.id}, userAvatarId: ${post.userAvatarId}, userId: ${post.userId}")

    // Move Base64 decoding logic here, outside the UI tree
    val bitmap = remember(post.imageUrl) {
        if (post.type == PostType.IMAGE && post.imageUrl != null) {
            try {
                Log.d("ImageDebug", "Attempting to load image from Base64 string")
                val imageBytes = Base64.decode(post.imageUrl, Base64.DEFAULT)
                val decodedBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                if (decodedBitmap != null) {
                    Log.d("ImageDebug", "Successfully decoded Base64 to bitmap")
                } else {
                    Log.e("ImageDebug", "Failed to decode bitmap from Base64")
                }
                decodedBitmap
            } catch (e: Exception) {
                Log.e("ImageDebug", "Error processing Base64 image", e)
                null
            }
        } else null
    }

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
            // Show repost information if it's a repost
            if (post.isRepost) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.reshare_icon),
                        contentDescription = "Repost icon",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFF4CAF50),  // Same green as username
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Reposted by ${post.repostUserId?.take(6) ?: "Anonymous"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White  // White text for contrast
                        )
                    }
                }
            }

            // Header with Avatar and Username
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Avatar
                    Image(
                        painter = painterResource(
                            id = when {
                                post.userId.startsWith("bot_") -> R.drawable.bot_avatar
                                post.userAvatarId == 1 -> R.drawable.avatar_1
                                // ... other avatar cases ...
                                else -> R.drawable.avatar_1
                            }
                        ),
                        contentDescription = "User Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Username box
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (post.userId.startsWith("bot_")) {
                                    Color(0xFFAE57C0)  // Purple for bots
                                } else {
                                    Color(0xFF4CAF50)  // Green for users
                                },
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (post.userId.startsWith("bot_")) {
                                when {
                                    post.username.isNotEmpty() -> post.username
                                    post.userId == "bot_support_bot" -> "Serenity Support"
                                    post.userId == "bot_mindful_bot" -> "Serenebot Mindful"
                                    post.userId == "bot_motivation_bot" -> "Serenebot Motivation"
                                    post.userId == "bot_wellness_bot" -> "Serenebot Wellness"
                                    else -> post.userId.removePrefix("bot_")
                                }
                            } else {
                                post.userId.take(6)
                            },
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        )
                    }
                }
            }

            // Split content and source
            val (mainContent, source) = remember(post.content) {
                if (post.content.contains("\n\n> ")) {
                    val parts = post.content.split("\n\n> ", limit = 2)
                    Pair(parts[0], parts[1])
                } else {
                    Pair(post.content, "")
                }
            }

            // Display main content
            Text(
                text = mainContent,
                modifier = Modifier.padding(vertical = 12.dp),
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 24.sp,
                color = Color.Black
            )

            // Display source with background if it exists
            if (source.isNotEmpty()) {
                Column {
                    Text(
                        text = source,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFFF5F5F5),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(8.dp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 9.sp
                        ),
                        color = Color.Gray
                    )
                    
                    // Add Visit Source button in detail view
                    if (isDetailView) {
                        TextButton(
                            onClick = {
                                // Extract URL from source text and open it
                                val url = when {
                                    source.contains("Mayo Clinic") -> "https://www.mayoclinic.org/healthy-lifestyle/stress-management/in-depth/anxiety/art-20046407"
                                    source.contains("WHO") -> "https://www.who.int/news-room/fact-sheets/detail/mental-health-strengthening-our-response"
                                    source.contains("NHS") -> "https://www.nhs.uk/mental-health/"
                                    source.contains("CDC") -> "https://www.cdc.gov/mentalhealth/index.htm"
                                    source.contains("NIMH") -> "https://www.nimh.nih.gov/health"
                                    source.contains("Beyond Blue") -> "https://www.beyondblue.org.au/"
                                    source.contains("CAMH") -> "https://www.camh.ca/"
                                    source.contains("Psychology Today") -> "https://www.psychologytoday.com/"
                                    else -> null
                                }
                                // Open URL in browser
                                url?.let { uri ->
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                                    context.startActivity(intent)
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(top = 0.dp),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color.Black
                            )
                        ) {
                            Text(
                                "Visit Source",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }

            // Use the bitmap we already decoded with remember
            if (bitmap != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Post image",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }

            // Add Poll Options if post is a poll
            if (post.type == PostType.POLL && post.pollOptions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                // Calculate total votes for percentages
                val totalVotes = post.pollOptions.sumOf { it.votes.size }

                post.pollOptions.forEachIndexed { index, option ->
                    val votes = option.votes.size
                    val isVoted = currentUserId != null && option.votes.contains(currentUserId)

                    // Calculate percentage
                    val percentage = if (totalVotes > 0) {
                        ((votes.toFloat() / totalVotes.toFloat()) * 100).toInt()
                    } else 0

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF5F5F5))
                                .clickable(enabled = !isVoted) { onVoteClick(post.id, index) }
                        ) {
                            // Background progress bar
                            if (isVoted || currentUserId == null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(percentage / 100f)
                                        .fillMaxHeight()
                                        .background(
                                            color = if (isVoted) {
                                                Color(0xFFE8F0FE)
                                            } else {
                                                Color(0xFFF5F5F5)
                                            }
                                        )
                                )
                            }

                            // Content
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (isVoted) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = Color(0xFF1A73E8),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    
                                    Text(
                                        text = option.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isVoted) Color(0xFF1A73E8) else Color.Black
                                    )
                                }

                                if (isVoted || currentUserId == null) {
                                    Text(
                                        text = "$percentage%",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isVoted) Color(0xFF1A73E8) else Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }

                // Show total votes with updated style
                Text(
                    text = "$totalVotes votes",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 12.dp, start = 4.dp)
                )
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
                        imageVector = if (currentUserId != null && post.likes.contains(currentUserId))
                            Icons.Default.Favorite
                        else
                            Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (currentUserId != null && post.likes.contains(currentUserId))
                            Color.Red
                        else
                            Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${post.likes.size}",
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
                    text = "${post.comments.size}",
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Repost Button
                IconButton(
                    onClick = { showRepostDialog = true },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.reshare_icon),
                        contentDescription = "Repost",
                        tint = if (isReposted) Color(0xFF4CAF50) else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${post.repostedBy.size}",
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
        DropdownMenu(
            expanded = true,
            onDismissRequest = { showMenu = false }
        ) {
            if (post.userId.startsWith("bot_") && isAdmin) {
                DropdownMenuItem(
                    text = { Text("Delete Bot Post") },
                    onClick = {
                        showMenu = false
                        showDeleteDialog = true
                    }
                )
            }
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

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Bot Post") },
            text = { Text("Are you sure you want to delete this bot post?") },
            confirmButton = {
                Button(
                    onClick = {
                        // Delete the post from Firestore
                        FirebaseFirestore.getInstance()
                            .collection("posts")
                            .document(post.id)
                            .delete()
                            .addOnSuccessListener {
                                Log.d("PostCard", "Bot post successfully deleted")
                            }
                            .addOnFailureListener { e ->
                                Log.e("PostCard", "Error deleting bot post", e)
                            }
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showRepostDialog) {
        RepostDialog(
            onDismiss = { showRepostDialog = false },
            onConfirm = { comment ->
                onRepostClick(comment)
                showRepostDialog = false
            }
        )
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

@Composable
private fun Base64Image(base64String: String, modifier: Modifier = Modifier) {
    val imageData = remember(base64String) {
        try {
            Base64.decode(base64String, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e("ImageDebug", "Error decoding Base64 string: ${e.message}")
            null
        }
    }

    if (imageData != null) {
        val bitmap = remember(imageData) {
            try {
                BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
            } catch (e: Exception) {
                Log.e("ImageDebug", "Error creating bitmap: ${e.message}")
                null
            }
        }

        if (bitmap != null) {
            val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Post image",
                modifier = modifier
                    .fillMaxWidth()
                    .aspectRatio(aspectRatio),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun RepostDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var commentText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Repost",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Add TextField for comment
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = { Text("Add your thoughts... (optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    shape = RoundedCornerShape(16.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = { onConfirm(commentText) },
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Repost")
                    }
                }
            }
        }
    }
} 