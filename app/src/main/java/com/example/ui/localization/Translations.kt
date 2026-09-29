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
        "coins" to Trans("BDT (৳)", "টাকা (৳)"),
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
        "premium_member" to Trans("Pro Member", "প্রো মেম্বার"),
        "upgrade_to_premium" to Trans("Upgrade to Pro", "প্রো মেম্বারশিপ নিন"),
        "premium_locked_title" to Trans("🔒 Pro / Premium Feature", "🔒 প্রো মেম্বারদের জন্য"),
        "premium_locked_desc" to Trans("This task is reserved for Pro Members. Upgrade your membership to unlock high-rate tasks and priority cashout!", "এই কাজটি শুধুমাত্র প্রো মেম্বারদের জন্য। উচ্চ আয়ের কাজ আনলক করতে এখনই প্রো-তে আপগ্রেড করুন!"),
        "upgrade_now_coins" to Trans("Upgrade for ৳%s", "৳%s টাকায় প্রো নিন"),
        "upgrade_success" to Trans("Congratulations! You are now a Pro Member.", "অভিনন্দন! আপনি এখন একজন প্রো মেম্বার।"),

        // Dashboard
        "welcome_back" to Trans("Welcome back,", "স্বাগতম,"),
        "guest_user" to Trans("Guest Member", "গেস্ট মেম্বার"),
        "total_balance" to Trans("Available Balance", "মোট ব্যালেন্স"),
        "approx_value" to Trans("Available Balance", "উপলব্ধ ব্যালেন্স"),
        "withdraw_btn" to Trans("Withdraw", "উইথড্র"),
        "refer_btn" to Trans("Invite & Earn", "রেফার ও আয়"),
        "quick_earn" to Trans("Quick Earn Activities", "দ্রুত আয়ের সুযোগ"),
        "community_channels" to Trans("Official Community", "অফিসিয়াল কমিউনিটি"),
        "community_desc" to Trans("Join our official channels for payment proofs, updates & daily giveaway codes!", "পেমেন্ট প্রুফ, নতুন আপডেট ও গিভওয়ের জন্য আমাদের অফিশিয়াল গ্রুপে যুক্ত হোন!"),
        "join_telegram" to Trans("Join Telegram", "টেলিগ্রাম যুক্ত হোন"),
        "join_facebook" to Trans("Visit Facebook", "ফেসবুকে যান"),
        "announcement" to Trans("Notice Board", "জরুরী নোটিশ"),
        "daily_streak" to Trans("Daily Tasks", "দৈনিক কাজ"),
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
        "correct_answer" to Trans("Brilliant! Correct Answer (+৳5)", "চমৎকার! সঠিক উত্তর (+৳৫)"),

        // Wallet Full Redesign
        "wallet_title" to Trans("Wallet & Finances", "ওয়ালেট ও লেনদেন"),
        "available_balance" to Trans("Available Balance", "উত্তোলনযোগ্য ব্যালেন্স"),
        "available_coins" to Trans("Available Balance", "উত্তোলনযোগ্য ব্যালেন্স"),
        "current_rate" to Trans("Currency", "মুদ্রা"),
        "rate_value" to Trans("Bangladeshi Taka (BDT)", "বাংলাদেশি টাকা (৳)"),
        "min_withdraw" to Trans("Min Withdrawal", "সর্বনিম্ন উইথড্র"),
        "withdraw_money" to Trans("Withdraw", "টাকা উত্তোলন"),
        "deposit_money" to Trans("Deposit / Add Funds", "ডিপোজিট / ফান্ড যোগ"),
        "select_method" to Trans("Select Payment Method", "পেমেন্ট মাধ্যম বেছে নিন"),
        "account_number" to Trans("Account / Mobile Number", "একাউন্ট / মোবাইল নম্বর"),
        "withdraw_amount_bdt" to Trans("Withdraw Amount (৳)", "কত টাকা তুলতে চান (৳)"),
        "deposit_amount_bdt" to Trans("Deposit Amount (৳)", "কত টাকা জমা দিতে চান (৳)"),
        "withdraw_amount_coins" to Trans("Withdraw Amount (৳)", "কত টাকা তুলতে চান (৳)"),
        "amount_in_bdt" to Trans("Net Payout", "আপনি পাবেন"),
        "submit_withdraw" to Trans("Submit Withdrawal", "উইথড্র কনফার্ম করুন"),
        "withdraw_charge_fee" to Trans("Withdrawal Fee", "উইথড্র চার্জ / ফি"),
        "net_received_amount" to Trans("Net Amount", "প্রাপ্ত টাকা"),
        "coming_soon" to Trans("COMING SOON", "শীঘ্রই আসছে"),
        "recharge_coming_soon" to Trans("Mobile Recharge withdrawal is coming soon! Please use bKash, Nagad or Rocket.", "মোবাইল রিচার্জ শীঘ্রই চালু হবে! অনুগ্রহ করে বিকাশ, নগদ বা রকেট ব্যবহার করুন।"),
        "deposit_reason" to Trans("Deposit Reason / Purpose", "ডিপোজিটের উদ্দেশ্য"),
        "reason_activation" to Trans("Account Activation", "একাউন্ট একটিভেশন"),
        "reason_pro" to Trans("Pro / Premium Membership", "প্রো মেম্বারশিপ আপগ্রেড"),
        "reason_other" to Trans("General Wallet Deposit", "সাধারণ ডিপোজিট"),
        "account_status" to Trans("Account Status", "একাউন্টের অবস্থা"),
        "account_active" to Trans("Active & Verified", "সক্রিয় ও যাচাইকৃত"),
        "account_inactive" to Trans("Inactive (Activation Required)", "নিষ্ক্রিয় (একটিভেশন প্রয়োজন)"),
        "activate_account_now" to Trans("Activate Account Now", "একাউন্ট একটিভ করুন"),
        "activation_required_msg" to Trans("Your account is currently inactive. Please deposit the activation fee to unlock all earning tasks and cashout features!", "আপনার একাউন্টটি বর্তমানে নিষ্ক্রিয়। সকল কাজের সুযোগ এবং টাকা উত্তোলনের জন্য একাউন্ট একটিভ করুন!"),
        "deposit_instruction" to Trans("Send money to any number below, then enter your TrxID and Sender Number:", "নিচের যেকোনো নম্বরে সেন্ড মানি করে TrxID ও প্রেরক নম্বরটি দিন:"),
        "trx_id" to Trans("Transaction ID (TrxID)", "ট্রানজেকশন আইডি (TrxID)"),
        "sender_number" to Trans("Sender Phone / Wallet", "প্রেরকের নম্বর / ওয়ালেট"),
        "submit_deposit" to Trans("Submit Deposit", "ডিপোজিট সাবমিট করুন"),
        "deposit_submitted_success" to Trans("Deposit request submitted successfully! Admin will verify and activate your request.", "ডিপোজিট রিকোয়েস্ট সফলভাবে পাঠানো হয়েছে! এডমিন দ্রুত ভেরিফাই করবেন।"),
        "income_summary" to Trans("Income Breakdown", "আয়ের বিবরণী"),
        "total_income" to Trans("Total Income", "সর্বমোট আয়"),
        "category_social_income" to Trans("Digital Leads Earnings", "সোশ্যাল ও ডিজিটাল লিডস"),
        "category_text_income" to Trans("Text Sell Earnings", "টেক্সট সেল আয়"),
        "category_vip_income" to Trans("VIP Task Earnings", "ভিআইপি টাস্ক আয়"),

        // Profile & Edit
        "profile_title" to Trans("User Profile & Settings", "ইউজার প্রোফাইল ও সেটিংস"),
        "appearance" to Trans("Appearance & Theme", "থিম ও ডিসপ্লে"),
        "dark_mode" to Trans("Dark Mode", "ডার্ক মোড"),
        "light_mode" to Trans("Light Mode", "লাইট মোড"),
        "language_setting" to Trans("Language / ভাষা", "ভাষা / Language"),
        "edit_profile" to Trans("Edit Profile", "প্রোফাইল এডিট"),
        "save_profile" to Trans("Save Profile Changes", "পরিবর্তন সংরক্ষণ করুন"),
        "full_name" to Trans("Full Name", "পুরো নাম"),
        "phone_number" to Trans("Phone Number", "মোবাইল নম্বর"),
        "new_password_optional" to Trans("New Password (leave blank to keep current)", "নতুন পাসওয়ার্ড (পরিবর্তন না করতে ফাঁকা রাখুন)"),
        "profile_updated_success" to Trans("Profile updated successfully!", "প্রোফাইল সফলভাবে আপডেট করা হয়েছে!"),
        "logout" to Trans("Logout", "লগআউট"),
        "login_register" to Trans("Login / Register", "লগইন / রেজিস্ট্রেশন"),

        // Referral Link
        "refer_title" to Trans("Refer & Earn", "রেফার করুন ও আয় করুন"),
        "refer_headline" to Trans("Invite Friends & Earn Together!", "বন্ধুদের আমন্ত্রণ জানিয়ে দ্বিগুণ আয় করুন!"),
        "refer_rules" to Trans("Share your unique referral code or link. Earn ৳50 bonus when friends register!", "আপনার রেফারেল কোড বা লিংক শেয়ার করুন। বন্ধু যুক্ত হলে পাবেন ৳৫০ বোনাস!"),
        "your_code" to Trans("Your Referral Code", "আপনার রেফারেল কোড"),
        "referral_link" to Trans("Your Referral Link", "আপনার রেফারেল লিংক"),
        "copy_link" to Trans("Copy Referral Link", "রেফারেল লিংক কপি"),
        "share_message_en" to Trans("Join IncomeZoneX to earn real money via micro-tasks! Register with my link: %s or code %s. Instant bonus!", "ইনকামজোনএক্সে যুক্ত হয়ে নগদ টাকা আয় করুন! আমার লিংক %s বা কোড %s দিয়ে রেজিস্টার করুন!"),

        // Auth
        "auth_welcome" to Trans("Join IncomeZoneX", "ইনকামজোনএক্সে যোগ দিন"),
        "auth_subtitle" to Trans("Sign in or create a new account to sync and protect your earnings", "আপনার ব্যালেন্স সুরক্ষিত রাখতে লগইন বা রেজিস্টার করুন"),
        "tab_login" to Trans("Sign In", "লগইন"),
        "tab_register" to Trans("Create Account", "নতুন একাউন্ট"),
        "auth_btn_login" to Trans("Sign In", "লগইন করুন"),
        "auth_btn_register" to Trans("Register & Get ৳100 Welcome Bonus", "রেজিস্টার করুন ও ৳১০০ ওয়েলকাম বোনাস নিন"),

        // Pro & Task Rules
        "pro_earning_range" to Trans("Earn ৳1–৳99 per task", "প্রতি টাস্কে ৳১–৳৯৯ পর্যন্ত আয়"),
        "pro_task_limit_reached" to Trans("Daily Pro task limit reached! Come back tomorrow.", "আজকের প্রো টাস্ক লিমিট পূর্ণ হয়েছে! আগামীকাল আবার আসুন।"),
        "task_offline_notice" to Trans("This task is temporarily undergoing maintenance. Please check back later!", "এই কাজটি সাময়িকভাবে রক্ষণাবেক্ষণের অধীনে রয়েছে। কিছুক্ষণ পর চেষ্টা করুন!"),

        // Maintenance
        "maintenance_title" to Trans("System Maintenance", "সিস্টেম রক্ষণাবেক্ষণ"),
        "maintenance_screen_desc" to Trans("IncomeZoneX is currently undergoing scheduled updates to provide a smoother and safer experience.", "ইনকামজোনএক্সে উন্নত সেবার স্বার্থে সিস্টেম আপডেট চলছে।"),
        "refresh_btn" to Trans("Check Again", "পুনরায় চেষ্টা করুন"),

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
        "admin_tab_settings" to Trans("System Settings", "সিস্টেম সেটিংস"),
        "save_success" to Trans("Changes saved successfully!", "পরিবর্তনগুলো সফলভাবে সংরক্ষিত হয়েছে!"),
        "admin_change_pin" to Trans("Change Admin Password / PIN", "এডমিন পাসওয়ার্ড / পিন পরিবর্তন"),
        "current_pin" to Trans("Current PIN", "বর্তমান পিন"),
        "new_pin" to Trans("New PIN (min 4 digits)", "নতুন পিন (কমপক্ষে ৪ সংখ্যা)"),
        "confirm_new_pin" to Trans("Confirm New PIN", "নতুন পিন নিশ্চিত করুন"),
        "save_pin" to Trans("Update Password", "পাসওয়ার্ড আপডেট করুন")
    )
}

fun String.tr(lang: LanguageCode): String = Strings.get(this, lang)
