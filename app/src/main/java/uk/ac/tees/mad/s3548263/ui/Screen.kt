package uk.ac.tees.mad.s3548263.ui

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object History : Screen("history")
    object Statistics : Screen("statistics")
    object Achievements : Screen("achievements")
}