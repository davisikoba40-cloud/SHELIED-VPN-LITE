package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ShieldCyan
import com.example.ui.theme.ShieldGreen
import com.example.ui.theme.ShieldRed
import com.example.ui.theme.ShieldSurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpeedometerGauge(
    currentSpeedMbps: Float,
    maxSpeedMbps: Float = 120f,
    modifier: Modifier = Modifier,
    label: String = "DOWNLOAD SPEED"
) {
    val animatedSpeed by animateFloatAsState(
        targetValue = currentSpeedMbps.coerceIn(0f, maxSpeedMbps),
        animationSpec = tween(durationMillis = 200),
        label = "SpeedGauge"
    )

    val fraction = (animatedSpeed / maxSpeedMbps).coerceIn(0f, 1f)
    val startAngle = 140f
    val sweepTotal = 260f
    val activeSweep = sweepTotal * fraction

    Box(
        modifier = modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(220.dp)) {
            val strokeWidth = 14.dp.toPx()
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val arcTopLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

            // Background Track
            drawArc(
                color = ShieldSurfaceVariantDark,
                startAngle = startAngle,
                sweepAngle = sweepTotal,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Neon Gradient Active Sweep
            if (activeSweep > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to ShieldCyan,
                        0.5f to ShieldGreen,
                        1.0f to ShieldCyan
                    ),
                    startAngle = startAngle,
                    sweepAngle = activeSweep,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Ticks around the arc
            val radius = (size.width - strokeWidth) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            val totalTicks = 20
            for (i in 0..totalTicks) {
                val tickAngleDeg = startAngle + (sweepTotal * (i.toFloat() / totalTicks))
                val tickAngleRad = Math.toRadians(tickAngleDeg.toDouble())
                val innerR = radius - 16.dp.toPx()
                val outerR = radius - 8.dp.toPx()
                val p1 = Offset(
                    (center.x + innerR * cos(tickAngleRad)).toFloat(),
                    (center.y + innerR * sin(tickAngleRad)).toFloat()
                )
                val p2 = Offset(
                    (center.x + outerR * cos(tickAngleRad)).toFloat(),
                    (center.y + outerR * sin(tickAngleRad)).toFloat()
                )
                val isPassed = (i.toFloat() / totalTicks) <= fraction
                drawLine(
                    color = if (isPassed) ShieldCyan else TextMuted.copy(alpha = 0.4f),
                    start = p1,
                    end = p2,
                    strokeWidth = if (i % 5 == 0) 3.dp.toPx() else 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = String.format("%.1f", animatedSpeed),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
            )
            Text(
                text = "Mbps",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = ShieldCyan
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.5.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}
