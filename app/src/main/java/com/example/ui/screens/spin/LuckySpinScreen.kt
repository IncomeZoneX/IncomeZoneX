package com.example.ui.screens.spin

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.ui.components.TakaTopBar
import com.example.ui.localization.tr
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun LuckySpinScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val lang by viewModel.currentLanguage.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val spinTaskState by viewModel.spinTaskState.collectAsState()

    val scope = rememberCoroutineScope()
    val rotation = remember { Animatable(0f) }
    var isSpinning by remember { mutableStateOf(false) }
    var wonCoinsDialog by remember { mutableStateOf<Long?>(null) }

    val slices = remember {
        listOf(
            SpinSlice(20, Color(0xFFEF4444)),
            SpinSlice(50, Color(0xFF3B82F6)),
            SpinSlice(100, Color(0xFF10B981)),
            SpinSlice(30, Color(0xFFF59E0B)),
            SpinSlice(150, Color(0xFF8B5CF6)),
            SpinSlice(40, Color(0xFFEC4899)),
            SpinSlice(250, Color(0xFF06B6D4)),
            SpinSlice(500, Color(0xFFEAB308))
        )
    }

    val spinsCompleted = spinTaskState?.countCompleted ?: 0
    val spinsMax = spinTaskState?.maxAllowed ?: 10
    val spinsLeft = (spinsMax - spinsCompleted).coerceAtLeast(0)

    Scaffold(
        topBar = {
            TakaTopBar(
                title = "lucky_spin_title".tr(lang),
                coins = user?.coins ?: 0,
                lang = lang,
                themeMode = themeMode,
                onToggleLang = { viewModel.toggleLanguage() },
                onToggleTheme = { viewModel.toggleTheme() },
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Stats Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Casino,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "spins_remaining".tr(lang),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "$spinsLeft / $spinsMax",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (spinsLeft > 0) EmeraldPrimary else Color.Red
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Wheel Canvas Box
            Box(
                modifier = Modifier
                    .size(310.dp)
                    .testTag("spin_wheel_box"),
                contentAlignment = Alignment.Center
            ) {
                // Background Outer Rim Ring
                Box(
                    modifier = Modifier
                        .size(310.dp)
                        .clip(CircleShape)
                        .background(GoldAccent.copy(alpha = 0.2f))
                )

                // Wheel Canvas
                Canvas(
                    modifier = Modifier
                        .size(290.dp)
                        .rotate(rotation.value)
                ) {
                    val canvasSize = size.minDimension
                    val radius = canvasSize / 2f
                    val center = Offset(radius, radius)
                    val sliceAngle = 360f / slices.size

                    slices.forEachIndexed { i, slice ->
                        val startAngle = i * sliceAngle
                        drawArc(
                            color = slice.color,
                            startAngle = startAngle,
                            sweepAngle = sliceAngle,
                            useCenter = true,
                            size = Size(canvasSize, canvasSize),
                            topLeft = Offset.Zero
                        )

                        // Divider lines
                        val rad = Math.toRadians(startAngle.toDouble())
                        val endX = center.x + radius * cos(rad).toFloat()
                        val endY = center.y + radius * sin(rad).toFloat()
                        drawLine(
                            color = Color.White,
                            start = center,
                            end = Offset(endX, endY),
                            strokeWidth = 3f
                        )

                        // Text labels for slices
                        val midAngle = startAngle + (sliceAngle / 2f)
                        val textRadius = radius * 0.65f
                        val textRad = Math.toRadians(midAngle.toDouble())
                        val textX = center.x + textRadius * cos(textRad).toFloat()
                        val textY = center.y + textRadius * sin(textRad).toFloat()

                        drawContext.canvas.nativeCanvas.apply {
                            val paint = android.graphics.Paint().apply {
                                color = android.graphics.Color.WHITE
                                textSize = 36f
                                isFakeBoldText = true
                                textAlign = android.graphics.Paint.Align.CENTER
                                setShadowLayer(4f, 1f, 1f, android.graphics.Color.BLACK)
                            }
                            save()
                            rotate(midAngle + 90f, textX, textY)
                            drawText("${slice.coins}", textX, textY + 12f, paint)
                            restore()
                        }
                    }

                    // Outer border
                    drawCircle(
                        color = Color.White,
                        radius = radius,
                        center = center,
                        style = Stroke(width = 6f)
                    )
                }

                // Center Hub
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Top Pointer Needle pointing down at 12 o'clock
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Pointer",
                        tint = Color.White,
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.TopCenter)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Spin Action Button
            Button(
                onClick = {
                    if (isSpinning || spinsLeft <= 0) return@Button
                    isSpinning = true
                    scope.launch {
                        // Pick random slice
                        val winningIndex = Random.nextInt(slices.size)
                        val winningSlice = slices[winningIndex]
                        val sliceAngle = 360f / slices.size

                        // Pointer is at Top (270 degrees in canvas coordinates)
                        val targetSectorCenter = winningIndex * sliceAngle + (sliceAngle / 2f)
                        val extraFullRounds = Random.nextInt(5, 9) * 360f
                        val targetRotation = extraFullRounds + (270f - targetSectorCenter)

                        rotation.animateTo(
                            targetValue = targetRotation,
                            animationSpec = tween(
                                durationMillis = 4000,
                                easing = FastOutSlowInEasing
                            )
                        )
                        isSpinning = false
                        viewModel.onSpinComplete(winningSlice.coins.toLong())
                        wonCoinsDialog = winningSlice.coins.toLong()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("spin_wheel_action_button"),
                enabled = !isSpinning && spinsLeft > 0,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldAccent,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = if (isSpinning) "loading".tr(lang) else if (spinsLeft > 0) "spin_button".tr(lang) else "spin_limit_reached".tr(lang),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }

    wonCoinsDialog?.let { coinsWon ->
        AlertDialog(
            onDismissRequest = { wonCoinsDialog = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "success".tr(lang),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "${"congrats_win".tr(lang)} $coinsWon ${"coins".tr(lang)}!",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                Button(
                    onClick = { wonCoinsDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("claim".tr(lang))
                }
            }
        )
    }
}

data class SpinSlice(val coins: Int, val color: Color)
