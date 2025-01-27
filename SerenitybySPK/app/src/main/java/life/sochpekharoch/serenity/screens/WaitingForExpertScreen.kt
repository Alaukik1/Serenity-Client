package life.sochpekharoch.serenity.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import life.sochpekharoch.serenity.R

@Composable
fun WaitingForExpertScreen(
    onCancelWaiting: () -> Unit
) {
    val robotoLight = FontFamily(Font(R.font.roboto_light))
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Loading indicator
        CircularProgressIndicator(
            modifier = Modifier.size(100.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 8.dp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Looking for an available expert...",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            fontFamily = robotoLight
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        OutlinedButton(
            onClick = onCancelWaiting,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            Text(
                text = "Cancel",
                fontFamily = robotoLight,
                fontSize = 16.sp
            )
        }
    }
} 