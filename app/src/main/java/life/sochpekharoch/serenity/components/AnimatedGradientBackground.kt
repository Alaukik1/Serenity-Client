package life.sochpekharoch.serenity.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedGradientBackground(content: @Composable () -> Unit) {
    val colors = listOf(
        Color(0xFFE468B4),  // Pink #e468b4
        Color(0xFF31A499),  // Teal #31a499
        Color(0xFFFFCEC4),  // Light Pink #ffcec4
        Color(0xFFFF9E17)   // Orange #ff9e17
    )
    
    val transition = rememberInfiniteTransition(label = "gradient")
    
    val moveX by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 15000,
                easing = EaseInOutSine
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "moveX"
    )

    val moveY by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 20000,
                easing = EaseInOutSine
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "moveY"
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Animated gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = colors,
                        center = Offset(
                            x = cos(moveX * 2 * PI.toFloat()) * 1000 + 1000,
                            y = sin(moveY * 2 * PI.toFloat()) * 1000 + 1000
                        ),
                        radius = 2000f,
                        tileMode = TileMode.Mirror
                    )
                )
        )

        // Translucent white overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.65f))  // Changed from 0.6f to 0.65f for 35% transparency
        )

        // Content
        content()
    }
} 