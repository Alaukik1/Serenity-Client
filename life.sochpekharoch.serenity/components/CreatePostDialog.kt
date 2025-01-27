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
                        modifier = Modifier.fillMaxSize(),
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

                // Image Preview
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

                // Attachment Options
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Image Button
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

                    // Poll Button
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

                // Poll Options
                if (selectedPostType == PostType.POLL) {
                    // ... Poll options code remains the same
                }

                // Action Buttons
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
                                onCreatePost(finalText, selectedPostType, selectedImageUri)
                                onDismiss()
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