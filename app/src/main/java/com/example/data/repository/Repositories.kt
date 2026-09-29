package com.example.data.repository

import com.example.data.local.dao.AnnouncementDao
import com.example.data.local.dao.AppConfigDao
import com.example.data.local.dao.AuditLogDao
import com.example.data.local.dao.DailyTaskDao
import com.example.data.local.dao.NotificationReadDao
import com.example.data.local.dao.ProductTaskDao
import com.example.data.local.dao.TaskGroupDao
import com.example.data.local.dao.TaskSubmissionDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.dao.UserDao
import com.example.data.local.dao.WithdrawalDao
import com.example.data.local.entity.AnnouncementEntity
import com.example.data.local.entity.AppConfigEntity
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.DailyTaskStateEntity
import com.example.data.local.entity.NotificationReadEntity
import com.example.data.local.entity.ProductTaskEntity
import com.example.data.local.entity.TaskGroupEntity
import com.example.data.local.entity.TaskSubmissionEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.WithdrawalEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class UserRepository(
    private val userDao: UserDao,
    private val transactionDao: TransactionDao,
    private val configDao: AppConfigDao,
    private val auditLogDao: AuditLogDao
) {
    fun getUserById(userId: Long): Flow<UserEntity?> = userDao.getUserById(userId)

    suspend fun getUserByIdSync(userId: Long): UserEntity? = userDao.getUserByIdSync(userId)

    fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()

    fun getUserCount(): Flow<Int> = userDao.getUserCount()

    suspend fun login(email: String, passwordHash: String): Result<UserEntity> {
        val user = userDao.getUserByEmail(email.trim().lowercase())
        return if (user != null && user.passwordHash == passwordHash) {
            Result.success(user)
        } else {
            Result.failure(Exception("Invalid email or password"))
        }
    }

    suspend fun register(
        name: String,
        email: String,
        phone: String,
        passwordHash: String,
        referredByCode: String? = null
    ): Result<UserEntity> {
        val normalizedEmail = email.trim().lowercase()
        val existing = userDao.getUserByEmail(normalizedEmail)
        if (existing != null) {
            return Result.failure(Exception("User already exists with this email"))
        }

        val referralCode = "IZ" + UUID.randomUUID().toString().take(4).uppercase()
        val welcomeCoins = 100L

        if (referredByCode.isNullOrBlank()) {
            return Result.failure(Exception("Registration requires a valid referral code or referral link"))
        }

        val cleanRef = referredByCode.trim().uppercase()
        val referrerUser = userDao.getUserByReferralCode(cleanRef)
            ?: return Result.failure(Exception("Invalid referral code. A valid referral code is required to register."))

        val initialCoins = welcomeCoins + 50L

        val newUser = UserEntity(
            name = name.trim(),
            email = normalizedEmail,
            phone = phone.trim(),
            passwordHash = passwordHash,
            referralCode = referralCode,
            referredBy = referrerUser?.referralCode,
            coins = initialCoins,
            totalEarned = initialCoins,
            isAdmin = false,
            membershipTier = "FREE",
            isActivated = false
        )

        val newId = userDao.insertUser(newUser)
        val createdUser = newUser.copy(id = newId)

        // Log welcome transaction
        transactionDao.insertTransaction(
            TransactionEntity(
                userId = newId,
                type = "WELCOME_BONUS",
                coins = welcomeCoins,
                titleEn = "Welcome Bonus (৳$welcomeCoins)",
                titleBn = "ওয়েলকাম বোনাস (৳$welcomeCoins)"
            )
        )

        if (referrerUser != null) {
            val refBonus = 50L
            userDao.addCoins(referrerUser.id, refBonus)
            transactionDao.insertTransaction(
                TransactionEntity(
                    userId = referrerUser.id,
                    type = "REFERRAL_REWARD",
                    coins = refBonus,
                    titleEn = "Referral Bonus for inviting ${createdUser.name}",
                    titleBn = "${createdUser.name} কে রেফার করায় বোনাস"
                )
            )

            transactionDao.insertTransaction(
                TransactionEntity(
                    userId = newId,
                    type = "REFERRAL_CLAIM",
                    coins = refBonus,
                    titleEn = "Referral Code Bonus",
                    titleBn = "রেফারেল কোড বোনাস"
                )
            )
        }

        return Result.success(createdUser)
    }

    suspend fun updateProfile(
        userId: Long,
        newName: String,
        newPhone: String,
        newPassword: String?
    ): Result<UserEntity> {
        val user = userDao.getUserByIdSync(userId) ?: return Result.failure(Exception("User not found"))
        val finalPassword = if (!newPassword.isNullOrBlank()) newPassword.trim() else user.passwordHash
        userDao.updateProfile(userId, newName.trim(), newPhone.trim(), finalPassword)
        val updated = user.copy(name = newName.trim(), phone = newPhone.trim(), passwordHash = finalPassword)
        auditLogDao.insertAuditLog(
            AuditLogEntity(
                action = "PROFILE_UPDATE",
                details = "User ID $userId updated profile: name=$newName, phone=$newPhone",
                performedBy = "UserSelf"
            )
        )
        return Result.success(updated)
    }

    suspend fun activateAccount(userId: Long, adminName: String = "System"): Result<Unit> {
        val user = userDao.getUserByIdSync(userId) ?: return Result.failure(Exception("User not found"))
        userDao.updateActivationStatus(userId, true)
        auditLogDao.insertAuditLog(
            AuditLogEntity(
                action = "ACCOUNT_ACTIVATED",
                details = "User ${user.name} (ID $userId) account was activated",
                performedBy = adminName
            )
        )
        return Result.success(Unit)
    }

    suspend fun applyReferralCode(userId: Long, code: String): Result<Long> {
        val user = userDao.getUserByIdSync(userId) ?: return Result.failure(Exception("User not found"))
        if (!user.referredBy.isNullOrBlank()) {
            return Result.failure(Exception("Referral code already applied"))
        }

        val cleanCode = code.trim().uppercase()
        if (cleanCode == user.referralCode) {
            return Result.failure(Exception("Cannot apply your own referral code"))
        }

        val referrer = userDao.getUserByReferralCode(cleanCode)
            ?: return Result.failure(Exception("Invalid referral code"))

        val bonus = 500L
        userDao.addCoins(userId, bonus)
        userDao.updateUser(user.copy(referredBy = referrer.referralCode))
        transactionDao.insertTransaction(
            TransactionEntity(
                userId = userId,
                type = "REFERRAL_CLAIM",
                coins = bonus,
                titleEn = "Referral Bonus Applied",
                titleBn = "রেফারেল বোনাস যোগ হয়েছে"
            )
        )

        userDao.addCoins(referrer.id, bonus)
        transactionDao.insertTransaction(
            TransactionEntity(
                userId = referrer.id,
                type = "REFERRAL_REWARD",
                coins = bonus,
                titleEn = "Referral Reward from ${user.name}",
                titleBn = "${user.name} এর রেফারেল রিওয়ার্ড"
            )
        )

        return Result.success(bonus)
    }

    suspend fun addRewardCoins(
        userId: Long,
        coins: Long,
        type: String,
        titleEn: String,
        titleBn: String
    ): Result<Long> {
        userDao.addCoins(userId, coins)
        transactionDao.insertTransaction(
            TransactionEntity(
                userId = userId,
                type = type,
                coins = coins,
                titleEn = titleEn,
                titleBn = titleBn
            )
        )
        return Result.success(coins)
    }

    suspend fun upgradeToPremium(userId: Long, costCoins: Long): Result<Unit> {
        val user = userDao.getUserByIdSync(userId) ?: return Result.failure(Exception("User not found"))
        if (user.membershipTier == "PREMIUM") {
            return Result.failure(Exception("Already a Premium Member"))
        }

        if (user.coins < costCoins) {
            return Result.failure(Exception("Insufficient balance! Required: ৳$costCoins"))
        }

        val rows = userDao.deductCoins(userId, costCoins)
        if (rows == 0) return Result.failure(Exception("Balance deduction failed"))

        userDao.updateMembershipTier(userId, "PREMIUM")

        transactionDao.insertTransaction(
            TransactionEntity(
                userId = userId,
                type = "UPGRADE_PREMIUM",
                coins = -costCoins,
                titleEn = "Upgraded to Premium Membership",
                titleBn = "প্রিমিয়াম মেম্বারশিপে আপগ্রেড করা হয়েছে"
            )
        )

        auditLogDao.insertAuditLog(
            AuditLogEntity(
                action = "MEMBERSHIP_UPGRADE",
                details = "User ${user.name} (ID $userId) upgraded to PREMIUM for ৳$costCoins",
                performedBy = "UserSelf"
            )
        )

        return Result.success(Unit)
    }

    suspend fun adminSetMembership(userId: Long, tier: String, adminName: String): Result<Unit> {
        userDao.updateMembershipTier(userId, tier)
        auditLogDao.insertAuditLog(
            AuditLogEntity(
                action = "ADMIN_SET_MEMBERSHIP",
                details = "Admin $adminName set user $userId tier to $tier",
                performedBy = adminName
            )
        )
        return Result.success(Unit)
    }

    suspend fun adminAdjustBalance(userId: Long, newCoins: Long, reason: String, adminName: String): Result<Unit> {
        val user = userDao.getUserByIdSync(userId) ?: return Result.failure(Exception("User not found"))
        val diff = newCoins - user.coins
        userDao.setCoins(userId, newCoins)

        transactionDao.insertTransaction(
            TransactionEntity(
                userId = userId,
                type = "ADMIN_ADJUSTMENT",
                coins = diff,
                titleEn = "Admin Adjustment: $reason",
                titleBn = "এডমিন এডজাস্টমেন্ট: $reason"
            )
        )

        auditLogDao.insertAuditLog(
            AuditLogEntity(
                action = "ADMIN_ADJUST_BALANCE",
                details = "Admin $adminName adjusted user $userId balance from ৳${user.coins} to ৳$newCoins. Reason: $reason",
                performedBy = adminName
            )
        )
        return Result.success(Unit)
    }

    suspend fun adminSetUserActivation(userId: Long, isActivated: Boolean, adminName: String): Result<Unit> {
        userDao.updateActivationStatus(userId, isActivated)
        auditLogDao.insertAuditLog(
            AuditLogEntity(
                action = "ADMIN_SET_ACTIVATION",
                details = "Admin $adminName set user $userId activation to ${if (isActivated) "ACTIVE" else "INACTIVE"}",
                performedBy = adminName
            )
        )
        return Result.success(Unit)
    }
}

class RewardRepository(
    private val userDao: UserDao,
    private val transactionDao: TransactionDao,
    private val withdrawalDao: WithdrawalDao,
    private val dailyTaskDao: DailyTaskDao
) {
    fun getUserTransactions(userId: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsForUser(userId)

    fun getUserWithdrawals(userId: Long): Flow<List<WithdrawalEntity>> =
        withdrawalDao.getWithdrawalsForUser(userId)

    fun getAllWithdrawals(): Flow<List<WithdrawalEntity>> =
        withdrawalDao.getAllWithdrawals()

    suspend fun requestWithdrawal(
        userId: Long,
        method: String,
        accountNumber: String,
        amountBdt: Long,
        feePercent: Double = 5.0
    ): Result<Long> {
        val user = userDao.getUserByIdSync(userId) ?: return Result.failure(Exception("User not found"))
        if (user.coins < amountBdt) {
            return Result.failure(Exception("Insufficient balance"))
        }

        val feeAmount = (amountBdt * feePercent / 100.0)
        val netPayout = amountBdt - feeAmount

        val rowsUpdated = userDao.deductCoins(userId, amountBdt)
        if (rowsUpdated == 0) {
            return Result.failure(Exception("Withdrawal failed: insufficient balance"))
        }

        val withdrawalId = withdrawalDao.insertWithdrawal(
            WithdrawalEntity(
                userId = userId,
                userName = user.name,
                userPhone = user.phone.ifEmpty { accountNumber },
                method = method,
                accountNumber = accountNumber,
                coins = amountBdt,
                amountCurrency = netPayout,
                status = "PENDING",
                remarks = "Fee: ৳${String.format(Locale.US, "%.2f", feeAmount)} ($feePercent%), Net: ৳${String.format(Locale.US, "%.2f", netPayout)}"
            )
        )

        transactionDao.insertTransaction(
            TransactionEntity(
                userId = userId,
                type = "WITHDRAWAL",
                coins = -amountBdt,
                titleEn = "Withdrawal ৳$amountBdt to $method ($accountNumber) [Net: ৳${String.format(Locale.US, "%.2f", netPayout)}]",
                titleBn = "$method ($accountNumber) এ ৳$amountBdt উইথড্র [প্রাপ্ত: ৳${String.format(Locale.US, "%.2f", netPayout)}]",
                status = "PENDING",
                method = method,
                accountNumber = accountNumber
            )
        )

        return Result.success(withdrawalId)
    }

    suspend fun submitDepositRequest(
        userId: Long,
        method: String,
        senderNumber: String,
        trxId: String,
        amountBdt: Double,
        reason: String
    ): Result<Long> {
        val user = userDao.getUserByIdSync(userId) ?: return Result.failure(Exception("User not found"))
        val cleanReason = when (reason) {
            "ACCOUNT_ACTIVATION" -> "Account Activation"
            "PRO_MEMBERSHIP" -> "Pro Upgrade"
            else -> "General Deposit"
        }

        // Record pending deposit transaction
        val txId = transactionDao.insertTransaction(
            TransactionEntity(
                userId = userId,
                type = "DEPOSIT",
                coins = amountBdt.toLong(),
                titleEn = "Deposit (৳$amountBdt) for $cleanReason via $method - TrxID: $trxId",
                titleBn = "$method দিয়ে $cleanReason ডিপোজিট (৳$amountBdt) - TrxID: $trxId",
                status = "COMPLETED",
                method = method,
                accountNumber = senderNumber
            )
        )

        // Process purpose
        if (reason == "ACCOUNT_ACTIVATION") {
            userDao.updateActivationStatus(userId, true)
        } else if (reason == "PRO_MEMBERSHIP") {
            userDao.updateMembershipTier(userId, "PREMIUM")
        }
        userDao.addCoins(userId, amountBdt.toLong())

        return Result.success(txId)
    }

    suspend fun cleanOldApprovedSubmissions(submissionDao: TaskSubmissionDao): Int {
        val twentyFourHoursAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000L)
        return submissionDao.deleteApprovedSubmissionsOlderThan(twentyFourHoursAgo)
    }

    suspend fun updateWithdrawalStatus(
        withdrawalId: Long,
        status: String,
        remarks: String
    ): Result<Unit> {
        val withdrawal = withdrawalDao.getWithdrawalById(withdrawalId)
            ?: return Result.failure(Exception("Withdrawal request not found"))

        withdrawalDao.updateWithdrawalStatus(
            id = withdrawalId,
            status = status,
            remarks = remarks,
            processedAt = System.currentTimeMillis()
        )

        if (status == "REJECTED") {
            userDao.addCoins(withdrawal.userId, withdrawal.coins)
            transactionDao.insertTransaction(
                TransactionEntity(
                    userId = withdrawal.userId,
                    type = "REFUND",
                    coins = withdrawal.coins,
                    titleEn = "Refund for rejected $status withdrawal",
                    titleBn = "বাতিলকৃত উইথড্রর কয়েন রিফান্ড"
                )
            )
        }

        return Result.success(Unit)
    }

    suspend fun getTaskState(userId: Long, taskType: String, maxPerDay: Int): DailyTaskStateEntity {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val key = "${userId}_${taskType}_$todayStr"
        val existing = dailyTaskDao.getTaskState(key)
        return existing ?: DailyTaskStateEntity(
            taskKey = key,
            userId = userId,
            taskType = taskType,
            dateString = todayStr,
            countCompleted = 0,
            maxAllowed = maxPerDay
        )
    }

    suspend fun incrementTaskCompleted(state: DailyTaskStateEntity): DailyTaskStateEntity {
        val updated = state.copy(
            countCompleted = state.countCompleted + 1,
            lastUpdated = System.currentTimeMillis()
        )
        dailyTaskDao.insertOrUpdateTaskState(updated)
        return updated
    }
}

class ProductRepository(
    private val groupDao: TaskGroupDao,
    private val productDao: ProductTaskDao,
    private val submissionDao: TaskSubmissionDao,
    private val userDao: UserDao,
    private val transactionDao: TransactionDao,
    private val auditLogDao: AuditLogDao
) {
    fun getAllGroups(): Flow<List<TaskGroupEntity>> = groupDao.getAllTaskGroups()

    fun getEnabledGroups(): Flow<List<TaskGroupEntity>> = groupDao.getEnabledTaskGroups()

    suspend fun getGroupById(id: Long): TaskGroupEntity? = groupDao.getGroupById(id)

    suspend fun saveGroup(group: TaskGroupEntity): Long {
        return if (group.id == 0L) {
            groupDao.insertGroup(group)
        } else {
            groupDao.updateGroup(group)
            group.id
        }
    }

    suspend fun deleteGroup(id: Long) = groupDao.deleteGroup(id)

    fun getAllProducts(): Flow<List<ProductTaskEntity>> = productDao.getAllProducts()

    fun getProductsByGroup(groupId: Long): Flow<List<ProductTaskEntity>> =
        productDao.getProductsByGroup(groupId)

    fun getEnabledProductsByGroup(groupId: Long): Flow<List<ProductTaskEntity>> =
        productDao.getEnabledProductsByGroup(groupId)

    suspend fun getProductById(id: Long): ProductTaskEntity? = productDao.getProductById(id)

    suspend fun saveProduct(product: ProductTaskEntity): Long {
        return if (product.id == 0L) {
            productDao.insertProduct(product)
        } else {
            productDao.updateProduct(product)
            product.id
        }
    }

    suspend fun deleteProduct(id: Long) = productDao.deleteProduct(id)

    // Submissions
    fun getSubmissionsByUser(userId: Long): Flow<List<TaskSubmissionEntity>> =
        submissionDao.getSubmissionsByUser(userId)

    fun getAllSubmissions(): Flow<List<TaskSubmissionEntity>> =
        submissionDao.getAllSubmissions()

    suspend fun getTodayProTaskCount(userId: Long): Int {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis
        return submissionDao.getTodayProSubmissionCount(userId, startOfDay)
    }

    suspend fun cleanOldApprovedSubmissions(): Int {
        val twentyFourHoursAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000L)
        return submissionDao.deleteApprovedSubmissionsOlderThan(twentyFourHoursAgo)
    }

    suspend fun submitTask(
        userId: Long,
        userName: String,
        membershipType: String,
        groupId: Long,
        groupName: String,
        productId: Long,
        productTitle: String,
        rateAmount: Double,
        submittedValuesJson: String
    ): Result<Long> {
        val id = submissionDao.insertSubmission(
            TaskSubmissionEntity(
                userId = userId,
                userName = userName,
                membershipType = membershipType,
                groupId = groupId,
                groupName = groupName,
                productId = productId,
                productTitle = productTitle,
                submittedValuesJson = submittedValuesJson,
                submittedAt = System.currentTimeMillis(),
                status = "PENDING",
                applicableRate = rateAmount,
                finalAmount = rateAmount
            )
        )
        return Result.success(id)
    }

    suspend fun processSubmission(
        submissionId: Long,
        status: String, // "APPROVED", "REJECTED"
        applicableRate: Double,
        finalAmount: Double,
        remarks: String,
        adminName: String,
        pointsPerBdt: Double
    ): Result<Unit> {
        val submission = submissionDao.getSubmissionById(submissionId)
            ?: return Result.failure(Exception("Submission not found"))

        submissionDao.processSubmission(
            id = submissionId,
            status = status,
            rate = applicableRate,
            finalAmount = finalAmount,
            remarks = remarks,
            processedAt = System.currentTimeMillis(),
            processedBy = adminName
        )

        if (status == "APPROVED") {
            // Credit BDT balance to user based on final approved amount
            val amountToAdd = finalAmount.toLong().coerceAtLeast(1L)
            userDao.addCoins(submission.userId, amountToAdd)

            transactionDao.insertTransaction(
                TransactionEntity(
                    userId = submission.userId,
                    type = "TASK_SUBMISSION",
                    coins = amountToAdd,
                    titleEn = "Task Approved: ${submission.productTitle} (৳$finalAmount)",
                    titleBn = "টাস্ক অনুমোদিত: ${submission.productTitle} (৳$finalAmount)"
                )
            )
        }

        auditLogDao.insertAuditLog(
            AuditLogEntity(
                action = "PROCESS_SUBMISSION",
                details = "Submission #$submissionId (${submission.productTitle}) marked $status by $adminName. Rate: $applicableRate, Final: $finalAmount. Remarks: $remarks",
                performedBy = adminName
            )
        )

        return Result.success(Unit)
    }
}

class AnnouncementRepository(
    private val announcementDao: AnnouncementDao,
    private val readDao: NotificationReadDao
) {
    fun getAllAnnouncements(): Flow<List<AnnouncementEntity>> = announcementDao.getAllAnnouncements()

    fun getActiveAnnouncements(): Flow<List<AnnouncementEntity>> = announcementDao.getActiveAnnouncements()

    fun getReadIds(userId: Long): Flow<List<Long>> = readDao.getReadAnnouncementIds(userId)

    suspend fun markAsRead(userId: Long, announcementId: Long) {
        readDao.markAsRead(
            NotificationReadEntity(
                id = "${userId}_$announcementId",
                userId = userId,
                announcementId = announcementId
            )
        )
    }

    suspend fun saveAnnouncement(announcement: AnnouncementEntity): Long {
        return if (announcement.id == 0L) {
            announcementDao.insertAnnouncement(announcement)
        } else {
            announcementDao.updateAnnouncement(announcement)
            announcement.id
        }
    }

    suspend fun deleteAnnouncement(id: Long) = announcementDao.deleteAnnouncement(id)
}

class ConfigRepository(private val appConfigDao: AppConfigDao) {
    fun getAllConfigs(): Flow<List<AppConfigEntity>> = appConfigDao.getAllConfigs()

    fun observeConfig(key: String): Flow<String?> = appConfigDao.observeConfigValue(key)

    suspend fun getConfigValue(key: String): String? = appConfigDao.getConfigValue(key)

    suspend fun setConfig(key: String, value: String, description: String = "") {
        appConfigDao.setConfig(AppConfigEntity(key, value, description))
    }

    suspend fun updateSocialLinks(tgChannel: String, tgGroup: String, fbPage: String) {
        appConfigDao.setConfig(AppConfigEntity("telegram_channel_url", tgChannel, "Telegram Channel"))
        appConfigDao.setConfig(AppConfigEntity("telegram_group_url", tgGroup, "Telegram Group"))
        appConfigDao.setConfig(AppConfigEntity("facebook_page_url", fbPage, "Facebook Page"))
    }

    suspend fun updateRates(pointsPerBdt: String, minWithdrawCoins: String, noticeEn: String, noticeBn: String) {
        appConfigDao.setConfig(AppConfigEntity("points_per_bdt", pointsPerBdt, "Points per BDT"))
        appConfigDao.setConfig(AppConfigEntity("min_withdraw_bdt", minWithdrawCoins, "Min Withdrawal BDT"))
        appConfigDao.setConfig(AppConfigEntity("notice_text_en", noticeEn, "Dashboard Notice English"))
        appConfigDao.setConfig(AppConfigEntity("notice_text_bn", noticeBn, "Dashboard Notice Bangla"))
    }

    suspend fun setFeatureToggle(featureKey: String, isEnabled: Boolean) {
        appConfigDao.setConfig(AppConfigEntity(featureKey, if (isEnabled) "true" else "false", "Feature Toggle"))
    }
}
