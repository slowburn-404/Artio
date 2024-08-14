package dev.borisochieng.sketchpad.ui.navigation

import android.annotation.SuppressLint
import androidx.navigation.NavHostController

class NavActions(private val navController: NavHostController) {

    fun navigate(screen: Screens) {
        when (screen) {
            Screens.HomeScreen -> navigateToHomeScreen()
            is Screens.SketchPad -> navigateToSketchPad(screen.sketchId, screen.boardId, screen.userId)
            Screens.SettingsScreen -> navigateToSettingsScreen()
            Screens.ProfileScreen -> navigateToProfileScreen()
            Screens.Back -> navController.navigateUp()
            Screens.OnBoardingScreen -> navigateToOnBoardingScreen()
            Screens.SignUpScreen -> navigateToSignUpScreen()
            Screens.LoginScreen -> navigateToLoginScreen()
            Screens.UpdateProfileScreen -> navigateToUpdateProfileScreen()
            Screens.ResetPasswordScreen -> navigateToResetPasswordScreen()
        }
    }

    private fun navigateToHomeScreen() {
        navController.navigate(AppRoute.HomeScreen.route) {
            popUpTo(AppRoute.HomeScreen.route) {
                inclusive = false
            }
            launchSingleTop = true
        }
    }

    private fun navigateToSketchPad(
        sketchId: String,
        boardId: String,
        userId: String
    ) {
        navController.navigate(
            AppRoute.SketchPad.routeWithIds(sketchId, boardId, userId)
        )
    }

    private fun navigateToSettingsScreen() {
        navController.navigate(AppRoute.SettingsScreen.route)
    }

    private fun navigateToProfileScreen() {
        navController.navigate(AppRoute.ProfileScreen.route)
    }

    private fun navigateToOnBoardingScreen() {
        navController.navigate(AppRoute.OnBoardingScreen.route)
    }

    private fun navigateToSignUpScreen() {
        navController.navigate(AppRoute.SignUpScreen.route) {
            popUpTo(AppRoute.SignUpScreen.route) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    private fun navigateToLoginScreen() {
        navController.navigate(AppRoute.LoginScreen.route) {
            popUpTo(AppRoute.LoginScreen.route) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    private fun navigateToResetPasswordScreen() {
        navController.navigate(AppRoute.ResetPasswordScreen.route)
    }

    private fun navigateToUpdateProfileScreen() {
        navController.navigate(AppRoute.UpdateProfileScreen.route)
    }

}

@SuppressLint("DefaultLocale")
sealed class AppRoute(val route: String) {
    data object HomeScreen : AppRoute("home_screen")
    data object SketchPad : AppRoute("sketchpad/{sketchId}/{boardId}/{userId}") {
        fun routeWithIds(
            sketchId: String,
            boardId: String,
            userId: String
        ) = String.format("sketchpad/%s/%2s/%3s", sketchId, boardId, userId)
    }

    data object SettingsScreen : AppRoute("settings_screen")
    data object ProfileScreen : AppRoute("profile_screen")
    data object OnBoardingScreen : AppRoute("onboarding_screen")
    data object SignUpScreen : AppRoute("welcome_screen")
    data object LoginScreen : AppRoute("login_screen")
    data object UpdateProfileScreen: AppRoute("update_profile")
    data object ResetPasswordScreen: AppRoute("'reset_password")
}

sealed class Screens {
    data object HomeScreen : Screens()
    data class SketchPad(
        val sketchId: String,
        val boardId: String = "",
        val userId: String = ""
    ) : Screens()
    data object SettingsScreen : Screens()
    data object ProfileScreen : Screens()
    data object Back : Screens()
    data object OnBoardingScreen : Screens()
    data object SignUpScreen : Screens()
    data object LoginScreen : Screens()
    data object UpdateProfileScreen: Screens()
    data object ResetPasswordScreen: Screens()
}