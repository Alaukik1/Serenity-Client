package life.sochpekharoch.serenity.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import life.sochpekharoch.serenity.R
import life.sochpekharoch.serenity.Screen

@Composable
fun MyPostsScreen(navController: NavController) {
    val josefinSans = FontFamily(Font(R.font.josefin_sans))
    val robotoLight = FontFamily(Font(R.font.roboto_light))
    
    // Add state for selected post type
    var selectedType by remember { mutableStateOf("text") }  // "text", "image", "poll"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Header with stats
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Posts",
                style = TextStyle(
                    fontFamily = josefinSans,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Expand",
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        // Stats Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(
                painter = painterResource(id = R.drawable.textpost_icon),
                count = "5",
                useCustomIcon = true,
                isSelected = selectedType == "text",
                onClick = { selectedType = "text" }
            )
            StatItem(
                painter = painterResource(id = R.drawable.imagepost_icon),
                count = "3",
                useCustomIcon = true,
                isSelected = selectedType == "image",
                onClick = { selectedType = "image" }
            )
            StatItem(
                painter = painterResource(id = R.drawable.pollpost_icon),
                count = "7",
                useCustomIcon = true,
                isSelected = selectedType == "poll",
                onClick = { selectedType = "poll" }
            )
        }

        // Content based on selected type
        when (selectedType) {
            "text" -> TextPostsList(robotoLight)
            "image" -> ImagePostsList()
            "poll" -> PollPostsList(robotoLight)
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector? = null,
    painter: Painter? = null,
    count: String,
    useCustomIcon: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clickable { onClick() }
            .background(
                if (isSelected) Color(0xFFFFF1F1) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        if (useCustomIcon && painter != null) {
            Icon(
                painter = painter,
                contentDescription = null,
                tint = Color.Black
            )
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Black
            )
        }
        Text(
            text = count,
            style = TextStyle(
                fontSize = 16.sp,
                color = Color.Black
            )
        )
    }
}

@Composable
private fun ImagePostsList() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(3) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.snaphelp_image),
                    contentDescription = "Post Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun TextPostsList(fontFamily: FontFamily) {
    // Implement text posts list
}

@Composable
private fun PollPostsList(fontFamily: FontFamily) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(4) { index ->
            PollPostItem(
                username = "XYZ123",
                question = "This is the column where heading/question of poll will be displayed.",
                options = listOf(
                    PollOption("Option 1", 0.7f),
                    PollOption("Option 2", 0.4f),
                    PollOption("Option 3", 0.6f)
                ),
                fontFamily = fontFamily
            )
        }
    }
}

data class PollOption(
    val text: String,
    val percentage: Float
)

@Composable
private fun PollPostItem(
    username: String,
    question: String,
    options: List<PollOption>,
    fontFamily: FontFamily
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // User info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.snaphelp_image),
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = username,
                    style = TextStyle(
                        fontFamily = fontFamily,
                        fontSize = 14.sp
                    )
                )
            }

            // Question
            Text(
                text = question,
                style = TextStyle(
                    fontFamily = fontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Poll options
            options.forEach { option ->
                PollOptionItem(option = option)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    Icon(
                        imageVector = Icons.Default.OpenInFull,
                        contentDescription = "Expand",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        modifier = Modifier.size(24.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun PollOptionItem(option: PollOption) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFFE5BA)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth(option.percentage)
                    .fillMaxHeight()
                    .background(
                        color = Color(0xFFFFD699),
                        shape = RoundedCornerShape(16.dp)
                    )
            )
            
            // Option text and percentage
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = option.text,
                    color = Color.Black,
                    fontSize = 14.sp
                )
                Text(
                    text = "${(option.percentage * 100).toInt()}%",
                    color = Color.Black,
                    fontSize = 14.sp
                )
            }
        }
    }
} 