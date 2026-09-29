package com.example.ui.screens.tasks

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.data.local.LanguageCode
import com.example.ui.components.TakaTopBar
import com.example.ui.localization.tr
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import kotlinx.coroutines.delay

@Composable
fun TasksScreen(
    viewModel: MainViewModel,
    onNavigateToSpin: () -> Unit,
    onNavigateToScratch: () -> Unit,
    onNavigateToQuiz: () -> Unit,
    onNavigateToCommunity: () -> Unit,
    onNavigateToReferral: () -> Unit,
    onNavigateToGroup: ((com.example.data.local.entity.TaskGroupEntity) -> Unit)? = null,
    onNavigateToSubmissions: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null
) {
    val lang by viewModel.currentLanguage.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val enabledGroups by viewModel.enabledTaskGroups.collectAsState()

    // Read Article Task State
    var isReadingArticle by remember { mutableStateOf(false) }
    var readingTimeLeft by remember { mutableIntStateOf(15) }
    var articleFinished by remember { mutableStateOf(false) }

    LaunchedEffect(isReadingArticle) {
        if (isReadingArticle && !articleFinished) {
            readingTimeLeft = 15
            while (readingTimeLeft > 0) {
                delay(1000)
                readingTimeLeft -= 1
            }
            articleFinished = true
            isReadingArticle = false
            viewModel.onArticleReadComplete(30)
        }
    }

    Scaffold(
        topBar = {
            TakaTopBar(
                title = "tasks_title".tr(lang),
                coins = user?.coins ?: 0,
                lang = lang,
                themeMode = themeMode,
                onToggleLang = { viewModel.toggleLanguage() },
                onToggleTheme = { viewModel.toggleTheme() },
                onBack = onBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // My Submissions button
            if (onNavigateToSubmissions != null) {
                item {
                    Button(
                        onClick = onNavigateToSubmissions,
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text("my_submissions".tr(lang), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Dynamic Groups
            if (enabledGroups.isNotEmpty() && onNavigateToGroup != null) {
                item {
                    Text(
                        text = "task_groups_title".tr(lang),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                items(enabledGroups) { group ->
                    TaskActionCard(
                        title = if (lang == LanguageCode.BN) group.nameBn else group.nameEn,
                        desc = if (lang == LanguageCode.BN) group.descriptionBn else group.descriptionEn,
                        icon = Icons.Default.Assignment,
                        accentColor = if (group.accessRule == "PREMIUM") GoldAccent else EmeraldPrimary,
                        rewardText = group.accessRule,
                        buttonText = "Explore Group",
                        testTag = "open_group_${group.id}",
                        onClick = { onNavigateToGroup(group) }
                    )
                }
            }

            // Task 1: Lucky Spin
            item {
                TaskActionCard(
                    title = "spin_wheel".tr(lang),
                    desc = "spin_desc".tr(lang),
                    icon = Icons.Default.Casino,
                    accentColor = GoldAccent,
                    rewardText = "Up to +500 Coins",
                    buttonText = "Spin Now",
                    testTag = "task_open_spin",
                    onClick = onNavigateToSpin
                )
            }

            // Task 2: Scratch Card
            item {
                TaskActionCard(
                    title = "scratch_card".tr(lang),
                    desc = "scratch_desc".tr(lang),
                    icon = Icons.Default.CardGiftcard,
                    accentColor = EmeraldPrimary,
                    rewardText = "Up to +120 Coins",
                    buttonText = "Scratch Now",
                    testTag = "task_open_scratch",
                    onClick = onNavigateToScratch
                )
            }

            // Task 3: Math Quiz
            item {
                TaskActionCard(
                    title = "math_quiz".tr(lang),
                    desc = "math_quiz_desc".tr(lang),
                    icon = Icons.Default.Quiz,
                    accentColor = Color(0xFF3B82F6),
                    rewardText = "+125 Coins / 5 Qs",
                    buttonText = "Start Quiz",
                    testTag = "task_open_quiz",
                    onClick = onNavigateToQuiz
                )
            }

            // Task 4: Read & Earn (Interactive Timer Inside)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_card_read_article"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF8B5CF6).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color(0xFF8B5CF6))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "article_task".tr(lang),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "article_task_desc".tr(lang),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GoldAccent.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "+30 Coins",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = GoldAccent
                                )
                            }
                        }

                        if (isReadingArticle) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Reading tip: Complete daily streaks and share referral codes on social media for high daily commissions!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { (15 - readingTimeLeft) / 15f },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFF8B5CF6)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$readingTimeLeft ${"seconds_left".tr(lang)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (!isReadingArticle) {
                                    isReadingArticle = true
                                    articleFinished = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("start_read_task_button"),
                            enabled = !isReadingArticle,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                        ) {
                            Text(
                                text = if (isReadingArticle) "Verifying reading..." else if (articleFinished) "Read Again (+30)" else "Start Reading (15s)",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            // Task 5: Social Channels Task
            item {
                TaskActionCard(
                    title = "social_tasks".tr(lang),
                    desc = "Join our official Telegram and Facebook channels to claim +600 total bonus coins!",
                    icon = Icons.Default.Share,
                    accentColor = Color(0xFF229ED9),
                    rewardText = "+600 Coins",
                    buttonText = "View Social Tasks",
                    testTag = "task_open_community",
                    onClick = onNavigateToCommunity
                )
            }
        }
    }
}

@Composable
fun TaskActionCard(
    title: String,
    desc: String,
    icon: ImageVector,
    accentColor: Color,
    rewardText: String,
    buttonText: String,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = accentColor)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoldAccent.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = rewardText,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = GoldAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }
    }
}
