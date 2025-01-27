package life.sochpekharoch.serenity.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import life.sochpekharoch.serenity.R

@Composable
fun ProfileAvatarSelector(
    selectedAvatar: Int,
    onAvatarSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            color = Color(0xFFFFF1F1)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Choose Avatar",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(12) { index ->
                        val avatarId = index + 1
                        val resourceId = when (avatarId) {
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
                        
                        Image(
                            painter = painterResource(id = resourceId),
                            contentDescription = "Avatar $avatarId",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(
                                    width = if (selectedAvatar == avatarId) 2.dp else 0.dp,
                                    color = if (selectedAvatar == avatarId) Color.Black else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { onAvatarSelected(avatarId) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black
                    )
                ) {
                    Text("Done")
                }
            }
        }
    }
} 