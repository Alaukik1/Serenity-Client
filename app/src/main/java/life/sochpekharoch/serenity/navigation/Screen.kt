package life.sochpekharoch.serenity.navigation

sealed class Screen(val route: String) {
    object Profile : Screen("profile")
    object Support : Screen("support")
    object PrivacyPolicy : Screen("privacy_policy")
    object TermsOfService : Screen("terms_of_service")
    object Home : Screen("home")
    object Community : Screen("community")
    object SnapHelp : Screen("snaphelp")
    object MediMeet : Screen("medimeet")
    object Wallet : Screen("wallet")
} 