package com.example.ui.localization

import com.example.data.local.LanguageCode

object Strings {
    fun get(key: String, lang: LanguageCode): String {
        val entry = map[key] ?: return key
        return if (lang == LanguageCode.BN) entry.bn else entry.en
    }

    private data class Trans(val en: String, val bn: String)

    private val map = mapOf(
        // General & App
        "app_title" to Trans("IncomeZoneX", "ইনকামজোনএক্স"),
        "coins" to Trans("Coins", "কয়েন"),
        "taka" to Trans("BDT", "টাকা"),
        "currency_symbol" to Trans("৳", "৳"),
        "success" to Trans("Success!", "সফল হয়েছে!"),
        "error" to Trans("Error", "ত্রুটি"),
        "loading" to Trans("Loading...", "লোড হচ্ছে..."),
        "cancel" to Trans("Cancel", "বাতিল"),
        "confirm" to Trans("Confirm", "নিশ্চিত করুন"),
        "save" to Trans("Save Settings", "সেটিংস সংরক্ষণ করুন"),
        "close" to Trans("Close", "বন্ধ করুন"),
        "claim" to Trans("Claim", "দাবি করুন"),
        "claimed" to Trans("Claimed", "গৃহীত"),
        "copy" to Trans("Copy", "কপি"),
        "copied" to Trans("Copied to clipboard!", "ক্লিপবোর্ডে কপি করা হয়েছে!"),
        "share" to Trans("Share", "শেয়ার করুন"),
        "delete" to Trans("Delete", "মুছে ফেলুন"),
        "edit" to Trans("Edit", "এডিট"),
        "add" to Trans("Add New", "নতুন যোগ করুন"),
        "submit" to Trans("Submit", "সাবমিট করুন"),

        // Navigation
        "nav_home" to Trans("Home", "হোম"),
        "nav_tasks" to Trans("Tasks", "টাস্ক"),
        "nav_wallet" to Trans("Wallet", "ওয়ালেট"),
        "nav_community" to Trans("Community", "কমিউনিটি"),
        "nav_profile" to Trans("Profile", "প্রোফাইল"),
        "nav_admin" to Trans("Admin", "এডমিন"),
        "nav_menu" to Trans("Menu", "মেনু"),
        "nav_notifications" to Trans("Notifications", "নোটিফিকেশন"),
        "nav_submissions" to Trans("My Submissions", "আমার সাবমিশন"),

        // Membership
        "membership" to Trans("Membership", "মেম্বারশিপ"),
        "free_member" to Trans("Free Member", "ফ্রি মেম্বার"),
        "premium_member" to Trans("Premium Member", "প্রিমিয়াম মেম্বার"),
        "upgrade_to_premium" to Trans("Upgrade to Premium", "প্রিমিয়ামে আপগ্রেড"),
        "premium_locked_title" to Trans("🔒 Premium Only Feature", "🔒 শুধুমাত্র প্রিমিয়াম মেম্বারদের জন্য"),
        "premium_locked_desc" to Trans("This task is reserved for Premium Members. Upgrade your membership to unlock high-rate tasks and priority cashout!", "এই কাজটি শুধুমাত্র প্রিমিয়াম মেম্বারদের জন্য সংরক্ষিত। উচ্চ আয়ের টাস্ক আনলক করতে এখনই প্রিমিয়ামে আপগ্রেড করুন!"),
        "upgrade_now_coins" to Trans("Upgrade for %d Coins", "%d কয়েনে প্রিমিয়াম নিন"),
        "upgrade_success" to Trans("Congratulations! You are now a Premium Member.", "অভিনন্দন! আপনি এখন একজন প্রিমিয়াম মেম্বার।"),

        // Dashboard
        "welcome_back" to Trans("Welcome back,", "স্বাগতম,"),
        "guest_user" to Trans("Guest Member", "গেস্ট মেম্বার"),
        "total_balance" to Trans("Total Balance", "মোট ব্যালেন্স"),
        "approx_value" to Trans("Approx Value", "আনুমানিক মূল্য"),
        "withdraw_btn" to Trans("Withdraw", "উইথড্র"),
        "refer_btn" to Trans("Invite & Earn", "রেফার ও আয়"),
        "quick_earn" to Trans("Quick Earn Activities", "দ্রুত আয়ের সুযোগ"),
        "community_channels" to Trans("Official Community", "অফিসিয়াল কমিউনিটি"),
        "community_desc" to Trans("Join our official channels for payment proofs, updates & daily giveaway codes!", "পেমেন্ট প্রুফ, নতুন আপডেট ও গিভওয়ের জন্য আমাদের অফিশিয়াল গ্রুপে যুক্ত হোন!"),
        "join_telegram" to Trans("Join Telegram", "টেলিগ্রাম যুক্ত হোন"),
        "join_facebook" to Trans("Visit Facebook", "ফেসবুকে যান"),
        "announcement" to Trans("Notice Board", "জরুরী নোটিশ"),
        "daily_streak" to Trans("Daily Login Streak", "দৈনিক লগইন স্ট্রিক"),
        "day" to Trans("Day", "দিন"),

        // Tasks & Groups Flow
        "task_groups_title" to Trans("Task Groups & Categories", "টাস্ক গ্রুপ ও ক্যাটাগরি"),
        "text_sell_title" to Trans("Text-Only Sell System", "টেক্সট সেল সিস্টেম"),
        "all_products" to Trans("All Products / Tasks", "সকল প্রোডাক্ট ও টাস্ক"),
        "rate_label" to Trans("Rate / Payout", "রেট / পেমেন্ট"),
        "fields_required" to Trans("Custom Text Fields", "তথ্য পূরণের ঘরসমূহ"),
        "instructions" to Trans("Instructions", "নির্দেশনা"),
        "submission_success" to Trans("Submission sent for review! Admin will verify and credit your reward.", "সাবমিশন পাঠানো হয়েছে! এডমিন যাচাই করে পেমেন্ট যুক্ত করবেন।"),
        "view_description" to Trans("Product Description", "বিস্তারিত বিবরণ"),

        // Submissions
        "my_submissions" to Trans("My Task Submissions", "আমার টাস্ক সাবমিশনসমূহ"),
        "no_submissions" to Trans("No submissions yet.", "এখনো কোনো সাবমিশন নেই।"),
        "status_pending" to Trans("Under Review", "অপেক্ষমান"),
        "status_approved" to Trans("Approved", "অনুমোদিত"),
        "status_rejected" to Trans("Rejected", "বাতিল"),
        "applicable_rate" to Trans("Applicable Rate", "প্রযোজ্য রেট"),
        "final_amount" to Trans("Final Amount", "চূড়ান্ত টাকা"),
        "admin_remarks" to Trans("Admin Remarks", "এডমিনের মন্তব্য"),

        // Announcements & Notifications
        "notifications_title" to Trans("Announcements & Alerts", "ঘোষণা ও নোটিফিকেশন"),
        "mark_all_read" to Trans("Mark All Read", "সব পড়া হয়েছে চিহ্নিত করুন"),
        "no_notifications" to Trans("No announcements at this time.", "এই মুহূর্তে কোনো ঘোষণা নেই।"),
        "unread_badge" to Trans("Unread", "নতুন"),
        "priority_alert" to Trans("Priority Alert", "জরুরী বিজ্ঞপ্তি"),

        // Spin
        "lucky_spin_title" to Trans("Lucky Spin Wheel", "লাকি স্পিন হুইল"),
        "spin_button" to Trans("SPIN NOW", "স্পিন করুন"),
        "spins_remaining" to Trans("Spins Left Today", "আজকের অবশিষ্ট স্পিন"),
        "congrats_win" to Trans("Congratulations! You won", "অভিনন্দন! আপনি জিতেছেন"),
        "spin_limit_reached" to Trans("Daily spin limit reached! Check back tomorrow.", "আজকের স্পিন লিমিট শেষ! আগামীকাল আবার চেষ্টা করুন।"),

        // Scratch
        "scratch_title" to Trans("Scratch & Win", "স্ক্র্যাচ করে জিতুন"),
        "scratch_instruction" to Trans("Rub your finger on the card below to reveal your prize!", "পুরস্কার দেখতে নিচের কার্ডটিতে আঙুল ঘষুন!"),
        "card_revealed" to Trans("You've unlocked bonus reward!", "আপনি বোনাস উপহার উন্মোচন করেছেন!"),
        "claim_scratch" to Trans("Add to Wallet", "ওয়ালেটে যোগ করুন"),
        "new_scratch_card" to Trans("Next Card", "পরবর্তী কার্ড"),

        // Quiz
        "quiz_title" to Trans("Speed Math Quiz", "স্পীড অংক কুইজ"),
        "question" to Trans("Question", "প্রশ্ন"),
        "seconds_left" to Trans("seconds left", "সেকেন্ড বাকি"),
        "correct_answer" to Trans("Brilliant! Correct Answer (+25 Coins)", "চমৎকার! সঠিক উত্তর (+২৫ কয়েন)"),

        // Wallet
        "wallet_title" to Trans("My Wallet & Cashout", "আমার ওয়ালেট ও ক্যাশআউট"),
        "available_coins" to Trans("Available Coins", "উত্তোলনযোগ্য কয়েন"),
        "current_rate" to Trans("Conversion Rate", "রূপান্তর হার"),
        "rate_value" to Trans("100 Coins = 1 BDT", "১০০ কয়েন = ১ টাকা"),
        "min_withdraw" to Trans("Min Withdrawal", "সর্বনিম্ন উইথড্র"),
        "withdraw_money" to Trans("Request Payment", "টাকা উত্তোলনের আবেদন"),
        "select_method" to Trans("Select Payment Method", "পেমেন্ট মাধ্যম বেছে নিন"),
        "account_number" to Trans("Account / Mobile Number", "একাউন্ট / মোবাইল নম্বর"),
        "withdraw_amount_coins" to Trans("Coins to Cashout", "কত কয়েন তুলতে চান"),
        "amount_in_bdt" to Trans("You Will Receive", "আপনি পাবেন"),
        "submit_withdraw" to Trans("Confirm Cashout", "উইথড্র কনফার্ম করুন"),

        // Referral
        "refer_title" to Trans("Refer & Earn", "রেফার করুন ও আয় করুন"),
        "refer_headline" to Trans("Invite Friends & Earn Together!", "বন্ধুদের আমন্ত্রণ জানিয়ে দ্বিগুণ আয় করুন!"),
        "refer_rules" to Trans("Share your unique code. Both you and your friend get 500 bonus coins as soon as they sign up!", "আপনার কোড শেয়ার করুন। আপনার রেফারেল কোড ব্যবহার করলে উভয়েই ৫০০ কয়েন পাবেন!"),
        "your_code" to Trans("Your Referral Code", "আপনার রেফারেল কোড"),
        "share_message_en" to Trans("Join IncomeZoneX app to earn real money! Use my referral code %s to get 500 free coins instantly. Download and start earning today!", "ইনকামজোনএক্স অ্যাপে যুক্ত হয়ে নগদ টাকা আয় করুন! আমার রেফারেল কোড %s ব্যবহার করে তাৎক্ষণিক ৫০০ কয়েন বোনাস নিন!"),

        // Community
        "community_page_title" to Trans("Official Community & Support", "অফিসিয়াল কমিউনিটি ও সাপোর্ট"),
        "community_intro" to Trans("Connect with thousands of members, get 24/7 payment support, and join daily reward events.", "হাজারো মেম্বারের সাথে যুক্ত থাকুন, সার্বক্ষণিক পেমেন্ট সাপোর্ট পান এবং প্রতিদিনের রিওয়ার্ড ইভেন্টে অংশ নিন।"),

        // Profile
        "profile_title" to Trans("User Profile & Settings", "ইউজার প্রোফাইল ও সেটিংস"),
        "appearance" to Trans("Appearance & Theme", "থিম ও ডিসপ্লে"),
        "dark_mode" to Trans("Dark Mode", "ডার্ক মোড"),
        "light_mode" to Trans("Light Mode", "লাইট মোড"),
        "language_setting" to Trans("Language / ভাষা", "ভাষা / Language"),
        "admin_access" to Trans("Admin Control Panel", "এডমিন কন্ট্রোল প্যানেল"),
        "enter_admin_pin" to Trans("Enter 4-digit PIN", "৪-সংখ্যার পিন দিন"),
        "admin_button" to Trans("Open Admin Panel", "এডমিন প্যানেলে যান"),
        "logout" to Trans("Logout", "লগআউট"),
        "login_register" to Trans("Login / Register", "লগইন / রেজিস্ট্রেশন"),

        // Auth
        "auth_welcome" to Trans("Join IncomeZoneX", "ইনকামজোনএক্সে যোগ দিন"),
        "auth_subtitle" to Trans("Sign in or create a new account to sync and protect your earnings", "আপনার ব্যালেন্স সুরক্ষিত রাখতে লগইন বা রেজিস্টার করুন"),
        "tab_login" to Trans("Sign In", "লগইন"),
        "tab_register" to Trans("Create Account", "নতুন একাউন্ট"),
        "auth_btn_login" to Trans("Sign In", "লগইন করুন"),
        "auth_btn_register" to Trans("Register & Get 100 Coins", "রেজিস্টার করুন ও ১০০ কয়েন পান"),

        // Admin
        "admin_title" to Trans("Admin Control Console", "এডমিন কন্ট্রোল কনসোল"),
        "admin_tab_groups" to Trans("Groups", "গ্রুপসমূহ"),
        "admin_tab_products" to Trans("Tasks", "টাস্কসমূহ"),
        "admin_tab_submissions" to Trans("Submissions", "সাবমিশন"),
        "admin_tab_cashouts" to Trans("Cashouts", "উইথড্রল"),
        "admin_tab_toggles" to Trans("Toggles", "টগল ও ফিচার"),
        "admin_tab_notices" to Trans("Notices", "ঘোষণা"),
        "admin_tab_users" to Trans("Members", "মেম্বার"),
        "admin_tab_audit" to Trans("Audit Logs", "অডিট লগ"),
        "save_success" to Trans("Changes saved successfully!", "পরিবর্তনগুলো সফলভাবে সংরক্ষিত হয়েছে!")
    )
}

fun String.tr(lang: LanguageCode): String = Strings.get(this, lang)
