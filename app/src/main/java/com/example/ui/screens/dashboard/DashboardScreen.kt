package com.example.ui.screens.dashboard

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.R
import com.example.data.local.LanguageCode
import com.example.data.local.entity.TaskGroupEntity
import com.example.ui.components.BalanceHeroCard
import com.example.ui.components.NoticeBanner
import com.example.ui.components.TakaTopBar
import com.example.ui.localization.tr
import com.example.ui.screens.community.launchFacebook
import com.example.ui.screens.community.launchTelegram
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.FacebookBlue
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.TelegramBlue

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onOpenDrawer: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToSpin: () -> Unit,
    onNavigateToScratch: () -> Unit,
    onNavigateToQuiz: () -> Unit,
    onNavigateToWallet: () -> Unit,
    onNavigateToReferral: () -> Unit,
    onNavigateToCommunity: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToGroup: (TaskGroupEntity) -> Unit,
    onNavigateToUpgrade: () -> Unit
) {
    val lang by viewModel.currentLanguage.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val configs by viewModel.appConfigs.collectAsState()
    val enabledGroups by viewModel.enabledTaskGroups.collectAsState()
    val unreadNotifications by viewModel.unreadCount.collectAsState()
    val context = LocalContext.current

    val pointsPerBdt = configs["points_per_bdt"]?.toDoubleOrNull() ?: 100.0
    val noticeText = if (lang == LanguageCode.BN) {
        configs["notice_text_bn"] ?: ""
    } else {
        configs["notice_text_en"] ?: ""
    }

    val tgChannelUrl = configs["telegram_channel_url"] ?: "https://t.me/takareward_channel"
    val fbPageUrl = configs["facebook_page_url"] ?: "https://facebook.com/takareward.official"

    // Feature switches
    val spinEnabled = viewModel.isFeatureEnabled("feature_spin_enabled", true)
    val scratchEnabled = viewModel.isFeatureEnabled("feature_scratch_enabled", true)
    val quizEnabled = viewModel.isFeatureEnabled("feature_quiz_enabled", true)
    val sellTasksEnabled = viewModel.isFeatureEnabled("feature_sell_tasks_enabled", true)
    val communityEnabled = viewModel.isFeatureEnabled("feature_community_links_enabled", true)
    val announcementsEnabled = viewModel.isFeatureEnabled("feature_announcements_enabled", true)

    Scaffold(
        topBar = {
            TakaTopBar(
                title = "app_title".tr(lang),
                coins = user?.coins ?: 0,
                lang = lang,
                themeMode = themeMode,
                unreadNotifications = if (announcementsEnabled) unreadNotifications else 0,
                onToggleLang = { viewModel.toggleLanguage() },
                onToggleTheme = { viewModel.toggleTheme() },
                onNotificationClick = if (announcementsEnabled) onNavigateToNotifications else null,
                onMenuClick = onOpenDrawer
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            // Notice Announcement Banner
            if (announcementsEnabled && noticeText.isNotBlank()) {
                NoticeBanner(text = noticeText, lang = lang)
            }

            // Balance Hero Card with Membership Tier & Upgrade action
            BalanceHeroCard(
                coins = user?.coins ?: 0,
                pointsPerBdt = pointsPerBdt,
                membershipTier = user?.membershipTier ?: "FREE",
                lang = lang,
                onWithdrawClick = onNavigateToWallet,
                onReferClick = onNavigateToReferral,
                onUpgradeClick = onNavigateToUpgrade
            )

            // Visual Hero Banner Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("hero_illustration_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(125.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.reward_hero_banner),
                        contentDescription = "IncomeZoneX Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color(0xCC0F172A))
                                )
                            )
                            .padding(14.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Column {
                            Text(
                                text = "IncomeZoneX Multi-Tier Earning Platform",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "Complete micro-tasks, submit verified data, and withdraw instantly!",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFD1FAE5)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dynamic Task Groups / Categories
            if (sellTasksEnabled && enabledGroups.isNotEmpty()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "task_groups_title".tr(lang),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Side Panel ➔",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { onOpenDrawer() }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    enabledGroups.forEach { group ->
                        TaskGroupCard(
                            group = group,
                            lang = lang,
                            onClick = { onNavigateToGroup(group) }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Quick Activities Grid
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "quick_earn".tr(lang),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onNavigateToTasks() }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (spinEnabled) {
                        QuickEarnGridItem(
                            title = "spin_wheel".tr(lang),
                            sub = "Up to 500",
                            icon = Icons.Default.Casino,
                            accentColor = GoldAccent,
                            modifier = Modifier.weight(1f),
                            testTag = "home_quick_spin",
                            onClick = onNavigateToSpin
                        )
                    }

                    if (scratchEnabled) {
                        QuickEarnGridItem(
                            title = "scratch_card".tr(lang),
                            sub = "Up to 120",
                            icon = Icons.Default.CardGiftcard,
                            accentColor = EmeraldPrimary,
                            modifier = Modifier.weight(1f),
                            testTag = "home_quick_scratch",
                            onClick = onNavigateToScratch
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (quizEnabled) {
                        QuickEarnGridItem(
                            title = "quiz_title".tr(lang),
                            sub = "+৳5",
                            icon = Icons.Default.Quiz,
                            accentColor = Color(0xFF3B82F6),
                            modifier = Modifier.weight(1f),
                            testTag = "home_quick_quiz",
                            onClick = onNavigateToQuiz
                        )
                    }

                    QuickEarnGridItem(
                        title = "refer_btn".tr(lang),
                        sub = "+৳50 Each",
                        icon = Icons.Default.Share,
                        accentColor = Color(0xFF8B5CF6),
                        modifier = Modifier.weight(1f),
                        testTag = "home_quick_refer",
                        onClick = onNavigateToReferral
                    )
                }
            }

            // Community Links
            if (communityEnabled) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("home_community_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = null,
                                    tint = TelegramBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "community_channels".tr(lang),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GoldAccent.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "+৳10 Bonus",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = GoldAccent
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "community_desc".tr(lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    launchTelegram(context, tgChannelUrl)
                                    viewModel.onSocialTaskComplete("Telegram Channel", 200)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("home_tg_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue)
                            ) {
                                Text(
                                    text = "join_telegram".tr(lang),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            Button(
                                onClick = {
                                    launchFacebook(context, fbPageUrl)
                                    viewModel.onSocialTaskComplete("Facebook Page", 200)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("home_fb_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
                            ) {
                                Text(
                                    text = "join_facebook".tr(lang),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun TaskGroupCard(
    group: TaskGroupEntity,
    lang: LanguageCode,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("task_group_card_${group.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (group.accessRule == "PREMIUM") GoldAccent.copy(alpha = 0.2f) else EmeraldPrimary.copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (group.accessRule == "PREMIUM") Icons.Default.Star else Icons.Default.Work,
                        contentDescription = null,
                        tint = if (group.accessRule == "PREMIUM") GoldAccent else EmeraldPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (lang == LanguageCode.BN) group.nameBn else group.nameEn,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (group.accessRule == "PREMIUM") {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(GoldAccent)
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text("VIP", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                            }
                        }
                    }

                    val desc = if (lang == LanguageCode.BN) group.descriptionBn else group.descriptionEn
                    if (desc.isNotBlank()) {
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun QuickEarnGridItem(
    title: String,
    sub: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = sub,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = accentColor
            )
        }
    }
}
