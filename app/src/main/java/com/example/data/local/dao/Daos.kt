package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserById(userId: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserByIdSync(userId: Long): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE referralCode = :code LIMIT 1")
    suspend fun getUserByReferralCode(code: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT COUNT(*) FROM users")
    fun getUserCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET coins = coins + :coinsToAdd, totalEarned = totalEarned + :coinsToAdd WHERE id = :userId")
    suspend fun addCoins(userId: Long, coinsToAdd: Long)

    @Query("UPDATE users SET coins = coins - :coinsToDeduct, totalWithdrawn = totalWithdrawn + :coinsToDeduct WHERE id = :userId AND coins >= :coinsToDeduct")
    suspend fun deductCoins(userId: Long, coinsToDeduct: Long): Int

    @Query("UPDATE users SET coins = :newCoins WHERE id = :userId")
    suspend fun setCoins(userId: Long, newCoins: Long)

    @Query("UPDATE users SET membershipTier = :tier WHERE id = :userId")
    suspend fun updateMembershipTier(userId: Long, tier: String)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getTransactionsForUser(userId: Long): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT 50")
    fun getAllRecentTransactions(): Flow<List<TransactionEntity>>
}

@Dao
interface WithdrawalDao {
    @Query("SELECT * FROM withdrawals WHERE userId = :userId ORDER BY requestedAt DESC")
    fun getWithdrawalsForUser(userId: Long): Flow<List<WithdrawalEntity>>

    @Query("SELECT * FROM withdrawals ORDER BY requestedAt DESC")
    fun getAllWithdrawals(): Flow<List<WithdrawalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWithdrawal(withdrawal: WithdrawalEntity): Long

    @Query("UPDATE withdrawals SET status = :status, remarks = :remarks, processedAt = :processedAt WHERE id = :id")
    suspend fun updateWithdrawalStatus(id: Long, status: String, remarks: String, processedAt: Long)

    @Query("SELECT * FROM withdrawals WHERE id = :id LIMIT 1")
    suspend fun getWithdrawalById(id: Long): WithdrawalEntity?
}

@Dao
interface AppConfigDao {
    @Query("SELECT * FROM app_config")
    fun getAllConfigs(): Flow<List<AppConfigEntity>>

    @Query("SELECT configValue FROM app_config WHERE configKey = :key LIMIT 1")
    suspend fun getConfigValue(key: String): String?

    @Query("SELECT configValue FROM app_config WHERE configKey = :key LIMIT 1")
    fun observeConfigValue(key: String): Flow<String?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setConfig(config: AppConfigEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setConfigs(configs: List<AppConfigEntity>)
}

@Dao
interface DailyTaskDao {
    @Query("SELECT * FROM daily_tasks_state WHERE taskKey = :key LIMIT 1")
    suspend fun getTaskState(key: String): DailyTaskStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTaskState(state: DailyTaskStateEntity)

    @Query("SELECT * FROM daily_tasks_state WHERE userId = :userId AND dateString = :dateString")
    fun getDailyTasksForUser(userId: Long, dateString: String): Flow<List<DailyTaskStateEntity>>
}

@Dao
interface TaskGroupDao {
    @Query("SELECT * FROM task_groups ORDER BY displayOrder ASC, id ASC")
    fun getAllTaskGroups(): Flow<List<TaskGroupEntity>>

    @Query("SELECT * FROM task_groups WHERE isEnabled = 1 ORDER BY displayOrder ASC, id ASC")
    fun getEnabledTaskGroups(): Flow<List<TaskGroupEntity>>

    @Query("SELECT * FROM task_groups WHERE id = :groupId LIMIT 1")
    suspend fun getGroupById(groupId: Long): TaskGroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: TaskGroupEntity): Long

    @Update
    suspend fun updateGroup(group: TaskGroupEntity)

    @Query("DELETE FROM task_groups WHERE id = :groupId")
    suspend fun deleteGroup(groupId: Long)
}

@Dao
interface ProductTaskDao {
    @Query("SELECT * FROM products_tasks ORDER BY displayOrder ASC, id ASC")
    fun getAllProducts(): Flow<List<ProductTaskEntity>>

    @Query("SELECT * FROM products_tasks WHERE groupId = :groupId ORDER BY displayOrder ASC, id ASC")
    fun getProductsByGroup(groupId: Long): Flow<List<ProductTaskEntity>>

    @Query("SELECT * FROM products_tasks WHERE groupId = :groupId AND isEnabled = 1 ORDER BY displayOrder ASC, id ASC")
    fun getEnabledProductsByGroup(groupId: Long): Flow<List<ProductTaskEntity>>

    @Query("SELECT * FROM products_tasks WHERE id = :productId LIMIT 1")
    suspend fun getProductById(productId: Long): ProductTaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductTaskEntity): Long

    @Update
    suspend fun updateProduct(product: ProductTaskEntity)

    @Query("DELETE FROM products_tasks WHERE id = :productId")
    suspend fun deleteProduct(productId: Long)
}

@Dao
interface TaskSubmissionDao {
    @Query("SELECT * FROM task_submissions WHERE userId = :userId ORDER BY submittedAt DESC")
    fun getSubmissionsByUser(userId: Long): Flow<List<TaskSubmissionEntity>>

    @Query("SELECT * FROM task_submissions ORDER BY submittedAt DESC")
    fun getAllSubmissions(): Flow<List<TaskSubmissionEntity>>

    @Query("SELECT * FROM task_submissions WHERE id = :id LIMIT 1")
    suspend fun getSubmissionById(id: Long): TaskSubmissionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(submission: TaskSubmissionEntity): Long

    @Query("UPDATE task_submissions SET status = :status, applicableRate = :rate, finalAmount = :finalAmount, adminRemarks = :remarks, processedAt = :processedAt, processedBy = :processedBy WHERE id = :id")
    suspend fun processSubmission(
        id: Long,
        status: String,
        rate: Double,
        finalAmount: Double,
        remarks: String,
        processedAt: Long,
        processedBy: String
    )
}

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcements ORDER BY createdAt DESC")
    fun getAllAnnouncements(): Flow<List<AnnouncementEntity>>

    @Query("SELECT * FROM announcements WHERE isEnabled = 1 ORDER BY createdAt DESC")
    fun getActiveAnnouncements(): Flow<List<AnnouncementEntity>>

    @Query("SELECT * FROM announcements WHERE id = :id LIMIT 1")
    suspend fun getAnnouncementById(id: Long): AnnouncementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: AnnouncementEntity): Long

    @Update
    suspend fun updateAnnouncement(announcement: AnnouncementEntity)

    @Query("DELETE FROM announcements WHERE id = :id")
    suspend fun deleteAnnouncement(id: Long)
}

@Dao
interface NotificationReadDao {
    @Query("SELECT announcementId FROM notification_reads WHERE userId = :userId")
    fun getReadAnnouncementIds(userId: Long): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markAsRead(readEntity: NotificationReadEntity)

    @Query("SELECT COUNT(*) FROM notification_reads WHERE userId = :userId AND announcementId = :announcementId")
    suspend fun isRead(userId: Long, announcementId: Long): Int
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity): Long
}
