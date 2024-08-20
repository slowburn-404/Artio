package dev.borisochieng.sketchpad.ui.navigation

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.firebase.auth.FirebaseAuth
import dev.borisochieng.sketchpad.ui.screens.auth.AuthViewModel
import dev.borisochieng.sketchpad.ui.screens.auth.LoginScreen
import dev.borisochieng.sketchpad.ui.screens.auth.ResetPasswordScreen
import dev.borisochieng.sketchpad.ui.screens.auth.SignUpScreen
import dev.borisochieng.sketchpad.ui.screens.drawingboard.DrawingBoard
import dev.borisochieng.sketchpad.ui.screens.drawingboard.SketchPadViewModel
import dev.borisochieng.sketchpad.ui.screens.home.HomeScreen
import dev.borisochieng.sketchpad.ui.screens.home.HomeViewModel
import dev.borisochieng.sketchpad.ui.screens.onboarding.OnBoardingScreen
import dev.borisochieng.sketchpad.ui.screens.profile.ProfileScreen
import dev.borisochieng.sketchpad.ui.screens.profile.UpdateProfileScreen
import dev.borisochieng.sketchpad.ui.screens.settings.SettingsScreen
import dev.borisochieng.sketchpad.utils.AnimationDirection
import dev.borisochieng.sketchpad.utils.VOID_ID
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
	broadCastUrl: (Uri) -> Unit,
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
			val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: VOID_ID
			val userId = backStackEntry.arguments?.getString("userId") ?: currentUserId
			LaunchedEffect(sketchId) {
				sketchPadViewModel.apply {
					fetchSketch(sketchId) // from local db
					if (!uiState.userIsLoggedIn) return@LaunchedEffect
					generateCollabUrl(sketchId)
					if (userId == currentUserId) return@LaunchedEffect
					fetchSingleSketch(sketchId, userId) // from remote db
				}
			}

			DrawingBoard(
				uiState = sketchPadViewModel.uiState,
				exportSketch = saveImage,
				actions = sketchPadViewModel::actions,
				exportSketchAsPdf = saveImageAsPdf,
				navigate = navActions::navigate,
				onBroadCastUrl = broadCastUrl,
				boardId = sketchId,
				userId = userId
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