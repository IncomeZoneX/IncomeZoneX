# IncomeZoneX (ইনকামজোনএক্স) - Complete Multi-Tier Earning & Rewards Platform

A modern, responsive, production-ready earning and reward Android application built with **Kotlin** and **Jetpack Compose** following **Clean Architecture** and **MVVM** design principles.

---

## ✨ Features

### 1. 👥 Two-Tier Membership System
- **Free Member:** All newly registered users begin as Free Members with access to standard earning activities and Free-tier task products.
- **Premium Member:** Can be activated via coin upgrades or directly by the Admin. Unlocks VIP high-paying tasks, exclusive categories, and priority payouts.
- **Access Control:** Every task group, product, and earning feature supports configurable access rules (`FREE`, `PREMIUM`, `BOTH`, `DISABLED`).
- **Professional Locked State:** If a Free Member attempts to access a Premium task or group, a locked dialog clearly outlines the VIP benefits and provides the 1-tap upgrade action.

### 2. 🗂️ Task Groups & Text-Only Sell System (Side Panel Flow)
- **Professional Navigation Flow:**
  `Side Panel (Drawer)` ➔ `Main Task/Group` ➔ `Category` ➔ `Product/Task` ➔ `Description` ➔ `Custom Text Submission Form` ➔ `Submit` ➔ `Admin Review` ➔ `Transaction Audit Record`
- **Text-Only Submission:** Users submit data strictly through validated text fields. No photo or media uploads required.
- **Custom Dynamic Fields per Task:**
  - Label shown above textbox
  - Guidance instruction shown above/near textbox
  - Placeholder / watermark inside textbox (strictly UI guidance—never saved as user data)
  - Required vs Optional validation
  - Configurable field ordering
- **Flexible Market Rate Processing:** The submission date does not permanently lock the rate. The Admin can determine/edit the applicable rate when reviewing the submission, and the final rate and payout amount are permanently recorded with the transaction audit record.

### 3. 🔔 In-App Announcements & Notification Center
- **Free-Friendly In-App System:** Operates directly on the local database and reactive StateFlows—no paid push notification service required.
- **Header Bell Icon 🔔:** Displays an interactive unread badge count in the top bar.
- **Notification Center:** Tap to view announcements, expand full messages, and automatically mark them as read.
- **Audience Targeting:** Admin can target announcements to `ALL`, `FREE Members Only`, or `PREMIUM Members Only`, with an optional Priority flag.

### 4. 🎛️ Centralized Feature Toggles (ON / OFF)
Admin can enable or disable platform features in real time without deleting data:
- User Registration
- Free Membership Access
- Premium Membership Upgrades
- Lucky Spin Wheel
- Scratch & Win Cards
- Speed Math Quiz
- Text-Only Sell System
- Telegram / Facebook Links
- Cashout / Withdrawal System
- Announcements & Notices

### 5. 💰 Earning & Mini-Game Activities
- **Daily Streak:** 7-day progressive ladder with streak multiplier and daily claim enforcement.
- **Lucky Spin Wheel:** Interactive 2D Canvas fortune wheel with 8 prize sectors (up to 500 coins) and decelerating spin animation.
- **Scratch Card:** Touch drag gesture scratchable canvas revealing hidden coin rewards.
- **Speed Math Quiz:** 5-question timed arithmetic test with live feedback and coin payouts.
- **Read & Earn:** 15-second timed reading task with anti-cheat progress verification.
- **Referral Program:** Automatic unique referral codes, native Android share sheet integration, and dual rewards (+500 coins each).

### 6. 💳 Wallet & Fast Cashout System
- **Real-time Balance:** Dynamic conversion between coins and BDT (e.g., 100 Coins = 1 BDT, configurable).
- **Payment Methods:**
  - **bKash** (Personal)
  - **Nagad** (Personal)
  - **Rocket** (Personal)
  - **Mobile Recharge** (Grameenphone, Banglalink, Robi, Airtel, Teletalk)
  - **USDT** (TRC20 Crypto)
- **Validation:** Minimum cashout threshold, balance deduction, and transaction history with status tracking (`PENDING`, `APPROVED`, `PAID`, `REJECTED`).

### 7. 🛠️ Centralized Admin Control Console
Protected by PIN access (default PIN: `1234`):
- **Submissions Manager:** View submitted data, adjust applicable rate, approve (automatically credits coins), or reject with reason.
- **Task Groups Manager:** Create, edit, reorder, and enable/disable groups.
- **Products & Tasks Manager:** Add/edit products, configure custom text fields (labels, instructions, placeholders, required status).
- **Members Directory:** View users, set tier (`FREE` vs `PREMIUM`), adjust coin balances with audit logging.
- **Cashouts Review:** Approve, mark as paid, or reject with coin refund.
- **Feature Toggles:** Real-time ON/OFF switches for all platform capabilities.
- **Announcements Manager:** Create and publish targeted announcements.
- **Audit Logs & Links:** View timestamped admin audit trail, and configure Telegram and Facebook URLs.

### 8. 🎨 Theming & 🌍 Dual-Language Localization
- **Theme:** Instant Dark Mode and Light Mode toggle, remembered across sessions using Android DataStore.
- **Language:** Seamless toggle between **বাংলা (Bangla)** and **English** for all navigation, headings, task descriptions, forms, and messages.

---

## 🏗️ Architecture

- **UI Layer:** 100% Jetpack Compose with Material Design 3 (M3).
- **Architecture Pattern:** Clean Architecture + MVVM (Model-View-ViewModel).
- **Local Persistence:** Room Database with Reactive `Flow` queries:
  - `UserEntity` / `UserDao`
  - `TaskGroupEntity` / `TaskGroupDao`
  - `ProductTaskEntity` / `ProductTaskDao`
  - `TaskSubmissionEntity` / `TaskSubmissionDao`
  - `AnnouncementEntity` / `AnnouncementDao`
  - `NotificationReadEntity` / `NotificationReadDao`
  - `AuditLogEntity` / `AuditLogDao`
  - `TransactionEntity` / `TransactionDao`
  - `WithdrawalEntity` / `WithdrawalDao`
  - `AppConfigEntity` / `AppConfigDao`
- **Preferences:** Android Jetpack DataStore for persistent UI theme, language, and session state.

---

## 🚀 How to Run & Build

### Requirements
- Android Studio Ladybug / Meerkat or later
- JDK 17+
- Android SDK 36 (Minimum SDK 24)

### Steps
1. **Clone or Export the Project:**
   ```bash
   git clone <your-repo-url>
   cd incomezonex
   ```
2. **Open in Android Studio:**
   - Select `Open Project` and choose the project directory.
   - Wait for Gradle sync to complete.
3. **Build and Run:**
   - Connect an Android device or start an AVD emulator.
   - Click **Run** (`Shift + F10`) or execute:
     ```bash
     gradle :app:installDebug
     ```
4. **Run Unit & Robolectric Tests:**
   ```bash
   gradle :app:testDebugUnitTest
   ```

---

## 📤 How to Publish to GitHub

1. Open your terminal in the root folder:
   ```bash
   git init
   git add .
   git commit -m "feat: complete IncomeZoneX multi-tier earning platform with text sell system, notifications, and admin console"
   ```
2. Create a new repository on [GitHub](https://github.com/new).
3. Link your remote repository and push:
   ```bash
   git remote add origin https://github.com/<your-username>/<repo-name>.git
   git branch -M main
   git push -u origin main
   ```
