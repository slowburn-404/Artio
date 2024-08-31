package dev.borisochieng.artio.ui.screens.dialog

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.borisochieng.artio.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadingCanvasDialog(closeCanvas: () -> Unit) {
	BasicAlertDialog(
		onDismissRequest = {},
		modifier = Modifier
			.padding(16.dp)
			.clip(AlertDialogDefaults.shape)
			.background(MaterialTheme.colorScheme.background)
			.padding(20.dp)
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically
		) {
			CircularProgressIndicator()
			Text(
				text = "Setting up canvas...",
				modifier = Modifier.padding(start = 20.dp),
				fontSize = 20.sp,
				style = MaterialTheme.typography.headlineSmall
			)
		}

		BackHandler { closeCanvas() }
	}
}

@Preview
@Composable
private fun LoadingCanvasDialogPreview() {
	AppTheme {
		LoadingCanvasDialog {}
	}
}
