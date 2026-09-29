package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.JsonUtils
import com.example.data.local.LanguageCode
import com.example.data.local.PreferencesManager
import com.example.data.local.ThemeMode
import com.example.data.local.entity.AnnouncementEntity
import com.example.data.local.entity.AppConfigEntity
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.CustomFieldConfig
import com.example.data.local.entity.DailyTaskStateEntity
import com.example.data.local.entity.ProductTaskEntity
import com.example.data.local.entity.TaskGroupEntity
import com.example.data.local.entity.TaskSubmissionEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.WithdrawalEntity
import com.example.data.repository.AnnouncementRepository
import com.example.data.repository.ConfigRepository
import com.example.data.repository.ProductRepository
import com.example.data.repository.RewardRepository
import com.example.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UiMessage(
    val text: String,
    val isError: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val preferencesManager = PreferencesManager(application)

    val userRepository = UserRepository(db.userDao(), db.transactionDao(), db.appConfigDao(), db.auditLogDao())
    val rewardRepository = RewardRepository(db.userDao(), db.transactionDao(), db.withdrawalDao(), db.dailyTaskDao())
    val productRepository = ProductRepository(
        db.taskGroupDao(),
        db.productTaskDao(),
        db.taskSubmissionDao(),
        db.userDao(),
        db.transactionDao(),
        db.auditLogDao()
    )
    val announcementRepository = AnnouncementRepository(db.announcementDao(), db.notificationReadDao())
    val configRepository = ConfigRepository(db.appConfigDao())

    val themeMode: StateFlow<ThemeMode> = preferencesManager.themeModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.DARK)

    val currentLanguage: StateFlow<LanguageCode> = preferencesManager.languageFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LanguageCode.BN)

    private val _messageFlow = MutableSharedFlow<UiMessage>()
    val messageFlow: SharedFlow<UiMessage> = _messageFlow.asSharedFlow()

    // Current User
    val currentUserId: StateFlow<Long?> = preferencesManager.loggedInUserIdFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1L)

    val currentUser: StateFlow<UserEntity?> = currentUserId.flatMapLatest { id ->
        if (id != null) {
            userRepository.getUserById(id)
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // User Transactions & Withdrawals
    val userTransactions: StateFlow<List<TransactionEntity>> = currentUserId.flatMapLatest { id ->
        if (id != null) rewardRepository.getUserTransactions(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userWithdrawals: StateFlow<List<WithdrawalEntity>> = currentUserId.flatMapLatest { id ->
        if (id != null) rewardRepository.getUserWithdrawals(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All App Configs & Feature Toggles
    val appConfigs: StateFlow<Map<String, String>> = configRepository.getAllConfigs().flatMapLatest { list ->
        flowOf(list.associate { it.configKey to it.configValue })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Task Groups & Products
    val allTaskGroups: StateFlow<List<TaskGroupEntity>> = productRepository.getAllGroups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val enabledTaskGroups: StateFlow<List<TaskGroupEntity>> = productRepository.getEnabledGroups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProducts: StateFlow<List<ProductTaskEntity>> = productRepository.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Submissions
    val userSubmissions: StateFlow<List<TaskSubmissionEntity>> = currentUserId.flatMapLatest { id ->
        if (id != null) productRepository.getSubmissionsByUser(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSubmissions: StateFlow<List<TaskSubmissionEntity>> = productRepository.getAllSubmissions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Announcements & Notifications
    val allAnnouncements: StateFlow<List<AnnouncementEntity>> = announcementRepository.getAllAnnouncements()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeAnnouncements: StateFlow<List<AnnouncementEntity>> = announcementRepository.getActiveAnnouncements()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val readAnnouncementIds: StateFlow<List<Long>> = currentUserId.flatMapLatest { id ->
        if (id != null) announcementRepository.getReadIds(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filter announcements visible to current user based on membership
    val visibleAnnouncements: StateFlow<List<AnnouncementEntity>> = combine(
        activeAnnouncements,
        currentUser
    ) { announcements, user ->
        val tier = user?.membershipTier ?: "FREE"
        announcements.filter { ann ->
            when (ann.targetAudience) {
                "ALL" -> true
                "FREE" -> tier == "FREE"
                "PREMIUM" -> tier == "PREMIUM"
                else -> true
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadCount: StateFlow<Int> = combine(
        visibleAnnouncements,
        readAnnouncementIds
    ) { announcements, readIds ->
        announcements.count { it.id !in readIds }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Income Summary Breakdowns (BDT)
    val totalIncomeBdt: StateFlow<Double> = userSubmissions.map { list ->
        list.filter { it.status == "APPROVED" }.sumOf { it.finalAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val socialIncomeBdt: StateFlow<Double> = userSubmissions.map { list ->
        list.filter { it.status == "APPROVED" && it.groupId == 1L }.sumOf { it.finalAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val textSellIncomeBdt: StateFlow<Double> = userSubmissions.map { list ->
        list.filter { it.status == "APPROVED" && it.groupId == 2L }.sumOf { it.finalAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val vipIncomeBdt: StateFlow<Double> = userSubmissions.map { list ->
        list.filter { it.status == "APPROVED" && it.groupId == 3L }.sumOf { it.finalAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val isMaintenanceActive: StateFlow<Boolean> = appConfigs.map { configs ->
        configs["maintenance_mode_enabled"] == "true"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Admin Data
    val allWithdrawals: StateFlow<List<WithdrawalEntity>> = rewardRepository.getAllWithdrawals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = userRepository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLogEntity>> = db.auditLogDao().getRecentAuditLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _adminUnlocked = MutableStateFlow(false)
    val adminUnlocked: StateFlow<Boolean> = _adminUnlocked.asStateFlow()

    // Daily Tasks Status
    private val _spinTaskState = MutableStateFlow<DailyTaskStateEntity?>(null)
    val spinTaskState: StateFlow<DailyTaskStateEntity?> = _spinTaskState.asStateFlow()

    private val _scratchTaskState = MutableStateFlow<DailyTaskStateEntity?>(null)
    val scratchTaskState: StateFlow<DailyTaskStateEntity?> = _scratchTaskState.asStateFlow()

    private val _checkInStreak = MutableStateFlow(1L)
    val checkInStreak: StateFlow<Long> = _checkInStreak.asStateFlow()

    private val _isCheckedInToday = MutableStateFlow(false)
    val isCheckedInToday: StateFlow<Boolean> = _isCheckedInToday.asStateFlow()

    init {
        viewModelScope.launch {
            AppDatabase.populateDefaultData(db)
            refreshTaskStates()
            rewardRepository.cleanOldApprovedSubmissions(db.taskSubmissionDao())
        }
    }

    fun isFeatureEnabled(key: String, default: Boolean = true): Boolean {
        val config = appConfigs.value[key]
        return if (config != null) config == "true" else default
    }

    fun refreshTaskStates() {
        viewModelScope.launch {
            val uid = currentUserId.value ?: 1L
            _spinTaskState.value = rewardRepository.getTaskState(uid, "SPIN", 10)
            _scratchTaskState.value = rewardRepository.getTaskState(uid, "SCRATCH", 10)

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            preferencesManager.lastCheckInDateFlow.collect { lastDate ->
                _isCheckedInToday.value = (lastDate == today)
            }
        }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            val next = if (themeMode.value == ThemeMode.DARK) ThemeMode.LIGHT else ThemeMode.DARK
            preferencesManager.setThemeMode(next)
        }
    }

    fun toggleLanguage() {
        viewModelScope.launch {
            val next = if (currentLanguage.value == LanguageCode.EN) LanguageCode.BN else LanguageCode.EN
            preferencesManager.setLanguage(next)
        }
    }

    fun setLanguage(lang: LanguageCode) {
        viewModelScope.launch {
            preferencesManager.setLanguage(lang)
        }
    }

    fun showMessage(message: String, isError: Boolean = false) {
        viewModelScope.launch {
            _messageFlow.emit(UiMessage(message, isError))
        }
    }

    // User Task Form Submission
    fun submitTaskForm(
        product: ProductTaskEntity,
        group: TaskGroupEntity,
        fieldValues: Map<String, String>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val fieldsConfig = JsonUtils.parseFieldsConfig(product.fieldsConfigJson)

            // Validate required fields
            for (field in fieldsConfig) {
                if (field.isRequired) {
                    val value = fieldValues[field.fieldId]?.trim().orEmpty()
                    if (value.isEmpty()) {
                        showMessage("Please fill in: ${field.label}", isError = true)
                        return@launch
                    }
                }
            }

            val jsonValues = JsonUtils.serializeSubmittedValues(fieldValues)
            val result = productRepository.submitTask(
                userId = user.id,
                userName = user.name,
                membershipType = user.membershipTier,
                groupId = group.id,
                groupName = group.nameEn,
                productId = product.id,
                productTitle = product.titleEn,
                rateAmount = product.rateAmount,
                submittedValuesJson = jsonValues
            )

            result.onSuccess {
                showMessage("Task submitted successfully! Admin will verify and reward you.")
                onSuccess()
            }.onFailure {
                showMessage(it.message ?: "Submission failed", isError = true)
            }
        }
    }

    // Membership Upgrade
    fun upgradeToPremium(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val cost = appConfigs.value["premium_upgrade_cost_coins"]?.toLongOrNull() ?: 3000L
            val result = userRepository.upgradeToPremium(user.id, cost)
            result.onSuccess {
                showMessage("Upgraded to Premium Member! All VIP tasks are now unlocked.")
                onSuccess()
            }.onFailure {
                showMessage(it.message ?: "Upgrade failed", isError = true)
            }
        }
    }

    // Daily Claim & Mini Games
    fun claimDailyCheckIn() {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            if (_isCheckedInToday.value) {
                showMessage("Already claimed today's reward! Come back tomorrow.", isError = true)
                return@launch
            }

            val currentStreak = _checkInStreak.value
            val newStreak = (currentStreak % 7) + 1
            val baseCoins = 50L
            val bonusCoins = baseCoins + (newStreak * 10L)

            userRepository.addRewardCoins(
                userId = user.id,
                coins = bonusCoins,
                type = "DAILY_CHECKIN",
                titleEn = "Daily Check-in (Day $newStreak bonus)",
                titleBn = "দৈনিক চেক-ইন (দিন $newStreak বোনাস)"
            )

            preferencesManager.recordCheckIn(today, newStreak)
            _checkInStreak.value = newStreak
            _isCheckedInToday.value = true
            showMessage("Claimed ৳$bonusCoins for Day $newStreak!")
        }
    }

    fun onSpinComplete(wonCoins: Long) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val state = _spinTaskState.value ?: rewardRepository.getTaskState(user.id, "SPIN", 10)
            if (state.countCompleted >= state.maxAllowed) {
                showMessage("Daily spin limit reached (10/10)!", isError = true)
                return@launch
            }

            val updated = rewardRepository.incrementTaskCompleted(state)
            _spinTaskState.value = updated

            userRepository.addRewardCoins(
                userId = user.id,
                coins = wonCoins,
                type = "SPIN_WHEEL",
                titleEn = "Lucky Spin Reward",
                titleBn = "লাকি স্পিন রিওয়ার্ড"
            )
            showMessage("You won ৳$wonCoins from Lucky Spin!")
        }
    }

    fun onScratchComplete(wonCoins: Long) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val state = _scratchTaskState.value ?: rewardRepository.getTaskState(user.id, "SCRATCH", 10)
            if (state.countCompleted >= state.maxAllowed) {
                showMessage("Daily scratch limit reached (10/10)!", isError = true)
                return@launch
            }

            val updated = rewardRepository.incrementTaskCompleted(state)
            _scratchTaskState.value = updated

            userRepository.addRewardCoins(
                userId = user.id,
                coins = wonCoins,
                type = "SCRATCH_CARD",
                titleEn = "Scratch Card Bonus",
                titleBn = "স্ক্র্যাচ কার্ড বোনাস"
            )
            showMessage("Added ৳$wonCoins from Scratch Card!")
        }
    }

    fun onQuizCompleted(correctCount: Int, coinsPerAnswer: Long = 25) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val totalCoins = correctCount * coinsPerAnswer
            if (totalCoins > 0) {
                userRepository.addRewardCoins(
                    userId = user.id,
                    coins = totalCoins,
                    type = "MATH_QUIZ",
                    titleEn = "Speed Math Quiz Reward ($correctCount correct)",
                    titleBn = "স্পীড ম্যাথ কুইজ রিওয়ার্ড ($correctCount টি সঠিক)"
                )
                showMessage("Earned ৳$totalCoins from quiz challenge!")
            }
        }
    }

    fun onArticleReadComplete(coins: Long = 30) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            userRepository.addRewardCoins(
                userId = user.id,
                coins = coins,
                type = "READ_ARTICLE",
                titleEn = "Educational Article Read Task",
                titleBn = "শিক্ষণীয় আর্টিকেল রিডিং টাস্ক"
            )
            showMessage("Earned ৳$coins for reading!")
        }
    }

    fun onSocialTaskComplete(taskName: String, coins: Long = 200) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            userRepository.addRewardCoins(
                userId = user.id,
                coins = coins,
                type = "SOCIAL_TASK",
                titleEn = "Joined $taskName",
                titleBn = "$taskName এ যুক্ত হওয়ার রিওয়ার্ড"
            )
            showMessage("Bonus ৳$coins credited for $taskName!")
        }
    }

    fun applyReferral(code: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val result = userRepository.applyReferralCode(user.id, code)
            result.onSuccess { bonus ->
                showMessage("Referral code applied! ৳$bonus bonus added.")
            }.onFailure { err ->
                showMessage(err.message ?: "Failed to apply code", isError = true)
            }
        }
    }

    fun submitWithdrawal(
        method: String,
        accountNumber: String,
        coins: Long,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val minBdt = appConfigs.value["min_withdraw_bdt"]?.toLongOrNull() ?: 50L
            val feePercent = appConfigs.value["withdraw_charge_percent"]?.toDoubleOrNull() ?: 5.0

            if (coins < minBdt) {
                showMessage("Minimum withdrawal is ৳$minBdt", isError = true)
                return@launch
            }

            if (user.coins < coins) {
                showMessage("Insufficient balance! You have ৳${user.coins}.", isError = true)
                return@launch
            }

            val result = rewardRepository.requestWithdrawal(
                userId = user.id,
                method = method,
                accountNumber = accountNumber,
                amountBdt = coins,
                feePercent = feePercent
            )

            result.onSuccess {
                showMessage("Withdrawal request submitted for ৳$coins via $method. Fee: $feePercent%")
                onSuccess()
            }.onFailure {
                showMessage(it.message ?: "Withdrawal failed", isError = true)
            }
        }
    }

    fun submitDeposit(
        method: String,
        senderNumber: String,
        trxId: String,
        amountBdt: Double,
        reason: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            if (trxId.isBlank() || senderNumber.isBlank() || amountBdt <= 0) {
                showMessage("Please fill all deposit details correctly", isError = true)
                return@launch
            }
            val result = rewardRepository.submitDepositRequest(
                userId = user.id,
                method = method,
                senderNumber = senderNumber.trim(),
                trxId = trxId.trim(),
                amountBdt = amountBdt,
                reason = reason
            )
            result.onSuccess {
                showMessage("Deposit request submitted successfully!")
                onSuccess()
            }.onFailure {
                showMessage(it.message ?: "Deposit submission failed", isError = true)
            }
        }
    }

    fun updateProfile(
        name: String,
        phone: String,
        newPassword: String?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            if (name.isBlank()) {
                showMessage("Name cannot be empty", isError = true)
                return@launch
            }
            val res = userRepository.updateProfile(user.id, name, phone, newPassword)
            res.onSuccess {
                showMessage("Profile updated successfully!")
                onSuccess()
            }.onFailure {
                showMessage(it.message ?: "Failed to update profile", isError = true)
            }
        }
    }

    fun adminUpdateSettings(settings: Map<String, String>) {
        viewModelScope.launch {
            settings.forEach { (key, value) ->
                configRepository.setConfig(key, value)
            }
            showMessage("System settings saved successfully!")
        }
    }

    fun adminActivateAccount(userId: Long) {
        viewModelScope.launch {
            userRepository.activateAccount(userId, "Admin")
            showMessage("Account activated successfully!")
        }
    }

    // Announcements / In-app Notifications
    fun markAnnouncementRead(announcementId: Long) {
        viewModelScope.launch {
            val uid = currentUserId.value ?: return@launch
            announcementRepository.markAsRead(uid, announcementId)
        }
    }

    fun markAllAnnouncementsRead() {
        viewModelScope.launch {
            val uid = currentUserId.value ?: return@launch
            visibleAnnouncements.value.forEach { ann ->
                announcementRepository.markAsRead(uid, ann.id)
            }
            showMessage("All notifications marked as read")
        }
    }

    // Admin Functions
    fun unlockAdmin(pin: String): Boolean {
        val realPin = appConfigs.value["admin_pin"] ?: "1234"
        val matched = (pin.trim() == realPin.trim())
        _adminUnlocked.value = matched
        if (!matched) {
            showMessage("Invalid Admin PIN! Default is 1234", isError = true)
        }
        return matched
    }

    fun adminChangePin(currentPin: String, newPin: String, confirmPin: String): Boolean {
        val realPin = appConfigs.value["admin_pin"] ?: "1234"
        if (currentPin.trim() != realPin.trim()) {
            showMessage("Current PIN is incorrect!", isError = true)
            return false
        }
        if (newPin.trim().length < 4) {
            showMessage("New PIN must be at least 4 digits!", isError = true)
            return false
        }
        if (newPin.trim() != confirmPin.trim()) {
            showMessage("New PIN and Confirm PIN do not match!", isError = true)
            return false
        }
        viewModelScope.launch {
            configRepository.setConfig("admin_pin", newPin.trim(), "Admin Panel Access PIN")
            showMessage("Admin Password/PIN changed successfully!")
        }
        return true
    }

    fun adminSaveGroup(group: TaskGroupEntity) {
        viewModelScope.launch {
            productRepository.saveGroup(group)
            showMessage("Task Group '${group.nameEn}' saved successfully")
        }
    }

    fun adminDeleteGroup(groupId: Long) {
        viewModelScope.launch {
            productRepository.deleteGroup(groupId)
            showMessage("Task Group deleted")
        }
    }

    fun adminSaveProduct(product: ProductTaskEntity) {
        viewModelScope.launch {
            productRepository.saveProduct(product)
            showMessage("Product / Task '${product.titleEn}' saved successfully")
        }
    }

    fun adminDeleteProduct(productId: Long) {
        viewModelScope.launch {
            productRepository.deleteProduct(productId)
            showMessage("Product / Task deleted")
        }
    }

    fun adminProcessSubmission(
        submissionId: Long,
        status: String,
        applicableRate: Double,
        finalAmount: Double,
        remarks: String
    ) {
        viewModelScope.launch {
            val pointsPerBdt = appConfigs.value["points_per_bdt"]?.toDoubleOrNull() ?: 100.0
            val result = productRepository.processSubmission(
                submissionId = submissionId,
                status = status,
                applicableRate = applicableRate,
                finalAmount = finalAmount,
                remarks = remarks,
                adminName = "Admin",
                pointsPerBdt = pointsPerBdt
            )
            result.onSuccess {
                showMessage("Submission #$submissionId processed as $status!")
            }.onFailure {
                showMessage("Error: ${it.message}", isError = true)
            }
        }
    }

    fun adminSetMembership(userId: Long, tier: String) {
        viewModelScope.launch {
            userRepository.adminSetMembership(userId, tier, "Admin")
            showMessage("User membership set to $tier")
        }
    }

    fun adminAdjustBalance(userId: Long, newCoins: Long, reason: String) {
        viewModelScope.launch {
            userRepository.adminAdjustBalance(userId, newCoins, reason, "Admin")
            showMessage("User balance updated to ৳$newCoins")
        }
    }

    fun adminSaveAnnouncement(announcement: AnnouncementEntity) {
        viewModelScope.launch {
            announcementRepository.saveAnnouncement(announcement)
            showMessage("Announcement saved")
        }
    }

    fun adminDeleteAnnouncement(id: Long) {
        viewModelScope.launch {
            announcementRepository.deleteAnnouncement(id)
            showMessage("Announcement deleted")
        }
    }

    fun setFeatureToggle(key: String, isEnabled: Boolean) {
        viewModelScope.launch {
            configRepository.setFeatureToggle(key, isEnabled)
            showMessage("Feature '$key' updated to ${if (isEnabled) "ON" else "OFF"}")
        }
    }

    fun setAppConfig(key: String, value: String, description: String = "") {
        viewModelScope.launch {
            configRepository.setConfig(key, value.trim(), description)
            showMessage("Configuration updated successfully!")
        }
    }

    fun saveMultipleConfigs(configsToSave: Map<String, String>) {
        viewModelScope.launch {
            configsToSave.forEach { (k, v) ->
                configRepository.setConfig(k, v.trim(), "")
            }
            showMessage("All configurations saved successfully!")
        }
    }

    fun updateAdminSocialConfigs(tgChannel: String, tgGroup: String, fbPage: String) {
        viewModelScope.launch {
            configRepository.updateSocialLinks(tgChannel.trim(), tgGroup.trim(), fbPage.trim())
            showMessage("Social links updated successfully!")
        }
    }

    fun updateAdminRatesAndNotices(pointsPerBdt: String, minCoins: String, noticeEn: String, noticeBn: String) {
        viewModelScope.launch {
            configRepository.updateRates(pointsPerBdt.trim(), minCoins.trim(), noticeEn.trim(), noticeBn.trim())
            showMessage("Rates and announcements updated successfully!")
        }
    }

    fun updateWithdrawalStatus(withdrawalId: Long, status: String, remarks: String) {
        viewModelScope.launch {
            val result = rewardRepository.updateWithdrawalStatus(withdrawalId, status, remarks)
            result.onSuccess {
                showMessage("Withdrawal status updated to $status")
            }.onFailure {
                showMessage("Error: ${it.message}", isError = true)
            }
        }
    }

    // Auth functions
    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = userRepository.login(email, pass)
            result.onSuccess { user ->
                preferencesManager.setLoggedInUserId(user.id)
                refreshTaskStates()
                showMessage("Welcome back, ${user.name}!")
                onSuccess()
            }.onFailure {
                showMessage(it.message ?: "Login failed", isError = true)
            }
        }
    }

    fun register(name: String, email: String, phone: String, pass: String, refCode: String?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (!isFeatureEnabled("feature_registration_enabled", true)) {
                showMessage("Registration is temporarily closed by Admin", isError = true)
                return@launch
            }

            val result = userRepository.register(name, email, phone, pass, refCode)
            result.onSuccess { user ->
                preferencesManager.setLoggedInUserId(user.id)
                refreshTaskStates()
                showMessage("Account created! ৳100 welcome bonus added.")
                onSuccess()
            }.onFailure {
                showMessage(it.message ?: "Registration failed", isError = true)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            preferencesManager.setLoggedInUserId(1L)
            showMessage("Logged out successfully")
        }
    }
}
