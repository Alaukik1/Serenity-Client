package life.sochpekharoch.serenity.utils

import android.Manifest
import android.os.Build
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import android.content.Context
import android.content.SharedPreferences

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionHandler(
    onPermissionsGranted: () -> Unit
) {
    val context = LocalContext.current
    val prefs: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    
    // If permissions were already requested before, don't show dialog again
    if (prefs.getBoolean("permissions_requested", false)) {
        onPermissionsGranted()
        return
    }

    var showPermissionDialog by remember { mutableStateOf(true) }

    val permissions = buildList {
        // Notifications (required for Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        // Storage & Gallery
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            add(Manifest.permission.READ_EXTERNAL_STORAGE)
            add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        
        // Network state
        add(Manifest.permission.ACCESS_NETWORK_STATE)
        add(Manifest.permission.INTERNET)
    }

    val permissionsState = rememberMultiplePermissionsState(permissions = permissions) { permissionsMap ->
        // Mark permissions as requested regardless of the outcome
        prefs.edit().putBoolean("permissions_requested", true).apply()
        // Always proceed with app launch
        onPermissionsGranted()
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = {
                showPermissionDialog = false
                prefs.edit().putBoolean("permissions_requested", true).apply()
                onPermissionsGranted()
            },
            title = { Text("App Permissions") },
            text = { 
                Text(
                    "This app needs the following permissions to provide the best experience:\n\n" +
                    "• Notifications - To keep you updated\n" +
                    "• Storage/Gallery - To share images\n" +
                    "• Network - To connect to our services"
                )
            },
            confirmButton = {
                Button(onClick = {
                    showPermissionDialog = false
                    permissionsState.launchMultiplePermissionRequest()
                }) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPermissionDialog = false
                    prefs.edit().putBoolean("permissions_requested", true).apply()
                    onPermissionsGranted()
                }) {
                    Text("Skip")
                }
            }
        )
    }
} 