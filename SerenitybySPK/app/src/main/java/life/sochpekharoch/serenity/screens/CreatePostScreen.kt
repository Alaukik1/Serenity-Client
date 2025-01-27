package life.sochpekharoch.serenity.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import life.sochpekharoch.serenity.R
import life.sochpekharoch.serenity.viewmodels.CommunityViewModel
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import life.sochpekharoch.serenity.models.PostType

@Composable
fun CreatePostDialog(
    postType: PostType,
    onDismiss: () -> Unit,
    viewModel: CommunityViewModel = viewModel()
) {
    val josefinSans = FontFamily(Font(R.font.josefin_sans))
    val abeeZee = FontFamily(Font(R.font.abeezee_regular))
    
    var content by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (postType) {
                            PostType.TEXT -> "Create Text Post"
                            PostType.IMAGE -> "Create Image Post"
                            PostType.POLL -> "Create Post"
                        },
                        style = TextStyle(
                            fontFamily = josefinSans,
                            fontSize = 20.sp,
                            color = Color.Black
                        )
                    )
                    
                    Button(
                        onClick = {
                            viewModel.createPost(
                                content = content,
                                type = if (selectedImageUri != null) PostType.IMAGE else PostType.TEXT,
                                imageUri = selectedImageUri
                            )
                            onDismiss()
                        },
                        enabled = content.isNotBlank() && (postType != PostType.IMAGE || selectedImageUri != null),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White,
                            disabledContainerColor = Color.Gray,
                            disabledContentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .width(60.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Post",
                            style = TextStyle(
                                fontFamily = josefinSans,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Content
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = { 
                        Text(
                            text = "What's on your mind?",
                            style = TextStyle(
                                fontFamily = abeeZee,
                                color = Color.Gray
                            )
                        )
                    },
                    textStyle = TextStyle(
                        fontFamily = abeeZee,
                        color = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (postType == PostType.IMAGE) {
                    if (selectedImageUri == null) {
                        Button(
                            onClick = { imagePicker.launch("image/*") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Select Image",
                                style = TextStyle(
                                    fontFamily = abeeZee,
                                    color = Color.White
                                )
                            )
                        }
                    } else {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Button(
                            onClick = { selectedImageUri = null },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Remove Image",
                                style = TextStyle(
                                    fontFamily = abeeZee,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }
    }
} 