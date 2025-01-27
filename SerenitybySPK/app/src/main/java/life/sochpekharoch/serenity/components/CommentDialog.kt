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
    return sdf.format(Date(timestamp))e: file:///C:/Users/Vastav/Desktop/SerenitybySPK/SerenitybySPK/app/src/main/java/life/sochpekharoch/serenity/screens/CommunityScreen.kt:524:20 Type mismatch: inferred type is life.sochpekharoch.serenity.models.Post but life.sochpekharoch.serenity.data.Post was expected

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

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFFFFF1F1)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Comments",
                    style = TextStyle(
                        fontFamily = josefinSans,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Comments list
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(comments) { comment ->
                        CommentItem(comment = comment)
                        Divider(color = Color.LightGray, thickness = 0.5.dp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Comment input
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                                style = TextStyle(
                                    fontSize = 14.sp
                                )
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
            }
        }
    }
}

@Composable
private fun CommentItem(comment: Comment) {
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
        
        Text(
            text = comment.content,
            style = TextStyle(fontSize = 14.sp)
        )
    }
} 