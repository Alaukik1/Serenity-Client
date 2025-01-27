package life.sochpekharoch.serenity.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import life.sochpekharoch.serenity.R
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import com.razorpay.Checkout
import org.json.JSONObject
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import life.sochpekharoch.serenity.viewmodels.WalletViewModel
import life.sochpekharoch.serenity.navigation.Screen
import life.sochpekharoch.serenity.components.ComingSoonOverlay

@Composable
fun SnapHelpScreen(
    viewModel: WalletViewModel,
    navController: NavController
) {
    var showOverlay by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Your existing or placeholder SnapHelp UI here
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Your SnapHelp content (can be empty or placeholder for now)
        }

        // Overlay
        if (showOverlay) {
            ComingSoonOverlay(
                title = "SnapHelp",
                onDismiss = {
                    showOverlay = false
                    navController.navigate(Screen.Community.route) {
                        popUpTo(Screen.Community.route) {
                            inclusive = false
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun PlanButton(
    duration: String,
    price: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) Color.Black else Color.Transparent,
        ),
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = duration,
                style = TextStyle(
                    fontFamily = FontFamily(Font(R.font.roboto_light)),
                    fontSize = 16.sp,
                    color = if (isSelected) Color.White else Color.Black
                )
            )
            Text(
                text = price,
                style = TextStyle(
                    fontFamily = FontFamily(Font(R.font.roboto_light)),
                    fontSize = 16.sp,
                    color = if (isSelected) Color.White else Color.Black
                )
            )
        }
    }
}

@Composable
private fun MenuButton(text: String, fontFamily: FontFamily, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 24.dp)
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = fontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black
            )
        )
    }
} 