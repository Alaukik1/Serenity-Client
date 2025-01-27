package life.sochpekharoch.serenity.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import life.sochpekharoch.serenity.R

@Composable
fun AvatarSelector(
    selectedAvatar: Int,
    onAvatarSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFFFFF1F1)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Choose Avatar",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "An avatar is your visual identity in the community",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color.Gray
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    items(12) { index ->
                        val avatarId = index + 1
                        Image(
                            painter = painterResource(
                                id = when (avatarId) {
                                    1 -> R.drawable.avatar_1
                                    2 -> R.drawable.avatar_2
                                    3 -> R.drawable.avatar_3
                                    4 -> R.drawable.avatar_4
                                    5 -> R.drawable.avatar_5
                                    6 -> R.drawable.avatar_6
                                    7 -> R.drawable.avatar_7
                                    8 -> R.drawable.avatar_8
                                    9 -> R.drawable.avatar_9
                                    10 -> R.drawable.avatar_10
                                    11 -> R.drawable.avatar_11
                                    else -> R.drawable.avatar_12
                                }
                            ),
                            contentDescription = "Avatar $avatarId",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 2.dp,
                                    color = if (selectedAvatar == avatarId) Color.Black else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { onAvatarSelected(avatarId) }
                        )
                    }
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Next",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
} 