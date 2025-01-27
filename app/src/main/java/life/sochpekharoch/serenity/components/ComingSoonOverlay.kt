package life.sochpekharoch.serenity.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun ComingSoonOverlay(
    title: String,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    
    val animation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Animated Gradient Background
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val colors = listOf(
                Color(0xFFFF7171),  // Light red
                Color(0xFFFFB6B6),  // Lighter red
                Color(0xFFFFE4E4),  // Very light red
                Color(0xFFFFB6B6)   // Back to lighter red
            )
            
            // Create gradient
            val brush = Brush.linearGradient(colors)
            
            // Create animated paths
            val path = Path().apply {
                moveTo(0f, 0f)
                for (i in 0..12) {
                    val x = size.width * i / 12
                    val y = size.height / 2 + sin((i + animation) * PI.toFloat() / 180f) * 100
                    lineTo(x, y)
                }
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }

            // Draw multiple rotating gradients with different opacities
            for (i in 0..3) {
                rotate(animation + i * 90f) {
                    drawPath(
                        path = path,
                        brush = brush,
                        alpha = 0.3f
                    )
                }
            }
        }

        // Overlay with translucent black background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "👋 Hello!",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "$title is coming soon!",
                    color = Color.White,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "We're working hard to bring you amazing new features. Stay tuned!",
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Got it!")
                }
            }
        }
    }
} 