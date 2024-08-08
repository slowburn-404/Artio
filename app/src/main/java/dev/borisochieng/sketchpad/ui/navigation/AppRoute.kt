package dev.borisochieng.sketchpad.ui.navigation

import android.graphics.Bitmap
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.borisochieng.sketchpad.auth.presentation.screens.LoginScreen
import dev.borisochieng.sketchpad.auth.presentation.screens.OnBoardingScreen
import dev.borisochieng.sketchpad.auth.presentation.screens.SignUpScreen
import dev.borisochieng.sketchpad.ui.screens.drawingboard.SketchPadScreen
import dev.borisochieng.sketchpad.ui.screens.drawingboard.SketchPadViewModel
import dev.borisochieng.sketchpad.ui.screens.home.HomeScreen
import dev.borisochieng.sketchpad.ui.screens.home.HomeViewModel
import dev.borisochieng.sketchpad.ui.screens.profile.ProfileScreen
import dev.borisochieng.sketchpad.ui.screens.profile.UpdateProfileScreen
import dev.borisochieng.sketchpad.ui.screens.settings.SettingsScreen
import dev.borisochieng.sketchpad.utils.AnimationDirection
import dev.borisochieng.sketchpad.utils.animatedComposable
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppRoute(
	modifier: Modifier = Modifier,
	navActions: NavActions,
	navController: NavHostController,
	paddingValues: PaddingValues,
	saveImage: (Bitmap) -> Unit,
	homeViewModel: HomeViewModel = koinViewModel(),
	sketchPadViewModel: SketchPadViewModel = koinViewModel()
) {
	NavHost(
		navController = navController,
		startDestination = AppRoute.OnBoardingScreen.route,
		modifier = modifier.padding(paddingValues)
	) {
		composable(AppRoute.HomeScreen.route) {
			HomeScreen(
				savedSketches = homeViewModel.savedSketches,
				navigate = navActions::navigate
			)
		}
		animatedComposable(
			route = AppRoute.SketchPad.route,
			animationDirection = AnimationDirection.UpDown
		) { backStackEntry ->
			val sketchId = backStackEntry.arguments?.getInt("sketchId")
			LaunchedEffect(true) {
				sketchPadViewModel.fetchSketch(sketchId)
			}

			SketchPadScreen(
				sketch = sketchPadViewModel.sketch,
				save = saveImage,
				actions = sketchPadViewModel::actions,
				navigate = navActions::navigate
			)
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
		animatedComposable(AppRoute.LoginScreen.route) {
			LoginScreen(navigate = navActions::navigate)
		}
		animatedComposable(AppRoute.UpdateProfileScreen.route) {
			UpdateProfileScreen(navigate = navActions::navigate)
		}
	}
}