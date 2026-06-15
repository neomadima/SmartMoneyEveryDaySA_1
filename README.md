# Smart Money EveryDay SA_1 🇿🇦 (Part 3)

Smart Money EveryDay SA_1 is a feature-rich personal finance management application specifically tailored for the South African banking and economic landscape. This version represents **Part 3** of the project development.

## 🌟 Key Features

### 📊 Intelligent Dashboard
*   **Total Financial Snapshot:** Instantly see your main account balance and overall financial standing.
*   **Financial Health Score (0-100) [Added in Part 3]:** A proprietary algorithm that calculates your financial health based on budgeting discipline, savings consistency, and daily spending habits.
*   **Interactive Spending Trends:** Visualize your outgoing expenses over the last 7 days with a dynamic line graph.
*   **Category Breakdown:** View exactly where your money goes (e.g., Groceries, Transport, Utilities) with color-coded progress bars.
*   **Daily Financial Wisdom [Added in Part 3]:** Start your day with actionable tips and quotes to improve your financial literacy.
*   **Activity Streaks:** Stay motivated by maintaining a streak of logging your financial activities.

### 🏦 Comprehensive Account Management
*   **Multi-Account Support:** Manage Cheque, Savings, Credit Cards, and Investment accounts all in one place.
*   **Dynamic Account Creation:** Easily add new accounts with custom names, types, and starting balances.
*   **Detailed History:** Drill down into any account to see full transaction histories and specific balance details.
*   **Masked Account Numbers:** Enhanced privacy with masked account identifiers (e.g., **** 4582).

### 🎯 Goal-Oriented Savings
*   **Visual Progress Tracking:** Set goals like "Emergency Fund" or "New Car" and watch your progress grow through intuitive visual indicators.
*   **One-Tap Editing:** Click on any goal directly from the dashboard to update its name, target amount, or your current progress.
*   **Dynamic Coloring:** Progress bars change color (Orange -> Blue -> Green) as you get closer to achieving your financial milestones.

### 💸 Seamless Transactions & Payments
*   **Quick Expense Logging:** Log your daily spending in seconds with categories and timestamps.
*   **External Payments:** Send money to recipients directly through the "Pay" feature, selecting your source account.
*   **Internal Transfers:** Move money between your own accounts (e.g., from Cheque to Savings) with automated double-entry logging.
*   **Detailed Transaction Records:** View specifics for every transaction, including categories, dates, and account origins.

### 🛡️ Advanced Limits & Security Controls
*   **Granular Daily Limits:** Set and modify maximum daily caps for ATM withdrawals and Online purchases.
*   **Monthly Budgeting Goals:** Define minimum and maximum spending targets for the month.
*   **Balance Safety Net:** Configure a "Floor" amount and receive alerts if your balance drops below your safety threshold.
*   **Protected UI:** Automatic hiding of navigation elements during sensitive limit-setting to ensure focus and clarity.

### 📄 Banking Services
*   **Digital Statements:** Generate and view monthly account statements to keep professional records of your finances.
*   **Achievements & Badges [Added in Part 3]:** Earn badges like "Budget Master," "Super Saver," and "Goal Getter" as you reach specific financial milestones.

### 👤 Profile & Personalization
*   **User Identity:** Manage your personal profile, contact information, and authentication.
*   **Customizable Experience:** Tailor the app's behavior and alerts to match your personal financial journey.

## 🛠️ Technical Implementation

### Architecture & Frameworks
*   **Language:** 100% Kotlin.
*   **Architecture:** **MVVM (Model-View-ViewModel)** for a clean separation of concerns and testable code.
*   **Database:** **Room Persistence Library** for robust, local-first data storage.
*   **UI Components:** **Material Design 3 (M3)** with customized styling for a modern, South African feel.
*   **Navigation:** **Jetpack Navigation Component** with safe-args and deep-linking support.
*   **Concurrency:** **Kotlin Coroutines** and **Flow** for smooth, non-blocking background operations.
*   **Data Binding:** Both **ViewBinding** and **LiveData** for reactive and efficient UI updates.

### Project Organization
*   `com.example.smartmoneyeverydaysa_1.data`: The core data layer containing Room entities (Accounts, Goals, Transactions, Users) and DAOs.
*   `com.example.smartmoneyeverydaysa_1.ui`: (Inferred) UI logic handled through specialized Fragments for each feature.
*   `MainViewModel`: The central state manager that handles business logic and bridges the UI with the data layer.

## 🚀 How to Get Started
1.  **Clone:** `git clone https://github.com/[your-username]/SmartMoneyEveryDaySA_1.git`
2.  **Open:** Open the project in **Android Studio Ladybug (2024.2.1)** or newer.
3.  **Sync:** Let Gradle sync finish and download necessary dependencies.
4.  **Run:** Deploy to an emulator or physical device running **Android 8.0 (API 26)** or higher.

---
*Empowering South Africans to build a smarter financial future, one day at a time.* 🇿🇦❤️
