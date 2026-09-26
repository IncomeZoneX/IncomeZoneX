package com.example.ui.screens.quiz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.ui.components.TakaTopBar
import com.example.ui.localization.tr
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldAccent
import kotlinx.coroutines.delay
import kotlin.random.Random

data class QuizQuestion(
    val prompt: String,
    val options: List<Int>,
    val correctIndex: Int
)

@Composable
fun QuizChallengeScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val lang by viewModel.currentLanguage.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val user by viewModel.currentUser.collectAsState()

    var questionIndex by remember { mutableIntStateOf(1) }
    var score by remember { mutableIntStateOf(0) }
    var currentQuestion by remember { mutableStateOf(generateQuestion()) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var isAnswered by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableIntStateOf(15) }
    var quizFinished by remember { mutableStateOf(false) }

    // Countdown Timer
    LaunchedEffect(questionIndex, isAnswered, quizFinished) {
        if (!isAnswered && !quizFinished) {
            timeLeft = 15
            while (timeLeft > 0 && !isAnswered) {
                delay(1000)
                timeLeft -= 1
            }
            if (timeLeft == 0 && !isAnswered) {
                isAnswered = true
                selectedOption = -1 // timeout
            }
        }
    }

    Scaffold(
        topBar = {
            TakaTopBar(
                title = "quiz_title".tr(lang),
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
            if (!quizFinished) {
                // Top Progress & Timer
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${"question".tr(lang)} $questionIndex / 5",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = if (timeLeft <= 5) ErrorRed else GoldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$timeLeft s",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (timeLeft <= 5) ErrorRed else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { questionIndex / 5f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = EmeraldPrimary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Question Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quiz_question_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Quiz,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = currentQuestion.prompt,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Options List
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    currentQuestion.options.forEachIndexed { index, optionVal ->
                        val isCorrect = index == currentQuestion.correctIndex
                        val isSelected = selectedOption == index

                        val backgroundColor = when {
                            !isAnswered -> MaterialTheme.colorScheme.surface
                            isCorrect -> EmeraldPrimary.copy(alpha = 0.25f)
                            isSelected -> ErrorRed.copy(alpha = 0.25f)
                            else -> MaterialTheme.colorScheme.surface
                        }

                        val borderColor = when {
                            !isAnswered -> MaterialTheme.colorScheme.outline
                            isCorrect -> EmeraldPrimary
                            isSelected -> ErrorRed
                            else -> MaterialTheme.colorScheme.outline
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(backgroundColor)
                                .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
                                .clickable(enabled = !isAnswered) {
                                    selectedOption = index
                                    isAnswered = true
                                    if (isCorrect) score += 1
                                }
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                                .testTag("quiz_option_$index"),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$optionVal",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (isAnswered && isCorrect) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Correct",
                                        tint = EmeraldPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Next Button
                if (isAnswered) {
                    Button(
                        onClick = {
                            if (questionIndex < 5) {
                                questionIndex += 1
                                currentQuestion = generateQuestion()
                                isAnswered = false
                                selectedOption = null
                            } else {
                                quizFinished = true
                                viewModel.onQuizCompleted(score, 25)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("quiz_next_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black)
                    ) {
                        Text(
                            text = if (questionIndex < 5) "Next Question" else "Finish & Claim Reward",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

            } else {
                // Quiz Result Summary
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(64.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Quiz Challenge Completed!",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Score: $score / 5 correct answers",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "+${score * 25} Coins added to your wallet!",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = GoldAccent
                    )
                }

                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("quiz_return_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text(
                        text = "close".tr(lang),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

fun generateQuestion(): QuizQuestion {
    val operations = listOf("+", "-", "*")
    val op = operations.random()
    val a: Int
    val b: Int
    val correct: Int

    when (op) {
        "+" -> {
            a = Random.nextInt(10, 80)
            b = Random.nextInt(10, 80)
            correct = a + b
        }
        "-" -> {
            val n1 = Random.nextInt(20, 99)
            val n2 = Random.nextInt(10, n1)
            a = n1
            b = n2
            correct = a - b
        }
        else -> {
            a = Random.nextInt(3, 12)
            b = Random.nextInt(3, 12)
            correct = a * b
        }
    }

    val wrongOptions = mutableSetOf<Int>()
    while (wrongOptions.size < 3) {
        val delta = listOf(-10, -5, -2, -1, 1, 2, 5, 10, 15).random()
        val candidate = correct + delta
        if (candidate != correct && candidate > 0) {
            wrongOptions.add(candidate)
        }
    }

    val allOptions = (wrongOptions.toList() + correct).shuffled()
    val correctIndex = allOptions.indexOf(correct)

    return QuizQuestion(
        prompt = "$a $op $b = ?",
        options = allOptions,
        correctIndex = correctIndex
    )
}
