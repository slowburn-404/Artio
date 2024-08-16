package dev.borisochieng.sketchpad.ui.navigation

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.borisochieng.sketchpad.auth.presentation.screens.LoginScreen
import dev.borisochieng.sketchpad.auth.presentation.screens.OnBoardingScreen
import dev.borisochieng.sketchpad.auth.presentation.screens.ProfileScreen
import dev.borisochieng.sketchpad.auth.presentation.screens.ResetPasswordScreen
import dev.borisochieng.sketchpad.auth.presentation.screens.SignUpScreen
import dev.borisochieng.sketchpad.auth.presentation.screens.UpdateProfileScreen
import dev.borisochieng.sketchpad.auth.presentation.viewmodels.AuthViewModel
import dev.borisochieng.sketchpad.ui.screens.drawingboard.SketchPadViewModel
import dev.borisochieng.sketchpad.ui.screens.drawingboard.alt.DrawingBoard
import dev.borisochieng.sketchpad.ui.screens.home.HomeScreen
import dev.borisochieng.sketchpad.ui.screens.home.HomeViewModel
import dev.borisochieng.sketchpad.ui.screens.settings.SettingsScreen
import dev.borisochieng.sketchpad.utils.AnimationDirection
import dev.borisochieng.sketchpad.utils.animatedComposable
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppRoute(
	paddingValues: PaddingValues,
	navActions: NavActions,
	navController: NavHostController,
	saveImage: (Bitmap) -> Unit,
	saveImageAsPdf: (Bitmap) -> Unit,
	authViewModel: AuthViewModel = koinViewModel(),
	homeViewModel: HomeViewModel = koinViewModel(),
	sketchPadViewModel: SketchPadViewModel = koinViewModel(),
	broadCastUrl: (Uri) -> Unit
) {
	NavHost(
//		modifier = Modifier.padding(paddingValues), this gives the app unnecessary padding
		navController = navController,
		startDestination = authViewModel.startScreen
	) {
		composable(AppRoute.HomeScreen.route) {
			HomeScreen(
				bottomPadding = paddingValues.calculateBottomPadding(),
				uiState = homeViewModel.uiState,
				actions = homeViewModel::actions,
				navigate = navActions::navigate
			)
		}
		animatedComposable(
			route = AppRoute.SketchPad.route,
			animationDirection = AnimationDirection.UpDown
		) { backStackEntry ->
			val sketchId = backStackEntry.arguments?.getString("sketchId") ?: "" // sketchId is the same as boardId
			LaunchedEffect(sketchId) {
				sketchPadViewModel.fetchSketch(sketchId)
				sketchPadViewModel.generateCollabUrl(sketchId)
			}

			DrawingBoard(
				uiState = sketchPadViewModel.uiState,
				exportSketch = saveImage,
				actions = sketchPadViewModel::actions,
				exportSketchAsPdf = saveImageAsPdf,
				navigate = navActions::navigate,
				onBroadCastUrl = broadCastUrl
			)
		}
		composable(AppRoute.SettingsScreen.route) {
			SettingsScreen(navigate = navActions::navigate)
		}
		composable(AppRoute.ProfileScreen.route) {
			ProfileScreen(
				bottomPadding = paddingValues.calculateBottomPadding(),
				navigate = navActions::navigate
			)
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
		animatedComposable(AppRoute.ResetPasswordScreen.route) {
			ResetPasswordScreen(navigate = navActions::navigate)
		}
	}
}