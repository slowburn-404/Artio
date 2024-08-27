package dev.borisochieng.artio.ui.navigation

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import dev.borisochieng.artio.ui.screens.auth.AuthViewModel
import dev.borisochieng.artio.ui.screens.auth.LoginScreen
import dev.borisochieng.artio.ui.screens.auth.ResetPasswordScreen
import dev.borisochieng.artio.ui.screens.auth.SignUpScreen
import dev.borisochieng.artio.ui.screens.drawingboard.DrawingBoard
import dev.borisochieng.artio.ui.screens.drawingboard.SketchPadViewModel
import dev.borisochieng.artio.ui.screens.home.HomeScreen
import dev.borisochieng.artio.ui.screens.home.HomeViewModel
import dev.borisochieng.artio.ui.screens.onboarding.OnBoardingScreen
import dev.borisochieng.artio.ui.screens.profile.ProfileScreen
import dev.borisochieng.artio.ui.screens.profile.UpdateProfileScreen
import dev.borisochieng.artio.ui.screens.settings.SettingsScreen
import dev.borisochieng.artio.utils.AnimationDirection
import dev.borisochieng.artio.utils.VOID_ID
import dev.borisochieng.artio.utils.animatedComposable
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
            animationDirection = AnimationDirection.UpDown,
            arguments = listOf(
                navArgument("sketchId") { type = NavType.StringType},
                navArgument("userId") { type = NavType.StringType},
                navArgument("isFromCollabUrl") { type = NavType.StringType},
            )
        ) { backStackEntry ->
            val sketchId = backStackEntry.arguments?.getString("sketchId")
                ?: "" // sketchId is the same as boardId
            val userId = backStackEntry.arguments?.getString("userId")
                ?: FirebaseAuth.getInstance().currentUser?.uid ?: VOID_ID
            val isFromCollabUrl = backStackEntry.arguments?.getString("isFromCollabUrl").toBoolean()

            LaunchedEffect(sketchId, isFromCollabUrl) {
                if (!isFromCollabUrl) {
                    sketchPadViewModel.fetchSketch(sketchId)
                    sketchPadViewModel.generateCollabUrl(sketchId)
                } else if(userId != VOID_ID){
                    sketchPadViewModel.fetchSingleSketch(boardId = sketchId, userId = userId)
                }
                //listen for path changes in collab mode
                if(isFromCollabUrl && userId != VOID_ID && sketchId != VOID_ID) {
                    sketchPadViewModel.listenForSketchChanges(userId = userId, boardId = sketchId)
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
                userId = userId,
                isFromCollabUrl = isFromCollabUrl
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
            SignUpScreen(navigate = navActions::navigate)
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