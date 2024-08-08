package dev.borisochieng.sketchpad.ui.screens.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SizePickerDialog(
	selectedSize: Float,
	color: Color = Color.Black,
	onSelected: (Float) -> Unit,
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
				.padding(horizontal = 8.dp)
		) {
			Sizes.entries.forEach { size ->
				val borderColor = if (size.strokeWidth == selectedSize) Color.Blue else Color.Transparent

				Box(
					modifier = Modifier
						.padding(bottom = 6.dp)
						.clip(MaterialTheme.shapes.large)
						.border(2.dp, borderColor, MaterialTheme.shapes.large)
						.clickable { onSelected(size.strokeWidth); onDismiss() }
				) {
					HorizontalDivider(
						modifier = Modifier
							.fillMaxWidth()
							.padding(14.dp, 24.dp)
							.clip(MaterialTheme.shapes.large),
						thickness = size.strokeWidth.dp,
						color = color
					)
				}
			}
		}
	}
}

enum class Sizes(val strokeWidth: Float) {
	Small(10f),
	Medium(30f),
	Large(50f),
	ExtraLarge(70f)
}