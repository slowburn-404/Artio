package dev.borisochieng.sketchpad.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.borisochieng.sketchpad.auth.presentation.screens.OnBoardingScreen
import dev.borisochieng.sketchpad.auth.presentation.screens.SignUpScreen
import dev.borisochieng.sketchpad.ui.screens.drawingboard.SketchPad
import dev.borisochieng.sketchpad.ui.screens.home.HomeScreen
import dev.borisochieng.sketchpad.ui.screens.profile.ProfileScreen
import dev.borisochieng.sketchpad.ui.screens.settings.SettingsScreen
import dev.borisochieng.sketchpad.utils.AnimationDirection
import dev.borisochieng.sketchpad.utils.animatedComposable

@Composable
fun AppRoute(
	navActions: NavActions,
	navController: NavHostController,
	paddingValues: PaddingValues
) {
	NavHost(
		navController = navController,
		startDestination = AppRoute.OnBoardingScreen.route,
		modifier = Modifier.padding(paddingValues)
	) {
		composable(AppRoute.HomeScreen.route) {
			HomeScreen(navigate = navActions::navigate)
		}
		animatedComposable(
			route = AppRoute.SketchPad.route,
			animationDirection = AnimationDirection.UpDown
		) { backStackEntry ->
			val sketchId = backStackEntry.arguments?.getInt("sketchId") ?: 0
			LaunchedEffect(true) {
				// fetch sketch from database using id
			}

			SketchPad(navigate = navActions::navigate)
		}
		composable(AppRoute.SettingsScreen.route) {
			SettingsScreen(navigate = navActions::navigate)
		}
		composable(AppRoute.ProfileScreen.route) {
			ProfileScreen(navigate = navActions::navigate)
		}
		animatedComposable(AppRoute.OnBoardingScreen.route) {
			OnBoardingScreen(navigate = navActions::navigate)
		}
		animatedComposable(AppRoute.SignUpScreen.route) {
			SignUpScreen(navigate = navActions:: navigate)
		}
	}
}