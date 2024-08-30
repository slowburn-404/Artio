package dev.borisochieng.artio.utils

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ShimmerBoxItem(
	isLoading: Boolean,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier
			.fillMaxWidth()
			.padding(end = 10.dp, bottom = 10.dp)
			.clip(MaterialTheme.shapes.large),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.height(140.dp)
				.then(Modifier.shimmerModifier(isLoading))
		)
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.padding(30.dp, 10.dp)
				.height(14.dp)
				.then(Modifier.shimmerModifier(isLoading))
		)
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 40.dp)
				.padding(bottom = 10.dp)
				.height(12.dp)
				.alpha(0.8f)
				.then(Modifier.shimmerModifier(isLoading))
		)
	}
}

private fun Modifier.shimmerModifier(isLoading: Boolean): Modifier = composed {
	if (isLoading) {
		background(rememberAnimatedShimmerBrush())
	} else {
		background(Color.LightGray.copy(alpha = 0.6f))
	}
}

@Composable
private fun rememberAnimatedShimmerBrush(): Brush {
	val shimmerColors = listOf(
		Color.LightGray.copy(alpha = 0.6f),
		Color.LightGray.copy(alpha = 0.2f),
		Color.LightGray.copy(alpha = 0.6f)
	)

	val transition = rememberInfiniteTransition(label = "shimmer transition")
	val translateAnim = transition.animateFloat(
		initialValue = 0f,
		targetValue = 1000f,
		animationSpec = infiniteRepeatable(
			animation = tween(
				durationMillis = 1000,
				easing = FastOutLinearInEasing
			),
			repeatMode = RepeatMode.Reverse
		), label = "shimmer animation"
	)

	return Brush.linearGradient(
		colors = shimmerColors,
		start = Offset.Zero,
		end = Offset(x = translateAnim.value, y = translateAnim.value)
	)
}