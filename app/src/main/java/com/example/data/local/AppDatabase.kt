package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        TransactionEntity::class,
        WithdrawalEntity::class,
        AppConfigEntity::class,
        DailyTaskStateEntity::class,
        TaskGroupEntity::class,
        ProductTaskEntity::class,
        TaskSubmissionEntity::class,
        AnnouncementEntity::class,
        NotificationReadEntity::class,
        AuditLogEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun withdrawalDao(): WithdrawalDao
    abstract fun appConfigDao(): AppConfigDao
    abstract fun dailyTaskDao(): DailyTaskDao
    abstract fun taskGroupDao(): TaskGroupDao
    abstract fun productTaskDao(): ProductTaskDao
    abstract fun taskSubmissionDao(): TaskSubmissionDao
    abstract fun announcementDao(): AnnouncementDao
    abstract fun notificationReadDao(): NotificationReadDao
    abstract fun auditLogDao(): AuditLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "taka_reward_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDefaultData(database)
                    }
                }
            }
        }

        suspend fun populateDefaultData(database: AppDatabase) {
            val configDao = database.appConfigDao()
            val userDao = database.userDao()
            val groupDao = database.taskGroupDao()
            val productDao = database.productTaskDao()
            val announcementDao = database.announcementDao()

            val defaultConfigList = listOf(
                AppConfigEntity("telegram_channel_url", "https://t.me/takareward_channel", "Official Telegram Channel"),
                AppConfigEntity("telegram_group_url", "https://t.me/takareward_support", "Official Telegram Support Group"),
                AppConfigEntity("facebook_page_url", "https://facebook.com/takareward.official", "Official Facebook Page"),
                AppConfigEntity("admin_pin", "1234", "Admin Panel Access PIN"),
                AppConfigEntity("points_per_bdt", "100", "Coins equivalent to 1 BDT"),
                AppConfigEntity("min_withdraw_coins", "2000", "Minimum coins to withdraw (20 BDT)"),
                AppConfigEntity("referral_bonus_coins", "500", "Coins awarded for valid referral"),
                AppConfigEntity("daily_checkin_base_coins", "50", "Base coins for daily check-in"),
                AppConfigEntity(
                    "notice_text_en",
                    "Welcome to IncomeZoneX! Earn daily cash via Spin, Scratch & Sell Tasks. Withdraw via bKash, Nagad & Rocket instantly!",
                    "Dashboard Announcement (English)"
                ),
                AppConfigEntity(
                    "notice_text_bn",
                    "ইনকামজোনএক্সে স্বাগতম! প্রতিদিন স্পিন, স্ক্র্যাচ ও সেল টাস্ক করে টাকা আয় করুন। বিকাশ ও নগদে দ্রুত উইথড্র নিন!",
                    "Dashboard Announcement (Bangla)"
                ),
                // Feature Toggles
                AppConfigEntity("feature_registration_enabled", "true", "User Registration Switch"),
                AppConfigEntity("feature_free_membership_enabled", "true", "Free Membership Access"),
                AppConfigEntity("feature_premium_membership_enabled", "true", "Premium Membership Upgrades"),
                AppConfigEntity("feature_spin_enabled", "true", "Spin Wheel Switch"),
                AppConfigEntity("feature_scratch_enabled", "true", "Scratch Card Switch"),
                AppConfigEntity("feature_quiz_enabled", "true", "Math Quiz Switch"),
                AppConfigEntity("feature_sell_tasks_enabled", "true", "Text Sell Tasks System"),
                AppConfigEntity("feature_community_links_enabled", "true", "Social & Community Links"),
                AppConfigEntity("feature_withdrawals_enabled", "true", "Cashout / Withdrawal Switch"),
                AppConfigEntity("feature_announcements_enabled", "true", "Announcements / Notifications"),
                AppConfigEntity("premium_upgrade_cost_coins", "3000", "Coins needed to upgrade to Premium"),
                AppConfigEntity("premium_upgrade_cost_bdt", "30", "BDT equivalent for Premium"),
                // Maintenance Mode Configs
                AppConfigEntity("maintenance_mode_enabled", "false", "Global App Maintenance Mode Switch"),
                AppConfigEntity("maintenance_title_en", "System Maintenance in Progress", "Maintenance Screen Title EN"),
                AppConfigEntity("maintenance_title_bn", "সিস্টেম রক্ষণাবেক্ষণ চলছে", "Maintenance Screen Title BN"),
                AppConfigEntity("maintenance_message_en", "We are currently improving our services to provide you with the best experience. The app will be back online shortly!", "Maintenance Message EN"),
                AppConfigEntity("maintenance_message_bn", "উন্নত সেবার জন্য প্ল্যাটফর্ম রক্ষণাবেক্ষণের কাজ চলছে। সাময়িক অসুবিধার জন্য আমরা আন্তরিকভাবে দুঃখিত। দ্রুতই অ্যাপ পুনরায় চালু হবে!", "Maintenance Message BN"),
                AppConfigEntity("maintenance_start_time", "", "Scheduled Maintenance Start Time"),
                AppConfigEntity("maintenance_end_time", "", "Scheduled Maintenance End Time"),
                // Account Activation & Pro System
                AppConfigEntity("feature_require_activation", "true", "Require Account Activation Deposit"),
                AppConfigEntity("account_activation_fee_bdt", "100", "Activation Fee in BDT"),
                AppConfigEntity("pro_activation_percent", "150", "Pro Upgrade Requirement as Percentage of Base Activation"),
                AppConfigEntity("pro_daily_task_limit", "10", "Max daily Pro tasks allowed per account"),
                // Withdrawal Charges
                AppConfigEntity("withdraw_charge_percent", "5", "Withdrawal Fee Percentage"),
                AppConfigEntity("min_withdraw_bdt", "50", "Minimum Withdrawal in BDT"),
                // Deposit Payment Numbers & Details
                AppConfigEntity("deposit_bkash_number", "01700000000 (Send Money)", "Deposit bKash Number"),
                AppConfigEntity("deposit_nagad_number", "01800000000 (Send Money)", "Deposit Nagad Number"),
                AppConfigEntity("deposit_rocket_number", "01900000000 (Send Money)", "Deposit Rocket Number"),
                AppConfigEntity("deposit_upay_number", "01600000000 (Send Money)", "Deposit Upay Number"),
                AppConfigEntity("deposit_usdt_address", "TYD9q3...TRC20AddressHere", "Deposit USDT TRC20 Address"),
                AppConfigEntity("referral_base_url", "https://incomezonex.com/join?ref=", "Referral Base Web Link")
            )
            configDao.setConfigs(defaultConfigList)

            // Seed default demo user if none exists
            val existing = userDao.getUserByIdSync(1)
            if (existing == null) {
                val defaultUser = UserEntity(
                    id = 1,
                    name = "IncomeZoneX Member",
                    email = "member@incomezonex.com",
                    phone = "01700000000",
                    passwordHash = "123456",
                    referralCode = "IZ8842",
                    coins = 350,
                    totalEarned = 350,
                    isAdmin = false,
                    membershipTier = "FREE"
                )
                userDao.insertUser(defaultUser)
            }

            // Seed Task Groups only if not already seeded
            if (groupDao.getGroupCount() == 0) {
                val group1Id = groupDao.insertGroup(
                TaskGroupEntity(
                    nameEn = "Social & Digital Leads",
                    nameBn = "ডিজিটাল ও সোশ্যাল লিডস",
                    descriptionEn = "Submit verified channel links and social details",
                    descriptionBn = "ভেরিফাইড চ্যানেল ও সোশ্যাল তথ্যাদি সাবমিট করুন",
                    iconName = "Share",
                    displayOrder = 1,
                    isEnabled = true,
                    accessRule = "BOTH"
                )
            )

            val group2Id = groupDao.insertGroup(
                TaskGroupEntity(
                    nameEn = "Text-Only Sell System",
                    nameBn = "টেক্সট সেল সিস্টেম",
                    descriptionEn = "Submit micro texts, codes & verified data for high rates",
                    descriptionBn = "মাইক্রো টেক্সট, কোড ও ডাটা এন্ট্রি করে আয় করুন",
                    iconName = "Sell",
                    displayOrder = 2,
                    isEnabled = true,
                    accessRule = "BOTH"
                )
            )

            val group3Id = groupDao.insertGroup(
                TaskGroupEntity(
                    nameEn = "Premium VIP Tasks",
                    nameBn = "প্রিমিয়াম ভিআইপি টাস্ক",
                    descriptionEn = "Exclusive high-reward tasks for Premium Members only",
                    descriptionBn = "শুধুমাত্র প্রিমিয়াম মেম্বারদের জন্য বিশেষ উচ্চমূল্যের কাজ",
                    iconName = "Star",
                    displayOrder = 3,
                    isEnabled = true,
                    accessRule = "PREMIUM"
                )
            )

            // Seed Products with Custom Text Field definitions (JSON format)
            val p1Fields = """
                [
                    {"fieldId":"tg_channel_name","label":"Telegram Channel / Group Name","instruction":"Enter the exact channel name","placeholder":"e.g. Daily Tech Deals","isRequired":true,"order":1},
                    {"fieldId":"tg_channel_link","label":"Channel Username or Invite Link","instruction":"Provide public link or private invite URL","placeholder":"https://t.me/yourchannel","isRequired":true,"order":2},
                    {"fieldId":"member_count","label":"Current Subscriber / Member Count","instruction":"Total members at time of submission","placeholder":"e.g. 1500","isRequired":true,"order":3},
                    {"fieldId":"additional_notes","label":"Additional Notes / Comments","instruction":"Any extra proof details","placeholder":"Optional remarks","isRequired":false,"order":4}
                ]
            """.trimIndent()

            productDao.insertProduct(
                ProductTaskEntity(
                    groupId = group1Id,
                    categoryName = "Social Links",
                    titleEn = "Telegram Community Link Submission",
                    titleBn = "টেলিগ্রাম কমিউনিটি লিংক সাবমিশন",
                    descriptionEn = "Submit active public or private Telegram channels for community directory verification. Each approved submission pays real cash.",
                    descriptionBn = "কমিউনিটি ডিরেক্টরিতে যুক্ত করতে টেলিগ্রাম চ্যানেল লিংক দিন। প্রতিটি অনুমোদিত সাবমিশনে নিশ্চিত পেমেন্ট।",
                    rateAmount = 25.0,
                    rateType = "BDT",
                    accessRule = "BOTH",
                    displayOrder = 1,
                    isEnabled = true,
                    fieldsConfigJson = p1Fields
                )
            )

            val p2Fields = """
                [
                    {"fieldId":"fb_page_name","label":"Facebook Page Name","instruction":"Enter official FB Page title","placeholder":"e.g. Bangladesh Tech Corner","isRequired":true,"order":1},
                    {"fieldId":"fb_page_url","label":"Page URL / Link","instruction":"Copy and paste page URL","placeholder":"https://facebook.com/yourpage","isRequired":true,"order":2},
                    {"fieldId":"contact_email_or_phone","label":"Admin Contact Email or Phone","instruction":"Contact info shown on the page","placeholder":"info@example.com or 017...","isRequired":true,"order":3}
                ]
            """.trimIndent()

            productDao.insertProduct(
                ProductTaskEntity(
                    groupId = group1Id,
                    categoryName = "Social Links",
                    titleEn = "Facebook Page Lead Entry",
                    titleBn = "ফেসবুক পেইজ লিড এন্ট্রি",
                    descriptionEn = "Collect verified Facebook business page links and contact info to earn instant cash reward upon verification.",
                    descriptionBn = "ভেরিফাইড ফেসবুক পেইজের লিংক ও যোগাযোগের তথ্য জমা দিন। ভেরিফিকেশনের পর সাথে সাথে পেমেন্ট।",
                    rateAmount = 35.0,
                    rateType = "BDT",
                    accessRule = "BOTH",
                    displayOrder = 2,
                    isEnabled = true,
                    fieldsConfigJson = p2Fields
                )
            )

            val p3Fields = """
                [
                    {"fieldId":"data_code","label":"Redeem Code / Promo Token","instruction":"Enter the 10-16 character gift or promo code","placeholder":"XXXX-XXXX-XXXX","isRequired":true,"order":1},
                    {"fieldId":"provider_name","label":"Service or Brand Provider","instruction":"Platform where the code was generated","placeholder":"e.g. Google Play / Amazon / Steam","isRequired":true,"order":2},
                    {"fieldId":"user_wallet_phone","label":"Your Payout Account Number","instruction":"Number where you want cashout confirmation","placeholder":"017XXXXXXXX","isRequired":true,"order":3}
                ]
            """.trimIndent()

            productDao.insertProduct(
                ProductTaskEntity(
                    groupId = group2Id,
                    categoryName = "Text Sell",
                    titleEn = "Promo Code & Digital Token Sell",
                    titleBn = "প্রোমো কোড ও ডিজিটাল টোকেন সেল",
                    descriptionEn = "Sell promo codes, digital coupons and vouchers. Admin will check code validity and release funds at the highest applicable rate.",
                    descriptionBn = "ডিজিটাল কুপন বা প্রোমো কোড বিক্রি করুন। এডমিন যাচাই করে সর্বোচ্চ রেটে আপনার একাউন্টে টাকা যুক্ত করবেন।",
                    rateAmount = 50.0,
                    rateType = "BDT",
                    accessRule = "BOTH",
                    displayOrder = 1,
                    isEnabled = true,
                    fieldsConfigJson = p3Fields
                )
            )

            val p4Fields = """
                [
                    {"fieldId":"app_package","label":"Tested App / Service Name","instruction":"Name of the app tested","placeholder":"e.g. Fintech Pro App","isRequired":true,"order":1},
                    {"fieldId":"detailed_feedback","label":"Detailed Bug Report / Feedback Text","instruction":"Provide at least 3-4 lines of feedback","placeholder":"Describe what you tested and any issues...","isRequired":true,"order":2},
                    {"fieldId":"device_model","label":"Android Device Model & OS","instruction":"e.g. Samsung Galaxy A52 (Android 14)","placeholder":"Device model info","isRequired":true,"order":3}
                ]
            """.trimIndent()

            productDao.insertProduct(
                ProductTaskEntity(
                    groupId = group3Id,
                    categoryName = "VIP Audits",
                    titleEn = "VIP App & Web Testing Audit",
                    titleBn = "ভিআইপি অ্যাপ ও ওয়েব টেস্টিং অডিট",
                    descriptionEn = "Premium VIP task: complete app testing feedback through text report. Earn premium high payouts.",
                    descriptionBn = "প্রিমিয়াম ভিআইপি কাজ: অ্যাপ টেস্ট করে বিস্তারিত টেক্সট রিপোর্ট দিন। সর্বোচ্চ রেটে আয় করুন।",
                    rateAmount = 100.0,
                    rateType = "BDT",
                    accessRule = "PREMIUM",
                    displayOrder = 1,
                    isEnabled = true,
                    fieldsConfigJson = p4Fields
                )
            )

            // Seed Announcements
            announcementDao.insertAnnouncement(
                AnnouncementEntity(
                    titleEn = "🎉 Welcome to IncomeZoneX!",
                    titleBn = "🎉 ইনকামজোনএক্সে আপনাকে স্বাগতম!",
                    bodyEn = "Start earning today with Daily Streaks, Lucky Spin, Math Quizzes and the Text Sell System. Fast cashouts via bKash, Nagad & Rocket!",
                    bodyBn = "আজই ইনকাম শুরু করুন স্পিন, কুইজ এবং টেক্সট সেল টাস্কের মাধ্যমে। বিকাশ, নগদ ও রকেটে দ্রুত পেমেন্ট নিন!",
                    targetAudience = "ALL",
                    isImportant = true
                )
            )

            announcementDao.insertAnnouncement(
                AnnouncementEntity(
                    titleEn = "⭐ Upgrade to Premium Membership",
                    titleBn = "⭐ প্রিমিয়াম মেম্বারশিপে আপগ্রেড করুন",
                    bodyEn = "Premium members get access to VIP high-rate tasks (paying up to ৳100 per submission) and priority payout processing.",
                    bodyBn = "প্রিমিয়াম মেম্বাররা পাচ্ছেন ১০০ টাকা পর্যন্ত উচ্চমূল্যের ভিআইপি টাস্ক এবং সবার আগে পেমেন্ট পাওয়ার সুযোগ।",
                    targetAudience = "FREE",
                    isImportant = false
                )
            )
            }
        }
    }
}
