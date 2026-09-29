package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val email: String,
    val phone: String = "",
    val passwordHash: String,
    val referralCode: String,
    val referredBy: String? = null,
    val coins: Long = 100, // Welcome bonus of 100 coins
    val totalEarned: Long = 100,
    val totalWithdrawn: Long = 0,
    val isAdmin: Boolean = false,
    val membershipTier: String = "FREE", // "FREE" or "PREMIUM"
    val isActivated: Boolean = true, // Account activation status
    val premiumExpiresAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val type: String, // "CHECK_IN", "SPIN", "SCRATCH", "QUIZ", "READ_TASK", "REFERRAL_BONUS", "SOCIAL_JOIN", "TASK_SUBMISSION", "UPGRADE_PREMIUM", "WITHDRAWAL", "REFUND", "ADMIN_ADJUSTMENT"
    val coins: Long, // Positive for earnings, negative for deductions
    val titleEn: String,
    val titleBn: String,
    val status: String = "COMPLETED", // "COMPLETED", "PENDING", "REJECTED"
    val method: String? = null, // e.g. "bKash", "Nagad"
    val accountNumber: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "withdrawals")
data class WithdrawalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val userName: String,
    val userPhone: String,
    val method: String, // "bKash", "Nagad", "Rocket", "Recharge", "USDT"
    val accountNumber: String,
    val coins: Long,
    val amountCurrency: Double, // in BDT or USD
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED", "PAID"
    val remarks: String = "",
    val requestedAt: Long = System.currentTimeMillis(),
    val processedAt: Long? = null
)

@Entity(tableName = "app_config")
data class AppConfigEntity(
    @PrimaryKey
    val configKey: String,
    val configValue: String,
    val description: String = ""
)

@Entity(tableName = "daily_tasks_state")
data class DailyTaskStateEntity(
    @PrimaryKey
    val taskKey: String, // e.g. "userId_CHECK_IN_2026-09-26"
    val userId: Long,
    val taskType: String,
    val dateString: String, // "yyyy-MM-dd"
    val countCompleted: Int = 0,
    val maxAllowed: Int = 1,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "task_groups")
data class TaskGroupEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nameEn: String,
    val nameBn: String,
    val descriptionEn: String = "",
    val descriptionBn: String = "",
    val iconName: String = "Work", // "Work", "Sell", "Task", "Share", "Star"
    val displayOrder: Int = 0,
    val isEnabled: Boolean = true,
    val accessRule: String = "BOTH" // "FREE", "PREMIUM", "BOTH", "DISABLED"
)

@Entity(tableName = "products_tasks")
data class ProductTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val groupId: Long,
    val categoryName: String = "General",
    val titleEn: String,
    val titleBn: String,
    val descriptionEn: String = "",
    val descriptionBn: String = "",
    val rateAmount: Double = 10.0,
    val rateType: String = "BDT", // "BDT" or "COINS"
    val accessRule: String = "BOTH", // "FREE", "PREMIUM", "BOTH", "DISABLED"
    val displayOrder: Int = 0,
    val isEnabled: Boolean = true,
    val maintenanceNotice: String = "",
    val maintenanceNoticeBn: String = "",
    val upcomingNotice: String = "",
    val scheduleStart: Long = 0L,
    val scheduleEnd: Long = 0L,
    val fieldsConfigJson: String = "[]" // JSON representation of List<CustomFieldConfig>
)

data class CustomFieldConfig(
    val fieldId: String,
    val label: String,
    val instruction: String = "",
    val placeholder: String = "",
    val isRequired: Boolean = true,
    val order: Int = 0
)

@Entity(tableName = "task_submissions")
data class TaskSubmissionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val userName: String,
    val membershipType: String = "FREE",
    val groupId: Long,
    val groupName: String,
    val productId: Long,
    val productTitle: String,
    val submittedValuesJson: String, // JSON map of fieldId to value
    val submittedAt: Long = System.currentTimeMillis(),
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED", "PAID"
    val applicableRate: Double, // Admin can adjust prior to final approval
    val finalAmount: Double,
    val adminRemarks: String = "",
    val processedAt: Long? = null,
    val processedBy: String? = null
)

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val titleEn: String,
    val titleBn: String,
    val bodyEn: String,
    val bodyBn: String,
    val targetAudience: String = "ALL", // "ALL", "FREE", "PREMIUM"
    val isImportant: Boolean = false,
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notification_reads")
data class NotificationReadEntity(
    @PrimaryKey
    val id: String, // "${userId}_${announcementId}"
    val userId: Long,
    val announcementId: Long,
    val readAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val action: String,
    val details: String,
    val performedBy: String = "Admin",
    val timestamp: Long = System.currentTimeMillis()
)
