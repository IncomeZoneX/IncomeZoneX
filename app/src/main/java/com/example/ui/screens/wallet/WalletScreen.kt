package com.example.ui.screens.wallet

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.data.local.LanguageCode
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.WithdrawalEntity
import com.example.ui.components.TakaTopBar
import com.example.ui.localization.tr
import com.example.ui.theme.BkashPink
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NagadOrange
import com.example.ui.theme.RocketPurple
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    viewModel: MainViewModel,
    onBack: (() -> Unit)? = null
) {
    val lang by viewModel.currentLanguage.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val configs by viewModel.appConfigs.collectAsState()
    val transactions by viewModel.userTransactions.collectAsState()
    val withdrawals by viewModel.userWithdrawals.collectAsState()

    val totalIncome by viewModel.totalIncomeBdt.collectAsState()
    val socialIncome by viewModel.socialIncomeBdt.collectAsState()
    val textSellIncome by viewModel.textSellIncomeBdt.collectAsState()
    val vipIncome by viewModel.vipIncomeBdt.collectAsState()

    val context = LocalContext.current

    // Tab 0: Withdraw, Tab 1: Deposit & Activation, Tab 2: History
    var selectedTab by remember { mutableIntStateOf(0) }

    val feePercent = configs["withdraw_charge_percent"]?.toDoubleOrNull() ?: 5.0
    val minWithdrawBdt = configs["min_withdraw_bdt"]?.toLongOrNull() ?: 50L
    val activationFeeBdt = configs["account_activation_fee_bdt"]?.toDoubleOrNull() ?: 100.0
    val proPercent = configs["pro_activation_percent"]?.toDoubleOrNull() ?: 150.0
    val proRequiredFee = (activationFeeBdt * proPercent / 100.0)

    val isUserActivated = user?.isActivated ?: true
    val isPro = user?.membershipTier == "PREMIUM"

    // Withdraw Form States
    val withdrawMethods = listOf("bKash", "Nagad", "Rocket", "Upay", "USDT", "Recharge")
    var selectedWithdrawMethod by remember { mutableStateOf("bKash") }
    var withdrawAccount by remember { mutableStateOf("") }
    var withdrawAmountStr by remember { mutableStateOf("100") }

    val enteredWithdrawAmount = withdrawAmountStr.toLongOrNull() ?: 0L
    val feeAmount = (enteredWithdrawAmount * feePercent / 100.0)
    val netPayout = (enteredWithdrawAmount - feeAmount).coerceAtLeast(0.0)

    // Deposit Form States
    var selectedDepositReason by remember { mutableStateOf(if (!isUserActivated) "ACCOUNT_ACTIVATION" else "PRO_MEMBERSHIP") }
    val depositMethods = listOf("bKash", "Nagad", "Rocket", "Upay", "USDT")
    var selectedDepositMethod by remember { mutableStateOf("bKash") }
    var depositSenderPhone by remember { mutableStateOf("") }
    var depositTrxId by remember { mutableStateOf("") }
    var depositAmountStr by remember {
        mutableStateOf(
            if (!isUserActivated) String.format(Locale.US, "%.0f", activationFeeBdt)
            else String.format(Locale.US, "%.0f", proRequiredFee)
        )
    }

    val depositAdminNumber = when (selectedDepositMethod) {
        "bKash" -> configs["deposit_bkash_number"] ?: "01700000000 (Send Money)"
        "Nagad" -> configs["deposit_nagad_number"] ?: "01800000000 (Send Money)"
        "Rocket" -> configs["deposit_rocket_number"] ?: "01900000000 (Send Money)"
        "Upay" -> configs["deposit_upay_number"] ?: "01600000000 (Send Money)"
        else -> configs["deposit_usdt_address"] ?: "TYD9q3...TRC20Address"
    }

    Scaffold(
        topBar = {
            TakaTopBar(
                title = "wallet_title".tr(lang),
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
        ) {
            // Balance & Account Status Overview Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("wallet_balance_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "available_balance".tr(lang),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "৳ ${user?.coins ?: 0}",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = EmeraldPrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            // Activation Status Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isUserActivated) EmeraldPrimary.copy(alpha = 0.15f) else ErrorRed.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (isUserActivated) "account_active".tr(lang) else "account_inactive".tr(lang),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isUserActivated) EmeraldPrimary else ErrorRed
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            // Plan Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isPro) GoldAccent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (isPro) "premium_member".tr(lang) else "free_member".tr(lang),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isPro) GoldAccent else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Inactive Account Notice & Instant Activation Button
                    if (!isUserActivated) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f)),
                            modifier = Modifier.fillMaxWidth().testTag("activation_required_alert")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "account_inactive".tr(lang),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = ErrorRed
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "activation_required_msg".tr(lang),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        selectedDepositReason = "ACCOUNT_ACTIVATION"
                                        depositAmountStr = String.format(Locale.US, "%.0f", activationFeeBdt)
                                        selectedTab = 1
                                    },
                                    modifier = Modifier.fillMaxWidth().height(40.dp).testTag("activate_account_btn"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.White)
                                ) {
                                    Icon(imageVector = Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${"activate_account_now".tr(lang)} (৳${String.format(Locale.US, "%.0f", activationFeeBdt)})",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Income Summary Cards (BDT)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp)
                    .testTag("income_summary_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "income_summary".tr(lang),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${"total_income".tr(lang)}: ৳${String.format(Locale.US, "%.1f", totalIncome)}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = EmeraldPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Category 1: Social
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(
                                    text = "category_social_income".tr(lang),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "৳ ${String.format(Locale.US, "%.1f", socialIncome)}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Category 2: Text Sell
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(
                                    text = "category_text_income".tr(lang),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "৳ ${String.format(Locale.US, "%.1f", textSellIncome)}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Category 3: VIP Tasks
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(
                                    text = "category_vip_income".tr(lang),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "৳ ${String.format(Locale.US, "%.1f", vipIncome)}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = GoldAccent
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Navigation Tabs: Withdraw, Deposit, History
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CurrencyExchange, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("withdraw_money".tr(lang))
                        }
                    },
                    modifier = Modifier.testTag("tab_withdraw")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AddCard, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("deposit_money".tr(lang))
                        }
                    },
                    modifier = Modifier.testTag("tab_deposit")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("nav_submissions".tr(lang))
                        }
                    },
                    modifier = Modifier.testTag("tab_history")
                )
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // WITHDRAW TAB
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Text(
                                text = "select_method".tr(lang),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(withdrawMethods) { method ->
                                    val isSelected = selectedWithdrawMethod == method
                                    val isRecharge = method == "Recharge"
                                    val brandColor = when (method) {
                                        "bKash" -> BkashPink
                                        "Nagad" -> NagadOrange
                                        "Rocket" -> RocketPurple
                                        else -> EmeraldPrimary
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected && !isRecharge) brandColor else MaterialTheme.colorScheme.surfaceVariant)
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) brandColor else MaterialTheme.colorScheme.outline,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                if (isRecharge) {
                                                    Toast.makeText(context, "recharge_coming_soon".tr(lang), Toast.LENGTH_LONG).show()
                                                } else {
                                                    selectedWithdrawMethod = method
                                                }
                                            }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                            .testTag("withdraw_method_$method")
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = method,
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (isSelected && !isRecharge) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (isRecharge) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(GoldAccent)
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = "coming_soon".tr(lang),
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color.Black
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = withdrawAccount,
                                onValueChange = { withdrawAccount = it },
                                label = { Text("account_number".tr(lang)) },
                                placeholder = { Text(if (selectedWithdrawMethod == "USDT") "TRC20 Wallet Address" else "017XXXXXXXX") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("withdraw_account_input"),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = if (selectedWithdrawMethod == "USDT") KeyboardType.Text else KeyboardType.Phone
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = withdrawAmountStr,
                                onValueChange = { withdrawAmountStr = it },
                                label = { Text("withdraw_amount_bdt".tr(lang)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("withdraw_amount_input"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Quick Select Chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(50L, 100L, 200L, 500L).forEach { chipBdt ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .clickable { withdrawAmountStr = chipBdt.toString() }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "৳ $chipBdt",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            // Charge & Net Payout Summary Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${"withdraw_charge_fee".tr(lang)} ($feePercent%):",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "- ৳ ${String.format(Locale.US, "%.2f", feeAmount)}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = ErrorRed
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "net_received_amount".tr(lang),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "৳ ${String.format(Locale.US, "%.2f", netPayout)}",
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                            color = EmeraldPrimary
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Button(
                                onClick = {
                                    if (selectedWithdrawMethod == "Recharge") {
                                        Toast.makeText(context, "recharge_coming_soon".tr(lang), Toast.LENGTH_LONG).show()
                                        return@Button
                                    }
                                    if (withdrawAccount.isBlank() || withdrawAccount.length < 5) {
                                        viewModel.showMessage("invalid_phone".tr(lang), isError = true)
                                        return@Button
                                    }
                                    if (enteredWithdrawAmount < minWithdrawBdt) {
                                        viewModel.showMessage("${"min_withdraw".tr(lang)}: ৳$minWithdrawBdt", isError = true)
                                        return@Button
                                    }
                                    viewModel.submitWithdrawal(
                                        method = selectedWithdrawMethod,
                                        accountNumber = withdrawAccount.trim(),
                                        coins = enteredWithdrawAmount,
                                        onSuccess = {
                                            withdrawAccount = ""
                                            selectedTab = 2 // Switch to history tab
                                        }
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("submit_withdraw_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmeraldPrimary,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(imageVector = Icons.Default.Send, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "submit_withdraw".tr(lang),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // DEPOSIT & ACTIVATION TAB
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Text(
                                text = "deposit_reason".tr(lang),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Account Activation Option
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (selectedDepositReason == "ACCOUNT_ACTIVATION") EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable {
                                            selectedDepositReason = "ACCOUNT_ACTIVATION"
                                            depositAmountStr = String.format(Locale.US, "%.0f", activationFeeBdt)
                                        }
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "reason_activation".tr(lang),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (selectedDepositReason == "ACCOUNT_ACTIVATION") Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "৳ ${String.format(Locale.US, "%.0f", activationFeeBdt)}",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                            color = if (selectedDepositReason == "ACCOUNT_ACTIVATION") GoldAccent else EmeraldPrimary
                                        )
                                    }
                                }

                                // Pro Membership Option
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (selectedDepositReason == "PRO_MEMBERSHIP") GoldAccent else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable {
                                            selectedDepositReason = "PRO_MEMBERSHIP"
                                            depositAmountStr = String.format(Locale.US, "%.0f", proRequiredFee)
                                        }
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "reason_pro".tr(lang),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (selectedDepositReason == "PRO_MEMBERSHIP") Color.Black else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "৳ ${String.format(Locale.US, "%.0f", proRequiredFee)}",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                            color = if (selectedDepositReason == "PRO_MEMBERSHIP") Color.Black else GoldAccent
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = "select_method".tr(lang),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(depositMethods) { method ->
                                    val isSelected = selectedDepositMethod == method
                                    val brandColor = when (method) {
                                        "bKash" -> BkashPink
                                        "Nagad" -> NagadOrange
                                        "Rocket" -> RocketPurple
                                        else -> EmeraldPrimary
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) brandColor else MaterialTheme.colorScheme.surfaceVariant)
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) brandColor else MaterialTheme.colorScheme.outline,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable { selectedDepositMethod = method }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                            .testTag("deposit_method_$method")
                                    ) {
                                        Text(
                                            text = method,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            // Instruction & Admin Payment Number Display
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "deposit_instruction".tr(lang),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "$selectedDepositMethod: $depositAdminNumber",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(EmeraldPrimary.copy(alpha = 0.15f))
                                                .clickable {
                                                    val cleanNum = depositAdminNumber.split(" ").firstOrNull() ?: depositAdminNumber
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                    clipboard.setPrimaryClip(ClipData.newPlainText("Number", cleanNum))
                                                    Toast.makeText(context, "copied".tr(lang), Toast.LENGTH_SHORT).show()
                                                }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("copy".tr(lang), style = MaterialTheme.typography.labelSmall, color = EmeraldPrimary)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = depositSenderPhone,
                                onValueChange = { depositSenderPhone = it },
                                label = { Text("sender_number".tr(lang)) },
                                placeholder = { Text("017XXXXXXXX") },
                                modifier = Modifier.fillMaxWidth().testTag("deposit_sender_input"),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = depositTrxId,
                                onValueChange = { depositTrxId = it },
                                label = { Text("trx_id".tr(lang)) },
                                placeholder = { Text("e.g. 9J3K8L2P") },
                                modifier = Modifier.fillMaxWidth().testTag("deposit_trx_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = depositAmountStr,
                                onValueChange = { depositAmountStr = it },
                                label = { Text("deposit_amount_bdt".tr(lang)) },
                                modifier = Modifier.fillMaxWidth().testTag("deposit_amount_input"),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        item {
                            Button(
                                onClick = {
                                    val amt = depositAmountStr.toDoubleOrNull() ?: 0.0
                                    if (depositTrxId.isBlank() || depositSenderPhone.isBlank() || amt <= 0.0) {
                                        viewModel.showMessage("Please fill TrxID, sender number, and amount", isError = true)
                                        return@Button
                                    }
                                    viewModel.submitDeposit(
                                        method = selectedDepositMethod,
                                        senderNumber = depositSenderPhone.trim(),
                                        trxId = depositTrxId.trim(),
                                        amountBdt = amt,
                                        reason = selectedDepositReason,
                                        onSuccess = {
                                            depositTrxId = ""
                                            depositSenderPhone = ""
                                            selectedTab = 2 // Switch to history
                                        }
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("submit_deposit_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmeraldPrimary,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(imageVector = Icons.Default.AddCard, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "submit_deposit".tr(lang),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }

                2 -> {
                    // FINANCIAL HISTORY TAB
                    if (withdrawals.isEmpty() && transactions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "no_submissions".tr(lang),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Withdrawals
                            if (withdrawals.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Withdrawal Requests",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                items(withdrawals) { item ->
                                    WithdrawalItemCard(item = item, lang = lang)
                                }
                            }

                            // Transactions (Deposits, Welcome Bonus, Task Earnings)
                            if (transactions.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Recent Transactions & Earnings",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                items(transactions) { tx ->
                                    TransactionItemCard(tx = tx, lang = lang)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WithdrawalItemCard(item: WithdrawalEntity, lang: LanguageCode) {
    val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.US).format(Date(item.requestedAt))
    val statusColor = when (item.status) {
        "APPROVED", "PAID" -> EmeraldPrimary
        "REJECTED" -> ErrorRed
        else -> GoldAccent
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("withdrawal_item_${item.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${item.method} (${item.accountNumber})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (item.remarks.isNotBlank()) {
                    Text(
                        text = item.remarks,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "৳ ${String.format(Locale.US, "%.1f", item.amountCurrency)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = EmeraldPrimary
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.status,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = statusColor
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionItemCard(tx: TransactionEntity, lang: LanguageCode) {
    val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.US).format(Date(tx.timestamp))
    val isPositive = tx.coins > 0
    val title = if (lang == LanguageCode.BN) tx.titleBn else tx.titleEn

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "${if (isPositive) "+" else ""}৳ ${Math.abs(tx.coins)}",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = if (isPositive) EmeraldPrimary else ErrorRed
            )
        }
    }
}
