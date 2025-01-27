package life.sochpekharoch.serenity.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import life.sochpekharoch.serenity.R
import life.sochpekharoch.serenity.viewmodels.CommunityViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.style.TextOverflow
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log

@Composable
fun NotificationScreen(
    navController: NavController,
    viewModel: CommunityViewModel = viewModel()
) {
    val josefinSans = FontFamily(Font(R.font.josefin_sans))
    val notifications by viewModel.notifications.observeAsState(emptyList())
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val error by viewModel.error.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Add logging for state changes
    LaunchedEffect(notifications) {
        Log.d("NotificationScreen", "Notifications updated: size=${notifications.size}")
        notifications.forEach { notification ->
            Log.d("NotificationScreen", "Notification: $notification")
        }
    }

    LaunchedEffect(error) {
        error?.let {
            Log.e("NotificationScreen", "Error state: $it")
        }
    }

    LaunchedEffect(isLoading) {
        Log.d("NotificationScreen", "Loading state: $isLoading")
    }

    LaunchedEffect(Unit) {
        try {
            Log.d("NotificationScreen", "Refreshing notifications")
            viewModel.refreshNotifications()
        } catch (e: Exception) {
            Log.e("NotificationScreen", "Error refreshing notifications", e)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Notifications",
            style = TextStyle(
                fontFamily = josefinSans,
                fontSize = 24.sp,
                color = Color.Black
            ),
            modifier = Modifier.padding(vertical = 16.dp)
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF4CAF50))
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error loading notifications",
                        style = TextStyle(
                            fontFamily = josefinSans,
                            fontSize = 16.sp,
                            color = Color.Red
                        )
                    )
                }
            }
            notifications.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No notifications yet",
                        style = TextStyle(
                            fontFamily = josefinSans,
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notifications) { notification ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFF1F1)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = notification.title,
                                    style = TextStyle(
                                        fontFamily = josefinSans,
                                        fontSize = 16.sp,
                                        color = Color.Black
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Text(
                                    text = notification.message,
                                    style = TextStyle(
                                        fontFamily = josefinSans,
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    ),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Text(
                                    text = try {
                                        notification.timestamp?.let { 
                                            dateFormat.format(Date(it.seconds * 1000))
                                        } ?: ""
                                    } catch (e: Exception) {
                                        Log.e("NotificationScreen", "Error formatting date", e)
                                        ""
                                    },
                                    style = TextStyle(
                                        fontFamily = josefinSans,
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
    }
}