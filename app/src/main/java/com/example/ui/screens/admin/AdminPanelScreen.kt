package com.example.ui.screens.admin

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.data.local.JsonUtils
import com.example.data.local.LanguageCode
import com.example.data.local.entity.AnnouncementEntity
import com.example.data.local.entity.CustomFieldConfig
import com.example.data.local.entity.ProductTaskEntity
import com.example.data.local.entity.TaskGroupEntity
import com.example.data.local.entity.TaskSubmissionEntity
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.WithdrawalEntity
import com.example.ui.components.TakaTopBar
import com.example.ui.localization.tr
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldAccent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminPanelScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val lang by viewModel.currentLanguage.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val configs by viewModel.appConfigs.collectAsState()
    val isUnlocked by viewModel.adminUnlocked.collectAsState()

    val allGroups by viewModel.allTaskGroups.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val allSubmissions by viewModel.allSubmissions.collectAsState()
    val allWithdrawals by viewModel.allWithdrawals.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val allAnnouncements by viewModel.allAnnouncements.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()

    var pinInput by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }

    // Dialog States
    var showGroupDialog by remember { mutableStateOf(false) }
    var editingGroup by remember { mutableStateOf<TaskGroupEntity?>(null) }

    var showProductDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductTaskEntity?>(null) }

    var showAnnouncementDialog by remember { mutableStateOf(false) }
    var editingAnnouncement by remember { mutableStateOf<AnnouncementEntity?>(null) }

    var processingSubmission by remember { mutableStateOf<TaskSubmissionEntity?>(null) }
    var adjustingUser by remember { mutableStateOf<UserEntity?>(null) }
    var showChangePinDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TakaTopBar(
                title = "admin_title".tr(lang),
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
            if (!isUnlocked) {
                // PIN Gate Screen
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Admin Lock",
                        tint = GoldAccent,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "admin_access".tr(lang),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Enter PIN to access full control console (Default: 1234)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { pinInput = it },
                        placeholder = { Text("enter_admin_pin".tr(lang)) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .testTag("admin_pin_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.unlockAdmin(pinInput) },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(50.dp)
                            .testTag("admin_unlock_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text(
                            text = "admin_button".tr(lang),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            } else {
                // Admin Status & Action Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Admin Console (Active)",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldPrimary
                        )
                    }

                    Button(
                        onClick = { showChangePinDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("admin_change_pin_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Change PIN / Pass",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Unlocked Admin Console with 8 tabs
                val tabs = listOf(
                    "Submissions (${allSubmissions.count { it.status == "PENDING" }})",
                    "Groups (${allGroups.size})",
                    "Tasks (${allProducts.size})",
                    "Members (${allUsers.size})",
                    "Cashouts (${allWithdrawals.count { it.status == "PENDING" }})",
                    "Toggles & Maint.",
                    "Notices (${allAnnouncements.size})",
                    "Config & Audit"
                )

                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    edgePadding = 12.dp
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                            modifier = Modifier.testTag("admin_tab_$index")
                        )
                    }
                }

                when (selectedTab) {
                    // TAB 0: SUBMISSIONS REVIEW
                    0 -> AdminSubmissionsTab(
                        submissions = allSubmissions,
                        onSelectForProcessing = { processingSubmission = it }
                    )

                    // TAB 1: TASK GROUPS
                    1 -> AdminGroupsTab(
                        groups = allGroups,
                        onAddGroup = {
                            editingGroup = null
                            showGroupDialog = true
                        },
                        onEditGroup = { g ->
                            editingGroup = g
                            showGroupDialog = true
                        },
                        onDeleteGroup = { g -> viewModel.adminDeleteGroup(g.id) }
                    )

                    // TAB 2: PRODUCTS & TASKS
                    2 -> AdminProductsTab(
                        products = allProducts,
                        groups = allGroups,
                        onAddProduct = {
                            editingProduct = null
                            showProductDialog = true
                        },
                        onEditProduct = { p ->
                            editingProduct = p
                            showProductDialog = true
                        },
                        onDeleteProduct = { p -> viewModel.adminDeleteProduct(p.id) }
                    )

                    // TAB 3: MEMBERS & BALANCES
                    3 -> AdminMembersTab(
                        users = allUsers,
                        onSetTier = { u, tier -> viewModel.adminSetMembership(u.id, tier) },
                        onAdjustBalance = { u -> adjustingUser = u }
                    )

                    // TAB 4: CASHOUTS
                    4 -> AdminCashoutsTab(
                        withdrawals = allWithdrawals,
                        onUpdate = { id, st, note -> viewModel.updateWithdrawalStatus(id, st, note) }
                    )

                    // TAB 5: FEATURE TOGGLES & MAINTENANCE
                    5 -> AdminTogglesTab(
                        configs = configs,
                        onToggle = { key, value -> viewModel.setFeatureToggle(key, value) },
                        onSaveConfig = { key, value -> viewModel.setAppConfig(key, value) }
                    )

                    // TAB 6: NOTICES & ANNOUNCEMENTS
                    6 -> AdminAnnouncementsTab(
                        announcements = allAnnouncements,
                        onAdd = {
                            editingAnnouncement = null
                            showAnnouncementDialog = true
                        },
                        onEdit = { ann ->
                            editingAnnouncement = ann
                            showAnnouncementDialog = true
                        },
                        onDelete = { ann -> viewModel.adminDeleteAnnouncement(ann.id) }
                    )

                    // TAB 7: PLATFORM CONFIG & AUDIT LOGS
                    7 -> AdminLinksAndAuditTab(
                        configs = configs,
                        auditLogs = auditLogs,
                        onSaveConfig = { key, value -> viewModel.setAppConfig(key, value) },
                        onSaveLinks = { tg, grp, fb -> viewModel.updateAdminSocialConfigs(tg, grp, fb) },
                        onSaveRates = { rate, minW, nEn, nBn -> viewModel.updateAdminRatesAndNotices(rate, minW, nEn, nBn) }
                    )
                }
            }
        }
    }

    // Process Submission Dialog (Editable Rate & Final Amount)
    processingSubmission?.let { sub ->
        var editableRateStr by remember { mutableStateOf(sub.applicableRate.toString()) }
        var remarksText by remember { mutableStateOf("") }
        val pointsPerBdt = configs["points_per_bdt"]?.toDoubleOrNull() ?: 100.0

        AlertDialog(
            onDismissRequest = { processingSubmission = null },
            title = { Text("Process Task Submission #${sub.id}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("User: ${sub.userName} (${sub.membershipType})", fontWeight = FontWeight.Bold)
                    Text("Task: ${sub.productTitle} (${sub.groupName})")
                    Text("Submitted Values:", fontWeight = FontWeight.Bold)
                    val values = JsonUtils.parseSubmittedValues(sub.submittedValuesJson)
                    values.forEach { (k, v) ->
                        Text("• $k: $v", style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = editableRateStr,
                        onValueChange = { editableRateStr = it },
                        label = { Text("Applicable Final Rate (BDT)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().testTag("admin_edit_rate_input")
                    )

                    OutlinedTextField(
                        value = remarksText,
                        onValueChange = { remarksText = it },
                        label = { Text("Admin Remarks / Note") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val rate = editableRateStr.toDoubleOrNull() ?: sub.applicableRate
                        viewModel.adminProcessSubmission(
                            submissionId = sub.id,
                            status = "APPROVED",
                            applicableRate = rate,
                            finalAmount = rate,
                            remarks = remarksText
                        )
                        processingSubmission = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Approve & Pay")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val rate = editableRateStr.toDoubleOrNull() ?: sub.applicableRate
                        viewModel.adminProcessSubmission(
                            submissionId = sub.id,
                            status = "REJECTED",
                            applicableRate = rate,
                            finalAmount = 0.0,
                            remarks = remarksText.ifEmpty { "Did not meet requirements" }
                        )
                        processingSubmission = null
                    }
                ) {
                    Text("Reject", color = ErrorRed)
                }
            }
        )
    }

    // Change Admin PIN / Password Dialog
    if (showChangePinDialog) {
        var currentPin by remember { mutableStateOf("") }
        var newPin by remember { mutableStateOf("") }
        var confirmPin by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            title = { Text("Change Admin Password / PIN") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Update the access PIN for this Admin Panel. Default PIN is 1234.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = currentPin,
                        onValueChange = { currentPin = it },
                        label = { Text("Current PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth().testTag("current_pin_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { newPin = it },
                        label = { Text("New PIN (min 4 digits)") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth().testTag("new_pin_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = { confirmPin = it },
                        label = { Text("Confirm New PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth().testTag("confirm_pin_input"),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = viewModel.adminChangePin(currentPin, newPin, confirmPin)
                        if (success) {
                            showChangePinDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Update PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePinDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Adjust Balance Dialog
    adjustingUser?.let { targetUser ->
        var newCoinsStr by remember { mutableStateOf(targetUser.coins.toString()) }
        var reasonText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { adjustingUser = null },
            title = { Text("Adjust Balance: ${targetUser.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Current Balance: ৳${targetUser.coins}", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = newCoinsStr,
                        onValueChange = { newCoinsStr = it },
                        label = { Text("New Balance (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = reasonText,
                        onValueChange = { reasonText = it },
                        label = { Text("Reason for Adjustment") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newCoins = newCoinsStr.toLongOrNull() ?: targetUser.coins
                        viewModel.adminAdjustBalance(targetUser.id, newCoins, reasonText.ifEmpty { "Manual correction" })
                        adjustingUser = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Update Balance")
                }
            },
            dismissButton = {
                TextButton(onClick = { adjustingUser = null }) { Text("Cancel") }
            }
        )
    }

    // Add / Edit Group Dialog
    if (showGroupDialog) {
        var nameEn by remember { mutableStateOf(editingGroup?.nameEn ?: "") }
        var nameBn by remember { mutableStateOf(editingGroup?.nameBn ?: "") }
        var descEn by remember { mutableStateOf(editingGroup?.descriptionEn ?: "") }
        var accessRule by remember { mutableStateOf(editingGroup?.accessRule ?: "BOTH") }
        var isEnabled by remember { mutableStateOf(editingGroup?.isEnabled ?: true) }

        AlertDialog(
            onDismissRequest = { showGroupDialog = false },
            title = { Text(if (editingGroup == null) "Add Task Group" else "Edit Task Group") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nameEn,
                        onValueChange = { nameEn = it },
                        label = { Text("Group Name (English)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = nameBn,
                        onValueChange = { nameBn = it },
                        label = { Text("Group Name (বাংলা)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = descEn,
                        onValueChange = { descEn = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Membership Access: $accessRule", fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("BOTH", "FREE", "PREMIUM").forEach { rule ->
                            Button(
                                onClick = { accessRule = rule },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (accessRule == rule) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text(rule, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Group Enabled:")
                        Switch(checked = isEnabled, onCheckedChange = { isEnabled = it })
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameEn.isNotBlank()) {
                            val group = TaskGroupEntity(
                                id = editingGroup?.id ?: 0L,
                                nameEn = nameEn.trim(),
                                nameBn = nameBn.ifBlank { nameEn }.trim(),
                                descriptionEn = descEn.trim(),
                                descriptionBn = descEn.trim(),
                                accessRule = accessRule,
                                isEnabled = isEnabled
                            )
                            viewModel.adminSaveGroup(group)
                            showGroupDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Save Group")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGroupDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Add / Edit Product & Custom Fields Dialog
    if (showProductDialog) {
        var titleEn by remember { mutableStateOf(editingProduct?.titleEn ?: "") }
        var titleBn by remember { mutableStateOf(editingProduct?.titleBn ?: "") }
        var category by remember { mutableStateOf(editingProduct?.categoryName ?: "General") }
        var descEn by remember { mutableStateOf(editingProduct?.descriptionEn ?: "") }
        var rateStr by remember { mutableStateOf(editingProduct?.rateAmount?.toString() ?: "20.0") }
        var accessRule by remember { mutableStateOf(editingProduct?.accessRule ?: "BOTH") }
        var selectedGroupId by remember { mutableStateOf(editingProduct?.groupId ?: (allGroups.firstOrNull()?.id ?: 1L)) }
        var isEnabled by remember { mutableStateOf(editingProduct?.isEnabled ?: true) }
        var maintenanceNotice by remember { mutableStateOf(editingProduct?.maintenanceNotice ?: "") }

        // Fields config builder list
        val fieldsList = remember {
            mutableStateListOf<CustomFieldConfig>().apply {
                if (editingProduct != null) {
                    addAll(JsonUtils.parseFieldsConfig(editingProduct!!.fieldsConfigJson))
                } else {
                    add(CustomFieldConfig("field_1", "Information Text", "Enter details", "e.g. sample text", true, 1))
                }
            }
        }

        AlertDialog(
            onDismissRequest = { showProductDialog = false },
            title = { Text(if (editingProduct == null) "Create Product / Task" else "Edit Product / Task") },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = titleEn,
                            onValueChange = { titleEn = it },
                            label = { Text("Task Title (English)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = titleBn,
                            onValueChange = { titleBn = it },
                            label = { Text("Task Title (বাংলা)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = rateStr,
                            onValueChange = { rateStr = it },
                            label = { Text("Rate / Payout (BDT)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Text("Task Group:", fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            allGroups.forEach { g ->
                                val isSel = selectedGroupId == g.id
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSel) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { selectedGroupId = g.id }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(g.nameEn.take(12), fontSize = 11.sp, color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                    item {
                        Text("Access Rule: $accessRule", fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("BOTH", "FREE", "PREMIUM").forEach { rule ->
                                Button(
                                    onClick = { accessRule = rule },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (accessRule == rule) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Text(rule, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Task Active (ON/OFF):", fontWeight = FontWeight.Bold)
                                    Switch(checked = isEnabled, onCheckedChange = { isEnabled = it })
                                }
                                OutlinedTextField(
                                    value = maintenanceNotice,
                                    onValueChange = { maintenanceNotice = it },
                                    label = { Text("Offline / Maintenance Notice (Optional)") },
                                    placeholder = { Text("Shown to users if task is turned OFF") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Submission Fields (${fieldsList.size}):", fontWeight = FontWeight.Bold)
                            Button(
                                onClick = {
                                    val nextIdx = fieldsList.size + 1
                                    fieldsList.add(CustomFieldConfig("field_$nextIdx", "Field $nextIdx", "Provide info", "Placeholder $nextIdx", true, nextIdx))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black)
                            ) {
                                Text("+ Add Field", fontSize = 11.sp)
                            }
                        }
                    }

                    items(fieldsList.size) { idx ->
                        val f = fieldsList[idx]
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Field #${idx + 1}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    if (fieldsList.size > 1) {
                                        IconButton(onClick = { fieldsList.removeAt(idx) }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = ErrorRed, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                                OutlinedTextField(
                                    value = f.label,
                                    onValueChange = { fieldsList[idx] = f.copy(label = it) },
                                    label = { Text("Label (Shown above box)") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = f.placeholder,
                                    onValueChange = { fieldsList[idx] = f.copy(placeholder = it) },
                                    label = { Text("Placeholder (Inside watermark)") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (titleEn.isNotBlank()) {
                            val rate = rateStr.toDoubleOrNull() ?: 20.0
                            val jsonFields = JsonUtils.serializeFieldsConfig(fieldsList.toList())
                            val product = ProductTaskEntity(
                                id = editingProduct?.id ?: 0L,
                                groupId = selectedGroupId,
                                categoryName = category.ifBlank { "General" },
                                titleEn = titleEn.trim(),
                                titleBn = titleBn.ifBlank { titleEn }.trim(),
                                descriptionEn = descEn.trim(),
                                descriptionBn = descEn.trim(),
                                rateAmount = rate,
                                rateType = "BDT",
                                accessRule = accessRule,
                                isEnabled = isEnabled,
                                maintenanceNotice = maintenanceNotice.trim(),
                                fieldsConfigJson = jsonFields
                            )
                            viewModel.adminSaveProduct(product)
                            showProductDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Save Task")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProductDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Add / Edit Announcement Dialog
    if (showAnnouncementDialog) {
        var titleEn by remember { mutableStateOf(editingAnnouncement?.titleEn ?: "") }
        var titleBn by remember { mutableStateOf(editingAnnouncement?.titleBn ?: "") }
        var bodyEn by remember { mutableStateOf(editingAnnouncement?.bodyEn ?: "") }
        var bodyBn by remember { mutableStateOf(editingAnnouncement?.bodyBn ?: "") }
        var targetAudience by remember { mutableStateOf(editingAnnouncement?.targetAudience ?: "ALL") }
        var isImportant by remember { mutableStateOf(editingAnnouncement?.isImportant ?: false) }

        AlertDialog(
            onDismissRequest = { showAnnouncementDialog = false },
            title = { Text(if (editingAnnouncement == null) "New Announcement" else "Edit Announcement") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = titleEn,
                        onValueChange = { titleEn = it },
                        label = { Text("Title (English)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = titleBn,
                        onValueChange = { titleBn = it },
                        label = { Text("Title (বাংলা)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = bodyEn,
                        onValueChange = { bodyEn = it },
                        label = { Text("Message Body") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    Text("Target Audience: $targetAudience", fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("ALL", "FREE", "PREMIUM").forEach { aud ->
                            Button(
                                onClick = { targetAudience = aud },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (targetAudience == aud) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text(aud, fontSize = 11.sp)
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Mark as Important / Priority:")
                        Switch(checked = isImportant, onCheckedChange = { isImportant = it })
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (titleEn.isNotBlank()) {
                            val ann = AnnouncementEntity(
                                id = editingAnnouncement?.id ?: 0L,
                                titleEn = titleEn.trim(),
                                titleBn = titleBn.ifBlank { titleEn }.trim(),
                                bodyEn = bodyEn.trim(),
                                bodyBn = bodyBn.ifBlank { bodyEn }.trim(),
                                targetAudience = targetAudience,
                                isImportant = isImportant
                            )
                            viewModel.adminSaveAnnouncement(ann)
                            showAnnouncementDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Save Announcement")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAnnouncementDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun AdminSubmissionsTab(
    submissions: List<TaskSubmissionEntity>,
    onSelectForProcessing: (TaskSubmissionEntity) -> Unit
) {
    if (submissions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text("No user submissions yet.")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(submissions) { sub ->
                val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.US).format(Date(sub.submittedAt))
                val statusColor = when (sub.status) {
                    "APPROVED" -> EmeraldPrimary
                    "REJECTED" -> ErrorRed
                    else -> GoldAccent
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectForProcessing(sub) }
                        .testTag("admin_sub_${sub.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${sub.userName} (${sub.membershipType})", fontWeight = FontWeight.Bold)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(statusColor.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(sub.status, fontWeight = FontWeight.Bold, color = statusColor, fontSize = 11.sp)
                            }
                        }
                        Text("${sub.productTitle} • $dateStr", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Rate: ৳${sub.applicableRate} → Final: ৳${sub.finalAmount}", fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                            Text("Click to Process ➔", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminGroupsTab(
    groups: List<TaskGroupEntity>,
    onAddGroup: () -> Unit,
    onEditGroup: (TaskGroupEntity) -> Unit,
    onDeleteGroup: (TaskGroupEntity) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(
            onClick = onAddGroup,
            modifier = Modifier.fillMaxWidth().height(46.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Add Task Group")
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(groups) { g ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(g.nameEn, fontWeight = FontWeight.Bold)
                            Text("Access: ${g.accessRule} • Status: ${if (g.isEnabled) "Active" else "Disabled"}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Row {
                            IconButton(onClick = { onEditGroup(g) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = EmeraldPrimary)
                            }
                            IconButton(onClick = { onDeleteGroup(g) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminProductsTab(
    products: List<ProductTaskEntity>,
    groups: List<TaskGroupEntity>,
    onAddProduct: () -> Unit,
    onEditProduct: (ProductTaskEntity) -> Unit,
    onDeleteProduct: (ProductTaskEntity) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(
            onClick = onAddProduct,
            modifier = Modifier.fillMaxWidth().height(46.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Add Product / Task", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(products) { p ->
                val groupName = groups.find { it.id == p.groupId }?.nameEn ?: "Group #${p.groupId}"
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(p.titleEn, fontWeight = FontWeight.Bold)
                            Text("Group: $groupName • Rate: ৳${p.rateAmount} • Access: ${p.accessRule}", fontSize = 11.sp, color = EmeraldPrimary)
                        }

                        Row {
                            IconButton(onClick = { onEditProduct(p) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = EmeraldPrimary)
                            }
                            IconButton(onClick = { onDeleteProduct(p) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminMembersTab(
    users: List<UserEntity>,
    onSetTier: (UserEntity, String) -> Unit,
    onAdjustBalance: (UserEntity) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(users) { u ->
            val isPremium = u.membershipTier == "PREMIUM"
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(u.name, fontWeight = FontWeight.Bold)
                            Text(u.email, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isPremium) GoldAccent else EmeraldPrimary.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(u.membershipTier, color = if (isPremium) Color.Black else EmeraldPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Coins: ${u.coins} (৳${u.coins / 100})", fontWeight = FontWeight.Bold, color = GoldAccent)

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = { onSetTier(u, if (isPremium) "FREE" else "PREMIUM") },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(if (isPremium) "Set FREE" else "Set PREMIUM", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface)
                            }

                            Button(
                                onClick = { onAdjustBalance(u) },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Coins +/-", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminCashoutsTab(
    withdrawals: List<WithdrawalEntity>,
    onUpdate: (Long, String, String) -> Unit
) {
    if (withdrawals.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text("No cashout requests.")
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(withdrawals) { item ->
                AdminWithdrawalRow(item = item, onUpdateStatus = { st, note -> onUpdate(item.id, st, note) })
            }
        }
    }
}

@Composable
fun AdminTogglesTab(
    configs: Map<String, String>,
    onToggle: (String, Boolean) -> Unit,
    onSaveConfig: (String, String) -> Unit
) {
    var maintTitleEn by remember(configs) { mutableStateOf(configs["maintenance_title_en"] ?: "System Maintenance in Progress") }
    var maintTitleBn by remember(configs) { mutableStateOf(configs["maintenance_title_bn"] ?: "সিস্টেম রক্ষণাবেক্ষণ চলছে") }
    var maintMsgEn by remember(configs) { mutableStateOf(configs["maintenance_message_en"] ?: "We are improving our platform to give you the best experience. The app will return shortly!") }
    var maintMsgBn by remember(configs) { mutableStateOf(configs["maintenance_message_bn"] ?: "উন্নত সেবার জন্য প্ল্যাটফর্ম রক্ষণাবেক্ষণ চলছে। দ্রুতই অ্যাপ পুনরায় চালু হবে!") }
    var maintStartTime by remember(configs) { mutableStateOf(configs["maintenance_start_time"] ?: "") }
    var maintEndTime by remember(configs) { mutableStateOf(configs["maintenance_end_time"] ?: "") }
    var maintTgUrl by remember(configs) { mutableStateOf(configs["telegram_group_url"] ?: "https://t.me/takareward_support") }

    val isMaintEnabled = configs["maintenance_mode_enabled"] == "true"

    val features = listOf(
        "feature_registration_enabled" to "User Registration Switch",
        "feature_require_activation" to "Require Account Activation Deposit",
        "feature_free_membership_enabled" to "Free Membership Access",
        "feature_premium_membership_enabled" to "Pro Membership Upgrades",
        "feature_spin_enabled" to "Lucky Spin Wheel",
        "feature_scratch_enabled" to "Scratch & Win Cards",
        "feature_quiz_enabled" to "Math Quiz Challenge",
        "feature_sell_tasks_enabled" to "Text-Only Sell / Buy Tasks",
        "feature_community_links_enabled" to "Community & Social Links",
        "feature_withdrawals_enabled" to "Cashout / Withdrawal System",
        "feature_announcements_enabled" to "Announcements & Notices"
    )

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // GLOBAL MAINTENANCE MODE CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("admin_maintenance_config_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (isMaintEnabled) ErrorRed.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "🚨 Global Maintenance Mode",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isMaintEnabled) ErrorRed else MaterialTheme.colorScheme.primary
                            )
                            Text(
                                if (isMaintEnabled) "APP IS CURRENTLY OFFLINE FOR MEMBERS" else "App is ONLINE & Active",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isMaintEnabled) ErrorRed else EmeraldPrimary
                            )
                        }

                        Switch(
                            checked = isMaintEnabled,
                            onCheckedChange = { onToggle("maintenance_mode_enabled", it) },
                            modifier = Modifier.testTag("admin_maintenance_switch")
                        )
                    }

                    HorizontalDivider()

                    Text("Maintenance Screen Content & Scheduling", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                    OutlinedTextField(
                        value = maintTitleEn,
                        onValueChange = { maintTitleEn = it },
                        label = { Text("Title (English)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = maintTitleBn,
                        onValueChange = { maintTitleBn = it },
                        label = { Text("Title (বাংলা)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = maintMsgEn,
                        onValueChange = { maintMsgEn = it },
                        label = { Text("Notice / Message (English)") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = maintMsgBn,
                        onValueChange = { maintMsgBn = it },
                        label = { Text("Notice / Message (বাংলা)") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = maintStartTime,
                            onValueChange = { maintStartTime = it },
                            label = { Text("Start Time (Optional)") },
                            placeholder = { Text("e.g. 10:00 PM") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = maintEndTime,
                            onValueChange = { maintEndTime = it },
                            label = { Text("End Time (Optional)") },
                            placeholder = { Text("e.g. 06:00 AM") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = maintTgUrl,
                        onValueChange = { maintTgUrl = it },
                        label = { Text("Support Telegram Group URL") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            onSaveConfig("maintenance_title_en", maintTitleEn)
                            onSaveConfig("maintenance_title_bn", maintTitleBn)
                            onSaveConfig("maintenance_message_en", maintMsgEn)
                            onSaveConfig("maintenance_message_bn", maintMsgBn)
                            onSaveConfig("maintenance_start_time", maintStartTime)
                            onSaveConfig("maintenance_end_time", maintEndTime)
                            onSaveConfig("telegram_group_url", maintTgUrl)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isMaintEnabled) ErrorRed else EmeraldPrimary)
                    ) {
                        Text("Save Maintenance Settings", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text("Centralized Feature Switches (ON / OFF)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Instantly toggle platform features without deleting data", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        items(features) { (key, label) ->
            val isEnabled = configs[key] != "false"
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, fontWeight = FontWeight.Bold)
                    Switch(checked = isEnabled, onCheckedChange = { onToggle(key, it) })
                }
            }
        }
    }
}

@Composable
fun AdminAnnouncementsTab(
    announcements: List<AnnouncementEntity>,
    onAdd: () -> Unit,
    onEdit: (AnnouncementEntity) -> Unit,
    onDelete: (AnnouncementEntity) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(
            onClick = onAdd,
            modifier = Modifier.fillMaxWidth().height(46.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Create Announcement")
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(announcements) { ann ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(ann.titleEn, fontWeight = FontWeight.Bold)
                            Text("Audience: ${ann.targetAudience} • ${if (ann.isImportant) "Priority" else "Normal"}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }

                        Row {
                            IconButton(onClick = { onEdit(ann) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = EmeraldPrimary)
                            }
                            IconButton(onClick = { onDelete(ann) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminLinksAndAuditTab(
    configs: Map<String, String>,
    auditLogs: List<com.example.data.local.entity.AuditLogEntity>,
    onSaveConfig: (String, String) -> Unit,
    onSaveLinks: (String, String, String) -> Unit,
    onSaveRates: (String, String, String, String) -> Unit
) {
    var activationFeeBdt by remember(configs) { mutableStateOf(configs["account_activation_fee_bdt"] ?: "100") }
    var proPercent by remember(configs) { mutableStateOf(configs["pro_activation_percent"] ?: "150") }
    var proDailyLimit by remember(configs) { mutableStateOf(configs["pro_daily_task_limit"] ?: "10") }

    var minWithdrawBdt by remember(configs) { mutableStateOf(configs["min_withdraw_bdt"] ?: "50") }
    var withdrawChargePercent by remember(configs) { mutableStateOf(configs["withdraw_charge_percent"] ?: "5") }

    var bkashNum by remember(configs) { mutableStateOf(configs["deposit_bkash_number"] ?: "01700000000 (Send Money)") }
    var nagadNum by remember(configs) { mutableStateOf(configs["deposit_nagad_number"] ?: "01800000000 (Send Money)") }
    var rocketNum by remember(configs) { mutableStateOf(configs["deposit_rocket_number"] ?: "01900000000 (Send Money)") }
    var upayNum by remember(configs) { mutableStateOf(configs["deposit_upay_number"] ?: "01600000000 (Send Money)") }
    var usdtAddr by remember(configs) { mutableStateOf(configs["deposit_usdt_address"] ?: "TYD9q3...TRC20AddressHere") }

    var referralBaseUrl by remember(configs) { mutableStateOf(configs["referral_base_url"] ?: "https://incomezonex.com/join?ref=") }

    var tgChannel by remember(configs) { mutableStateOf(configs["telegram_channel_url"] ?: "https://t.me/takareward_channel") }
    var tgGroup by remember(configs) { mutableStateOf(configs["telegram_group_url"] ?: "https://t.me/takareward_support") }
    var fbPage by remember(configs) { mutableStateOf(configs["facebook_page_url"] ?: "https://facebook.com/takareward.official") }
    var noticeEn by remember(configs) { mutableStateOf(configs["notice_text_en"] ?: "") }
    var noticeBn by remember(configs) { mutableStateOf(configs["notice_text_bn"] ?: "") }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // PRO & ACTIVATION SYSTEM CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("⭐ Pro System & Account Activation", fontWeight = FontWeight.Bold, color = GoldAccent)
                    Text("Configure percentage-based Pro requirements and daily limits", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    OutlinedTextField(
                        value = activationFeeBdt,
                        onValueChange = { activationFeeBdt = it },
                        label = { Text("Base Account Activation Fee (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = proPercent,
                        onValueChange = { proPercent = it },
                        label = { Text("Pro Upgrade Requirement (% of Base Fee)") },
                        placeholder = { Text("e.g. 150 for 150%") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    val base = activationFeeBdt.toDoubleOrNull() ?: 100.0
                    val pct = proPercent.toDoubleOrNull() ?: 150.0
                    Text(
                        "Calculated Pro Upgrade Cost: ৳${(base * pct / 100.0).toInt()} ($pct% of ৳${base.toInt()})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )

                    OutlinedTextField(
                        value = proDailyLimit,
                        onValueChange = { proDailyLimit = it },
                        label = { Text("Max Daily Pro Tasks Allowed Per Account") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            onSaveConfig("account_activation_fee_bdt", activationFeeBdt)
                            onSaveConfig("pro_activation_percent", proPercent)
                            onSaveConfig("pro_daily_task_limit", proDailyLimit)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black)
                    ) {
                        Text("Save Pro & Activation Rules", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // CASHOUT & WITHDRAWAL FEES CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("💸 Cashout Rules & Withdrawal Fees", fontWeight = FontWeight.Bold, color = EmeraldPrimary)

                    OutlinedTextField(
                        value = minWithdrawBdt,
                        onValueChange = { minWithdrawBdt = it },
                        label = { Text("Minimum Withdrawal Amount (৳ BDT)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = withdrawChargePercent,
                        onValueChange = { withdrawChargePercent = it },
                        label = { Text("Withdrawal Fee / Charge Percentage (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            onSaveConfig("min_withdraw_bdt", minWithdrawBdt)
                            onSaveConfig("withdraw_charge_percent", withdrawChargePercent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("Save Cashout Settings", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // DEPOSIT PAYMENT NUMBERS CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("💳 Deposit Payment Accounts", fontWeight = FontWeight.Bold)
                    Text("Numbers and addresses shown to members during deposit/activation", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    OutlinedTextField(value = bkashNum, onValueChange = { bkashNum = it }, label = { Text("bKash Deposit Number") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = nagadNum, onValueChange = { nagadNum = it }, label = { Text("Nagad Deposit Number") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = rocketNum, onValueChange = { rocketNum = it }, label = { Text("Rocket Deposit Number") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = upayNum, onValueChange = { upayNum = it }, label = { Text("Upay Deposit Number") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = usdtAddr, onValueChange = { usdtAddr = it }, label = { Text("USDT TRC20 Address") }, modifier = Modifier.fillMaxWidth())

                    Button(
                        onClick = {
                            onSaveConfig("deposit_bkash_number", bkashNum)
                            onSaveConfig("deposit_nagad_number", nagadNum)
                            onSaveConfig("deposit_rocket_number", rocketNum)
                            onSaveConfig("deposit_upay_number", upayNum)
                            onSaveConfig("deposit_usdt_address", usdtAddr)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("Save Payment Accounts", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // REFERRAL LINK SETTINGS CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🔗 Referral System Settings", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = referralBaseUrl,
                        onValueChange = { referralBaseUrl = it },
                        label = { Text("Base Referral Link (Web / Deep Link)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { onSaveConfig("referral_base_url", referralBaseUrl) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("Save Referral Base URL", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // COMMUNITY & NOTICES CARDS
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Configurable Community Links", fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = tgChannel, onValueChange = { tgChannel = it }, label = { Text("Telegram Channel") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = tgGroup, onValueChange = { tgGroup = it }, label = { Text("Telegram Group") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = fbPage, onValueChange = { fbPage = it }, label = { Text("Facebook Page") }, modifier = Modifier.fillMaxWidth())
                    Button(
                        onClick = { onSaveLinks(tgChannel, tgGroup, fbPage) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("Save Community Links")
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Dashboard Announcements & Notices", fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = noticeEn, onValueChange = { noticeEn = it }, label = { Text("Notice (English)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = noticeBn, onValueChange = { noticeBn = it }, label = { Text("Notice (বাংলা)") }, modifier = Modifier.fillMaxWidth())
                    Button(
                        onClick = { onSaveRates("100", minWithdrawBdt, noticeEn, noticeBn) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black)
                    ) {
                        Text("Save Announcements")
                    }
                }
            }
        }

        item {
            Text("Recent Admin Audit Logs (${auditLogs.size})", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }

        items(auditLogs.take(20)) { log ->
            val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.US).format(Date(log.timestamp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(log.action, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EmeraldPrimary)
                        Text(dateStr, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(log.details, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
fun AdminWithdrawalRow(
    item: WithdrawalEntity,
    onUpdateStatus: (String, String) -> Unit
) {
    val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.US).format(Date(item.requestedAt))
    val statusColor = when (item.status) {
        "APPROVED", "PAID" -> EmeraldPrimary
        "REJECTED" -> ErrorRed
        else -> GoldAccent
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_withdrawal_${item.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${item.userName} (${item.userPhone})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${item.method} • Account: ${item.accountNumber}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "৳ ${String.format(Locale.US, "%.1f", item.amountCurrency)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = EmeraldPrimary
                    )
                    Text(
                        text = "${item.coins} Coins",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusColor.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.status,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = statusColor
                        )
                    }
                }
            }

            if (item.status == "PENDING") {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onUpdateStatus("PAID", "Payment completed") },
                        modifier = Modifier.weight(1f).height(38.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("Mark Paid", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onUpdateStatus("APPROVED", "Approved for processing") },
                        modifier = Modifier.weight(1f).height(38.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black)
                    ) {
                        Text("Approve", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onUpdateStatus("REJECTED", "Account invalid or policy violation") },
                        modifier = Modifier.weight(1f).height(38.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                    ) {
                        Text("Reject", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
