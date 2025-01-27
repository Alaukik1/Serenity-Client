package life.sochpekharoch.serenity.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import life.sochpekharoch.serenity.models.PostType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import android.util.Log
import life.sochpekharoch.serenity.models.Post

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostDialog(
    onDismiss: () -> Unit,
    onCreatePost: (String, PostType, Uri?) -> Unit
) {
    var postText by remember { mutableStateOf("") }
    var selectedPostType by remember { mutableStateOf(PostType.TEXT) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var pollOptions by remember { mutableStateOf(listOf("", "")) }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            selectedPostType = PostType.IMAGE
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFFFFF1F1)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Create Post",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White
                ) {
                    TextField(
                        value = postText,
                        onValueChange = { postText = it },
                        modifier = Modifier
                            .fillMaxSize(),
                        placeholder = { 
                            Text(
                                "What's on your mind?",
                                color = Color.Gray
                            ) 
                        },
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent
                        )
                    )
                }

                selectedImageUri?.let { uri ->
                    val context = LocalContext.current
                    val bitmap = remember(uri) {
                        context.contentResolver.openInputStream(uri)?.use {
                            BitmapFactory.decodeStream(it)
                        }?.asImageBitmap()
                    }
                    
                    bitmap?.let { imageBitmap ->
                        Surface(
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .fillMaxWidth()
                                .height(200.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Image(
                                bitmap = imageBitmap,
                                contentDescription = "Selected Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                    }
                }


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (selectedPostType == PostType.IMAGE) Color(0xFFFFE4E4) else Color.White)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Add Image",
                            tint = if (selectedPostType == PostType.IMAGE) Color.Black else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { selectedPostType = PostType.POLL },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (selectedPostType == PostType.POLL) Color(0xFFFFE4E4) else Color.White)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Poll,
                            contentDescription = "Create Poll",
                            tint = if (selectedPostType == PostType.POLL) Color.Black else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (selectedPostType == PostType.POLL) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        items(pollOptions.indices.toList()) { index ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = pollOptions[index],
                                    onValueChange = { newValue ->
                                        pollOptions = pollOptions.toMutableList().apply {
                                            this[index] = newValue
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .height(48.dp),
                                    placeholder = { 
                                        Text(
                                            "Option ${index + 1}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 13.sp
                                            )
                                        ) 
                                    },
                                    textStyle = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 13.sp
                                    ),
                                    singleLine = true,
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        containerColor = Color.White,
                                        unfocusedBorderColor = Color.LightGray,
                                        focusedBorderColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                
                                if (pollOptions.size > 2) {
                                    IconButton(
                                        onClick = {
                                            pollOptions = pollOptions.toMutableList().apply {
                                                removeAt(index)
                                            }
                                        },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFE4E4))
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            "Remove option",
                                            modifier = Modifier.size(16.dp),
                                            tint = Color.Black
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            if (pollOptions.size < 5) {
                                TextButton(
                                    onClick = {
                                        pollOptions = pollOptions + ""
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Add, "Add option")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Add Option")
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.Gray
                        )
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (postText.isNotBlank() && when(selectedPostType) {
                                    PostType.POLL -> pollOptions.filter { it.isNotBlank() }.size >= 2
                                    PostType.IMAGE -> selectedImageUri != null
                                    else -> true
                                }) {
                                val finalText = if (selectedPostType == PostType.POLL) {
                                    "$postText|${pollOptions.filter { it.isNotBlank() }.joinToString("|")}"
                                } else {
                                    postText
                                }
                                onCreatePost(finalText, selectedPostType, selectedImageUri)  // Add this line
                                onDismiss()  // Add this line
                            }
                        },
                        enabled = postText.isNotBlank() && when(selectedPostType) {
                            PostType.POLL -> pollOptions.filter { it.isNotBlank() }.size >= 2
                            PostType.IMAGE -> selectedImageUri != null
                            else -> true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White,
                            disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Post")
                    }
                }
            }
        }
    }
} 