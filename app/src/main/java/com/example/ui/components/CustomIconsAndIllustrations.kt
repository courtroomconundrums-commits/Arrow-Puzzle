package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Pink 3D cash stack icon matching the banknotes shown in the video.
 */
@Composable
fun PinkCashStackIcon(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        rotate(degrees = -14f, pivot = Offset(w / 2f, h / 2f)) {
            // Bottom shadow layer of stack
            drawRoundRect(
                color = Color(0xFF9D174D),
                topLeft = Offset(w * 0.14f, h * 0.34f),
                size = Size(w * 0.72f, h * 0.44f),
                cornerRadius = CornerRadius(w * 0.10f, w * 0.10f)
            )
            // Middle layer of bills
            drawRoundRect(
                color = Color(0xFFDB2777),
                topLeft = Offset(w * 0.14f, h * 0.28f),
                size = Size(w * 0.72f, h * 0.42f),
                cornerRadius = CornerRadius(w * 0.10f, w * 0.10f)
            )
            // Top bright pink banknote
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFF472B6), Color(0xFFEC4899))
                ),
                topLeft = Offset(w * 0.14f, h * 0.22f),
                size = Size(w * 0.72f, h * 0.40f),
                cornerRadius = CornerRadius(w * 0.10f, w * 0.10f)
            )
            // Inner border on top banknote
            drawRoundRect(
                color = Color(0xFFFBCFE8),
                topLeft = Offset(w * 0.19f, h * 0.26f),
                size = Size(w * 0.62f, h * 0.32f),
                cornerRadius = CornerRadius(w * 0.06f, w * 0.06f),
                style = Stroke(width = w * 0.03f)
            )
            // White paper strap across the middle
            drawRect(
                color = Color(0xFFFFF1F2),
                topLeft = Offset(w * 0.41f, h * 0.21f),
                size = Size(w * 0.18f, h * 0.54f)
            )
            drawRect(
                color = Color(0xFFE2E8F0),
                topLeft = Offset(w * 0.41f, h * 0.61f),
                size = Size(w * 0.18f, h * 0.14f)
            )
        }
    }
}

/**
 * Cute 3D pink heart with kawaii eyes & mouth matching the top life bar and Out-of-Lives dialog.
 */
@Composable
fun CuteHeartIcon(
    isFilled: Boolean,
    isSad: Boolean = false,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val heartPath = Path().apply {
            moveTo(w * 0.5f, h * 0.88f)
            cubicTo(
                w * 0.12f, h * 0.64f,
                w * 0.04f, h * 0.30f,
                w * 0.26f, h * 0.16f
            )
            cubicTo(
                w * 0.40f, h * 0.08f,
                w * 0.50f, h * 0.22f,
                w * 0.50f, h * 0.28f
            )
            cubicTo(
                w * 0.50f, h * 0.22f,
                w * 0.60f, h * 0.08f,
                w * 0.74f, h * 0.16f
            )
            cubicTo(
                w * 0.96f, h * 0.30f,
                w * 0.88f, h * 0.64f,
                w * 0.5f, h * 0.88f
            )
            close()
        }

        if (isFilled) {
            drawPath(
                path = heartPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFF8FA3), Color(0xFFF43F5E), Color(0xFFE11D48))
                )
            )
            drawPath(
                path = heartPath,
                color = Color(0xFF881337),
                style = Stroke(width = w * 0.055f, join = StrokeJoin.Round)
            )
            // Glossy highlight on top-left lobe
            drawOval(
                color = Color.White.copy(alpha = 0.55f),
                topLeft = Offset(w * 0.20f, h * 0.22f),
                size = Size(w * 0.14f, h * 0.09f)
            )
            // Cute eyes
            val eyeColor = Color(0xFF4C0519)
            drawCircle(
                color = eyeColor,
                radius = w * 0.045f,
                center = Offset(w * 0.37f, h * 0.45f)
            )
            drawCircle(
                color = eyeColor,
                radius = w * 0.045f,
                center = Offset(w * 0.63f, h * 0.45f)
            )
            // Mouth (happy or sad)
            val mouthPath = Path().apply {
                if (isSad) {
                    moveTo(w * 0.43f, h * 0.58f)
                    quadraticTo(w * 0.50f, h * 0.51f, w * 0.57f, h * 0.58f)
                } else {
                    moveTo(w * 0.44f, h * 0.52f)
                    quadraticTo(w * 0.50f, h * 0.58f, w * 0.56f, h * 0.52f)
                }
            }
            drawPath(
                path = mouthPath,
                color = eyeColor,
                style = Stroke(width = w * 0.04f, cap = StrokeCap.Round)
            )
        } else {
            // Empty / lost life slot
            drawPath(
                path = heartPath,
                color = Color(0xFFD6C7B2)
            )
            drawPath(
                path = heartPath,
                color = Color(0xFF9A8B78),
                style = Stroke(width = w * 0.05f, join = StrokeJoin.Round)
            )
        }
    }
}

/**
 * Brown leather wallet stuffed with pink banknotes (used on the Withdrawal Screen progress bar).
 */
@Composable
fun CashWalletIcon(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Pink bills sticking out of the top of the wallet
        drawRoundRect(
            color = Color(0xFFF472B6),
            topLeft = Offset(w * 0.18f, h * 0.08f),
            size = Size(w * 0.66f, h * 0.35f),
            cornerRadius = CornerRadius(w * 0.08f, w * 0.08f)
        )
        drawRoundRect(
            color = Color(0xFFEC4899),
            topLeft = Offset(w * 0.26f, h * 0.14f),
            size = Size(w * 0.62f, h * 0.32f),
            cornerRadius = CornerRadius(w * 0.08f, w * 0.08f)
        )
        // Wallet body
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFD97706), Color(0xFF92400E))
            ),
            topLeft = Offset(w * 0.10f, h * 0.28f),
            size = Size(w * 0.80f, h * 0.62f),
            cornerRadius = CornerRadius(w * 0.16f, w * 0.16f)
        )
        drawRoundRect(
            color = Color(0xFF78350F),
            topLeft = Offset(w * 0.10f, h * 0.28f),
            size = Size(w * 0.80f, h * 0.62f),
            cornerRadius = CornerRadius(w * 0.16f, w * 0.16f),
            style = Stroke(width = w * 0.04f)
        )
        // Wallet clasp on the right
        drawRoundRect(
            color = Color(0xFFB45309),
            topLeft = Offset(w * 0.62f, h * 0.48f),
            size = Size(w * 0.28f, h * 0.22f),
            cornerRadius = CornerRadius(w * 0.08f, w * 0.08f)
        )
        drawCircle(
            color = Color(0xFFFDE047),
            radius = w * 0.055f,
            center = Offset(w * 0.72f, h * 0.59f)
        )
    }
}

/**
 * Splash Screen matching 00:05 - 00:11 in the video:
 * Blue gradient background, bold yellow/blue 3D "CASH ARROW" logo with upward green arrow,
 * and green loading bar at the bottom.
 */
@Composable
fun CashArrowSplashScreen(
    progress: Int,
    onSkip: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash_pulse")
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF3882F6),
                        Color(0xFF2563EB),
                        Color(0xFF1D4ED8)
                    )
                )
            )
            .clickable { onSkip() }
            .padding(horizontal = 28.dp, vertical = 48.dp)
            .testTag("splash_screen")
    ) {
        // Logo in upper-center matching 00:08 in video
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 110.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .shadow(12.dp, RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF1E40AF), Color(0xFF1E3A8A))
                        ),
                        RoundedCornerShape(24.dp)
                    )
                    .border(3.dp, Color.White, RoundedCornerShape(24.dp))
                    .padding(horizontal = 26.dp, vertical = 14.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "JAZ CASH",
                        fontSize = (40 * logoScale).sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFACC15),
                        letterSpacing = 2.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ARROW PUZZLE",
                            fontSize = (26 * logoScale).sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFDE047),
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "↗",
                            fontSize = (32 * logoScale).sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF4ADE80)
                        )
                    }
                }
            }
        }

        // Bottom green progress bar matching 00:05 - 00:11 in video
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .height(24.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF1E40AF))
                    .border(1.5.dp, Color(0xFF93C5FD), RoundedCornerShape(50))
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth((progress / 100f).coerceIn(0.05f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF86EFAC), Color(0xFF22C55E), Color(0xFF16A34A))
                            )
                        )
                )
                Text(
                    text = "$progress%",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
