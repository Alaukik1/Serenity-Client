package life.sochpekharoch.serenity.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.draw.clip
import life.sochpekharoch.serenity.R
import life.sochpekharoch.serenity.models.Post
import life.sochpekharoch.serenity.models.Comment
import life.sochpekharoch.serenity.viewmodels.CommunityViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.background

// Add this utility function at the top level
private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentDialog(
    post: Post,
    onDismiss: () -> Unit,
    viewModel: CommunityViewModel
) {
    // Load comments when dialog opens
    LaunchedEffect(post.id) {
        viewModel.loadComments(post.id)
    }

    var commentText by remember { mutableStateOf("") }
    val comments by viewModel.comments.collectAsState()
    val josefinSans = FontFamily(Font(R.font.josefin_sans))
    val abeeZee = FontFamily(Font(R.font.abeezee_regular))

    val currentUserId = viewModel.getCurrentUserId()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = Color(0xFFFFF1F1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = "Comments",
                style = TextStyle(
                    fontFamily = josefinSans,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Comment input moved to top
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .height(48.dp),
                    placeholder = { 
                        Text(
                            "Add a comment...", 
                            fontFamily = abeeZee,
                            style = TextStyle(fontSize = 14.sp)
                        ) 
                    },
                    maxLines = 1,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontFamily = abeeZee,
                        fontSize = 14.sp
                    ),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = Color.White,
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = Color.Black
                    ),
                    shape = RoundedCornerShape(20.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                IconButton(
                    onClick = {
                        if (commentText.isNotBlank()) {
                            viewModel.addComment(post.id, commentText)
                            commentText = ""
                        }
                    },
                    enabled = commentText.isNotBlank(),
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFFFFC8C8))
                        .padding(4.dp)
                        .size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.send_comment),
                        contentDescription = "Send",
                        tint = if (commentText.isNotBlank()) Color.Black else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Comments list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(comments) { comment ->
                    CommentItem(
                        comment = comment,
                        onLikeClick = { commentId -> 
                            viewModel.likeComment(post.id, commentId)
                        },
                        currentUserId = currentUserId
                    )
                    Divider(color = Color.LightGray, thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
private fun CommentItem(
    comment: Comment,
    onLikeClick: (String) -> Unit,
    currentUserId: String?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = comment.userId.take(6),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = formatTimestamp(comment.timestamp),
                style = TextStyle(
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = comment.content,
                style = TextStyle(fontSize = 14.sp),
                modifier = Modifier.weight(1f)
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = { onLikeClick(comment.id) },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (currentUserId != null && comment.likedBy.contains(currentUserId))
                                    R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
                            ),
                            contentDescription = "Like",
                            tint = if (currentUserId != null && comment.likedBy.contains(currentUserId))
                                Color.Red else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height((-12).dp))
                    if (comment.likes > 0) {
                        Text(
                            text = comment.likes.toString(),
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        )
                    }
                }
            }
        }
    }
} 