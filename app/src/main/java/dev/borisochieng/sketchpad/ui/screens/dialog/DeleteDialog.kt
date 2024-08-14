package dev.borisochieng.sketchpad.ui.screens.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.outlinedButtonColors
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun DeleteDialog(
	onDeleteSketch: () -> Unit,
	onDismiss: () -> Unit
) {
	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text("Delete sketch") },
		text = {
			Text("Are you sure you want to delete this sketch? You may not be able to recover it.")
		},
		confirmButton = {
			OutlinedButton(
				onClick = onDeleteSketch,
				colors = outlinedButtonColors(contentColor = colorScheme.error)
			) {
				Text("Delete")
			}
		},
		dismissButton = {
			Button(onClick = onDismiss) {
				Text("Cancel")
			}
		}
	)
}