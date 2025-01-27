package life.sochpekharoch.serenity.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import life.sochpekharoch.serenity.models.Post
import life.sochpekharoch.serenity.models.PostType

@Composable
fun PostImage(
    imageUrl: String,
    isDetailView: Boolean = false,
    modifier: Modifier = Modifier
) {
    val imageModifier = if (isDetailView) {
        modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp)
    } else {
        modifier
            .size(80.dp)  // Drastically reduced from 120.dp to 80.dp
            .clip(RoundedCornerShape(6.dp))  // Smaller corner radius for smaller image
    }

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        contentDescription = "Post image",
        contentScale = if (isDetailView) ContentScale.FillWidth else ContentScale.Crop,
        modifier = imageModifier
    )
}

@Composable
fun PostComponent(
    post: Post,
    isDetailView: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Other post content...

        // Image if present
        if (post.type == PostType.IMAGE && post.imageUrl != null) {
            PostImage(
                imageUrl = post.imageUrl,
                isDetailView = isDetailView,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Rest of post content...
    }
} 