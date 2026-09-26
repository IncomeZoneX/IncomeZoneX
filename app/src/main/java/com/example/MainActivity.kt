package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.local.LanguageCode
import com.example.data.local.ThemeMode
import com.example.data.local.entity.ProductTaskEntity
import com.example.data.local.entity.TaskGroupEntity
import com.example.ui.localization.tr
import com.example.ui.screens.admin.AdminPanelScreen
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.community.CommunityScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.notifications.NotificationsScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.quiz.QuizChallengeScreen
import com.example.ui.screens.referral.ReferralScreen
import com.example.ui.screens.scratch.ScratchCardScreen
import com.example.ui.screens.spin.LuckySpinScreen
import com.example.ui.screens.submissions.UserSubmissionsScreen
import com.example.ui.screens.tasks.TaskGroupProductsScreen
import com.example.ui.screens.tasks.TaskProductDetailScreen
import com.example.ui.screens.tasks.TasksScreen
import com.example.ui.screens.wallet.WalletScreen
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.TakaRewardTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class Screen {
    HOME,
    TASKS,
    WALLET,
    COMMUNITY,
    PROFILE,
    NOTIFICATIONS,
    SUBMISSIONS,
    GROUP_PRODUCTS,
    PRODUCT_DETAIL,
    SPIN,
    SCRATCH,
    QUIZ,
    REFERRAL,
    ADMIN,
    AUTH
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val isDark = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> systemDark
            }

            val lang by viewModel.currentLanguage.collectAsState()
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(viewModel) {
                viewModel.messageFlow.collectLatest { msg ->
                    snackbarHostState.showSnackbar(msg.text)
                }
            }

            TakaRewardTheme(darkTheme = isDark) {
                MainAppContent(
                    viewModel = viewModel,
                    lang = lang,
                    snackbarHostState = snackbarHostState
                )
            }
        }
    }
}

@Composable
fun MainAppContent(
    viewModel: MainViewModel,
    lang: LanguageCode,
    snackbarHostState: SnackbarHostState
) {
    val navStack = remember { mutableStateListOf(Screen.HOME) }
    val currentScreen = navStack.lastOrNull() ?: Screen.HOME

    val user by viewModel.currentUser.collectAsState()
    val enabledGroups by viewModel.enabledTaskGroups.collectAsState()
    val unreadNotifications by viewModel.unreadCount.collectAsState()
    val configs by viewModel.appConfigs.collectAsState()
    val upgradeCostCoins = configs["premium_upgrade_cost_coins"]?.toLongOrNull() ?: 3000L

    var selectedGroup by remember { mutableStateOf<TaskGroupEntity?>(null) }
    var selectedProduct by remember { mutableStateOf<ProductTaskEntity?>(null) }
    var showUpgradeDialog by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    fun navigateTo(screen: Screen) {
        if (screen in listOf(Screen.HOME, Screen.TASKS, Screen.WALLET, Screen.COMMUNITY, Screen.PROFILE)) {
            navStack.clear()
            navStack.add(screen)
        } else {
            navStack.add(screen)
        }
    }

    fun popBack() {
        if (navStack.size > 1) {
            navStack.removeAt(navStack.lastIndex)
        } else if (currentScreen != Screen.HOME) {
            navStack.clear()
            navStack.add(Screen.HOME)
        }
    }

    BackHandler(enabled = navStack.size > 1 || currentScreen != Screen.HOME || drawerState.isOpen) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            popBack()
        }
    }

    val isPrimaryTab = currentScreen in listOf(
        Screen.HOME,
        Screen.TASKS,
        Screen.WALLET,
        Screen.COMMUNITY,
        Screen.PROFILE
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isPrimaryTab,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(310.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Drawer Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(GoldAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "IncomeZoneX",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val isPrem = user?.membershipTier == "PREMIUM"
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isPrem) GoldAccent else EmeraldPrimary.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isPrem) "PREMIUM" else "FREE",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = if (isPrem) Color.Black else EmeraldPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${user?.coins ?: 0} Coins",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent
                                )
                            }
                        }
                    }

                    if (user?.membershipTier != "PREMIUM") {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                scope.launch { drawerState.close() }
                                showUpgradeDialog = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("drawer_upgrade_button"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Upgrade to Premium", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))

                    // Primary Navigation Items
                    Text(
                        text = "Main Navigation",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("nav_home".tr(lang)) },
                        selected = currentScreen == Screen.HOME,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navigateTo(Screen.HOME)
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Assignment, contentDescription = null) },
                        label = { Text("nav_tasks".tr(lang)) },
                        selected = currentScreen == Screen.TASKS,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navigateTo(Screen.TASKS)
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Assignment, contentDescription = null, tint = EmeraldPrimary) },
                        label = { Text("nav_submissions".tr(lang)) },
                        selected = currentScreen == Screen.SUBMISSIONS,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navigateTo(Screen.SUBMISSIONS)
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (unreadNotifications > 0) {
                                        Badge { Text("$unreadNotifications") }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = null)
                            }
                        },
                        label = { Text("nav_notifications".tr(lang)) },
                        selected = currentScreen == Screen.NOTIFICATIONS,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navigateTo(Screen.NOTIFICATIONS)
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) },
                        label = { Text("nav_wallet".tr(lang)) },
                        selected = currentScreen == Screen.WALLET,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navigateTo(Screen.WALLET)
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Share, contentDescription = null) },
                        label = { Text("refer_title".tr(lang)) },
                        selected = currentScreen == Screen.REFERRAL,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navigateTo(Screen.REFERRAL)
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    // Side Panel Task Groups Section (Dynamic from Database)
                    if (enabledGroups.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "task_groups_title".tr(lang),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        enabledGroups.forEach { group ->
                            val isSel = currentScreen == Screen.GROUP_PRODUCTS && selectedGroup?.id == group.id
                            NavigationDrawerItem(
                                icon = {
                                    Icon(
                                        imageVector = if (group.accessRule == "PREMIUM") Icons.Default.Star else Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = if (group.accessRule == "PREMIUM") GoldAccent else EmeraldPrimary
                                    )
                                },
                                label = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (lang == LanguageCode.BN) group.nameBn else group.nameEn,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (group.accessRule == "PREMIUM") {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(GoldAccent)
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text("VIP", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                            }
                                        }
                                    }
                                },
                                selected = isSel,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    selectedGroup = group
                                    navigateTo(Screen.GROUP_PRODUCTS)
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Group, contentDescription = null) },
                        label = { Text("nav_community".tr(lang)) },
                        selected = currentScreen == Screen.COMMUNITY,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navigateTo(Screen.COMMUNITY)
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text("nav_profile".tr(lang)) },
                        selected = currentScreen == Screen.PROFILE,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navigateTo(Screen.PROFILE)
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = GoldAccent) },
                        label = { Text("nav_admin".tr(lang)) },
                        selected = currentScreen == Screen.ADMIN,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navigateTo(Screen.ADMIN)
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (isPrimaryTab) {
                    NavigationBar(
                        modifier = Modifier.navigationBarsPadding(),
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        val navItems = listOf(
                            BottomNavItem("nav_home", Screen.HOME, Icons.Filled.Home, Icons.Outlined.Home),
                            BottomNavItem("nav_tasks", Screen.TASKS, Icons.Filled.Assignment, Icons.Outlined.Assignment),
                            BottomNavItem("nav_wallet", Screen.WALLET, Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
                            BottomNavItem("nav_community", Screen.COMMUNITY, Icons.Filled.Group, Icons.Outlined.Group),
                            BottomNavItem("nav_profile", Screen.PROFILE, Icons.Filled.Person, Icons.Outlined.Person)
                        )

                        navItems.forEach { item ->
                            val selected = currentScreen == item.screen
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTo(item.screen) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.activeIcon else item.inactiveIcon,
                                        contentDescription = item.titleKey.tr(lang),
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.titleKey.tr(lang),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = EmeraldPrimary,
                                    selectedTextColor = EmeraldPrimary,
                                    indicatorColor = EmeraldPrimary.copy(alpha = 0.15f),
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag("nav_item_${item.screen.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    Screen.HOME -> DashboardScreen(
                        viewModel = viewModel,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onNavigateToTasks = { navigateTo(Screen.TASKS) },
                        onNavigateToSpin = { navigateTo(Screen.SPIN) },
                        onNavigateToScratch = { navigateTo(Screen.SCRATCH) },
                        onNavigateToQuiz = { navigateTo(Screen.QUIZ) },
                        onNavigateToWallet = { navigateTo(Screen.WALLET) },
                        onNavigateToReferral = { navigateTo(Screen.REFERRAL) },
                        onNavigateToCommunity = { navigateTo(Screen.COMMUNITY) },
                        onNavigateToNotifications = { navigateTo(Screen.NOTIFICATIONS) },
                        onNavigateToGroup = { g ->
                            selectedGroup = g
                            navigateTo(Screen.GROUP_PRODUCTS)
                        },
                        onNavigateToUpgrade = { showUpgradeDialog = true }
                    )

                    Screen.TASKS -> TasksScreen(
                        viewModel = viewModel,
                        onNavigateToSpin = { navigateTo(Screen.SPIN) },
                        onNavigateToScratch = { navigateTo(Screen.SCRATCH) },
                        onNavigateToQuiz = { navigateTo(Screen.QUIZ) },
                        onNavigateToCommunity = { navigateTo(Screen.COMMUNITY) },
                        onNavigateToReferral = { navigateTo(Screen.REFERRAL) },
                        onNavigateToGroup = { g ->
                            selectedGroup = g
                            navigateTo(Screen.GROUP_PRODUCTS)
                        },
                        onNavigateToSubmissions = { navigateTo(Screen.SUBMISSIONS) }
                    )

                    Screen.WALLET -> WalletScreen(
                        viewModel = viewModel
                    )

                    Screen.COMMUNITY -> CommunityScreen(
                        viewModel = viewModel
                    )

                    Screen.PROFILE -> ProfileScreen(
                        viewModel = viewModel,
                        onNavigateToAdmin = { navigateTo(Screen.ADMIN) },
                        onNavigateToAuth = { navigateTo(Screen.AUTH) }
                    )

                    Screen.NOTIFICATIONS -> NotificationsScreen(
                        viewModel = viewModel,
                        onBack = { popBack() }
                    )

                    Screen.SUBMISSIONS -> UserSubmissionsScreen(
                        viewModel = viewModel,
                        onBack = { popBack() }
                    )

                    Screen.GROUP_PRODUCTS -> {
                        val grp = selectedGroup
                        if (grp != null) {
                            TaskGroupProductsScreen(
                                viewModel = viewModel,
                                group = grp,
                                onSelectProduct = { prod ->
                                    selectedProduct = prod
                                    navigateTo(Screen.PRODUCT_DETAIL)
                                },
                                onNavigateToUpgrade = { showUpgradeDialog = true },
                                onBack = { popBack() }
                            )
                        } else {
                            popBack()
                        }
                    }

                    Screen.PRODUCT_DETAIL -> {
                        val prod = selectedProduct
                        val grp = selectedGroup
                        if (prod != null && grp != null) {
                            TaskProductDetailScreen(
                                viewModel = viewModel,
                                product = prod,
                                group = grp,
                                onSubmitSuccess = {
                                    popBack()
                                    navigateTo(Screen.SUBMISSIONS)
                                },
                                onBack = { popBack() }
                            )
                        } else {
                            popBack()
                        }
                    }

                    Screen.SPIN -> LuckySpinScreen(
                        viewModel = viewModel,
                        onBack = { popBack() }
                    )

                    Screen.SCRATCH -> ScratchCardScreen(
                        viewModel = viewModel,
                        onBack = { popBack() }
                    )

                    Screen.QUIZ -> QuizChallengeScreen(
                        viewModel = viewModel,
                        onBack = { popBack() }
                    )

                    Screen.REFERRAL -> ReferralScreen(
                        viewModel = viewModel,
                        onBack = { popBack() }
                    )

                    Screen.ADMIN -> AdminPanelScreen(
                        viewModel = viewModel,
                        onBack = { popBack() }
                    )

                    Screen.AUTH -> AuthScreen(
                        viewModel = viewModel,
                        onSuccessAuth = { popBack() },
                        onBack = { popBack() }
                    )
                }
            }
        }
    }

    // Upgrade to Premium Dialog
    if (showUpgradeDialog) {
        AlertDialog(
            onDismissRequest = { showUpgradeDialog = false },
            title = { Text("⭐ Upgrade to Premium Membership") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Unlock high-rate VIP tasks, exclusive sell categories, and priority fast payouts.")
                    Text("Upgrade Requirement: $upgradeCostCoins Coins (≈ ৳${upgradeCostCoins / 100})", fontWeight = FontWeight.Bold, color = GoldAccent)
                    Text("Your Balance: ${user?.coins ?: 0} Coins")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.upgradeToPremium {
                            showUpgradeDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black)
                ) {
                    Text("Confirm Upgrade")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpgradeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

data class BottomNavItem(
    val titleKey: String,
    val screen: Screen,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
)
