package life.sochpekharoch.serenity.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun FeaturePointer(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
    ) {
        val arrowHeadSize = 20f
        val curveControlPointX = size.width * 0.4f
        
        // Calculate positions
        val startX = size.width * 0.5f  // Start from center (dialog)
        val startY = size.height * 0.4f  // Start from dialog height
        val endX = size.width * 0.2f    // End at community button
        val endY = size.height - offsetY // Bottom with animation

        // Draw curved line
        val path = Path().apply {
            moveTo(startX, startY)
            quadraticBezierTo(
                curveControlPointX, startY + (endY - startY) * 0.5f,
                endX, endY - arrowHeadSize
            )
        }

        // Draw the curved line
        drawPath(
            path = path,
            color = Color(0xFFFF6B6B),
            style = Stroke(
                width = 5f,
                cap = StrokeCap.Round
            )
        )

        // Draw arrow head
        val arrowPath = Path().apply {
            moveTo(endX, endY)
            lineTo(endX - arrowHeadSize, endY - arrowHeadSize)
            lineTo(endX + arrowHeadSize, endY - arrowHeadSize)
            close()
        }
        drawPath(
            path = arrowPath,
            color = Color(0xFFFF6B6B),
            style = Fill
        )
    }
} 