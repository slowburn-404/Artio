package dev.borisochieng.sketchpad.utils

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.borisochieng.sketchpad.utils.AnimationDirection.LeftRight

// navigation animation
fun NavGraphBuilder.animatedComposable(
	route: String,
	animationDirection: AnimationDirection = LeftRight,
	arguments: List<NamedNavArgument> = emptyList(),
	deepLinks: List<NavDeepLink> = emptyList(),
	content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) = composable(
	route = route,
	arguments = arguments,
	deepLinks = deepLinks,
	enterTransition = {
		if (animationDirection == LeftRight) {
			slideInHorizontally(tween(700)) { it }
		} else {
			slideInVertically(tween(700)) { it }
		}
	},
	exitTransition = null,
	popEnterTransition = null,
	popExitTransition = {
		if (animationDirection == LeftRight) {
			slideOutHorizontally(tween(700)) { it }
		} else {
			slideOutVertically(tween(700)) { it }
		}
	},
	content = content
)

enum class AnimationDirection {
	LeftRight, UpDown
}