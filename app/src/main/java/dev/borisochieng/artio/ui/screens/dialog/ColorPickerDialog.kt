package dev.borisochieng.artio.ui.screens.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.borisochieng.artio.ui.theme.errorContainerLightMediumContrast
import dev.borisochieng.artio.ui.theme.errorLight
import dev.borisochieng.artio.ui.theme.errorLightMediumContrast
import dev.borisochieng.artio.ui.theme.inversePrimaryDarkHighContrast
import dev.borisochieng.artio.ui.theme.inversePrimaryLight
import dev.borisochieng.artio.ui.theme.onErrorContainerLight
import dev.borisochieng.artio.ui.theme.onPrimaryContainerLight
import dev.borisochieng.artio.ui.theme.onSurfaceLight
import dev.borisochieng.artio.ui.theme.onSurfaceVariantLight
import dev.borisochieng.artio.ui.theme.onTertiaryContainerLight
import dev.borisochieng.artio.ui.theme.outlineLight
import dev.borisochieng.artio.ui.theme.outlineVariantLight
import dev.borisochieng.artio.ui.theme.primaryContainerLight
import dev.borisochieng.artio.ui.theme.primaryLight
import dev.borisochieng.artio.ui.theme.secondaryLight
import dev.borisochieng.artio.ui.theme.surfaceBrightDarkHighContrast
import dev.borisochieng.artio.ui.theme.surfaceContainerLowestDarkHighContrast
import dev.borisochieng.artio.ui.theme.surfaceDimDarkHighContrast
import dev.borisochieng.artio.ui.theme.tertiaryContainerDarkHighContrast
import dev.borisochieng.artio.ui.theme.tertiaryLight
import dev.borisochieng.artio.utils.Extensions.transformList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPickerDialog(
	selectedColor: Color = Color.Black,
	onSelected: (Color) -> Unit,
	onDismiss: () -> Unit
) {
	BasicAlertDialog(
		onDismissRequest = onDismiss,
		modifier = Modifier
			.padding(16.dp)
			.clip(AlertDialogDefaults.shape)
			.background(MaterialTheme.colorScheme.background)
			.padding(20.dp)
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.verticalScroll(rememberScrollState()),
		) {
			val colors = colorsPalette.transformList()
			repeat(colors.size) { index ->
				val rowColors = colors[index]

				ColorRow(
					colors = rowColors,
					selectedColor = selectedColor,
					modifier = Modifier.fillMaxWidth(),
					onSelected = {
						onSelected(it)
						onDismiss()
					}
				)
			}
		}
	}
}

@Composable
private fun ColorRow(
	colors: List<Color>,
	selectedColor: Color,
	modifier: Modifier = Modifier,
	onSelected: (Color) -> Unit
) {
	Row(
		modifier = modifier
			.fillMaxWidth()
			.padding(bottom = 8.dp),
		horizontalArrangement = Arrangement.SpaceAround,
		verticalAlignment = Alignment.CenterVertically
	) {
		repeat(colors.size) { index ->
			val color = colors[index]

			Box(
				Modifier
					.size(48.dp)
					.clip(CircleShape)
					.background(color)
					.clickable { onSelected(color) },
				contentAlignment = Alignment.Center
			) {
				if (color == selectedColor) {
					Icon(Icons.Rounded.Done, null, tint = Color.White)
				}
			}
		}
	}
}

val colorsPalette = listOf(
	secondaryLight,
	primaryLight,
	onPrimaryContainerLight,
	primaryContainerLight,
	onTertiaryContainerLight,
	tertiaryLight,
	Color.Green,
	tertiaryContainerDarkHighContrast,
	errorContainerLightMediumContrast,
	errorLight,
	errorLightMediumContrast,
	onErrorContainerLight,
	Color.Black,
	onSurfaceLight,
	onSurfaceVariantLight,
	surfaceContainerLowestDarkHighContrast,
	outlineVariantLight,
	Color.Yellow.copy(alpha = 0.2f),
	inversePrimaryLight,
	Color.Yellow,
	inversePrimaryDarkHighContrast,
	surfaceDimDarkHighContrast,
	surfaceBrightDarkHighContrast,
	outlineLight,
	Color.Blue,
	Color.Blue.copy(alpha = 0.7f),
	Color.Blue.copy(alpha = 0.3f),
	Color.Blue.copy(alpha = 0.1f),
	Color.Magenta,
	Color.Magenta.copy(alpha = 0.7f),
	Color.Magenta.copy(alpha = 0.3f),
	Color.Magenta.copy(alpha = 0.1f)
)
