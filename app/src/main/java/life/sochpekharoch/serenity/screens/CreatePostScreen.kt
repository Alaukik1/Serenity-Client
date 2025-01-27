package life.sochpekharoch.serenity.screens

import android.net.Uri
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import life.sochpekharoch.serenity.components.ImagePickerButton
import life.sochpekharoch.serenity.models.PostType
import life.sochpekharoch.serenity.ui.ImagePickerActivity
import life.sochpekharoch.serenity.viewmodels.CommunityViewModel

class CreatePostActivity : ImagePickerActivity() {
    private val viewModel: CommunityViewModel by viewModels()
    private var selectedImageUri by mutableStateOf<Uri?>(null)

    @Composable
    fun CreatePostScreen() {
        var content by remember { mutableStateOf("") }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            TextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("What's on your mind?") }
            )

            ImagePickerButton(
                onClick = { launchImagePicker() }
            )

            // Show selected image preview if available
            selectedImageUri?.let { uri ->
                // Add image preview here
            }

            Button(
                onClick = {
                    viewModel.createPost(
                        content = content,
                        type = if (selectedImageUri != null) PostType.IMAGE else PostType.TEXT,
                        mediaUri = selectedImageUri
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Post")
            }
        }
    }

    override fun onImageCropped(uri: Uri) {
        selectedImageUri = uri
    }
} 