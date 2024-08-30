package dev.borisochieng.artio.ui.screens.dialog

import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.borisochieng.artio.R
import dev.borisochieng.artio.database.Sketch
import dev.borisochieng.artio.ui.screens.dialog.Menus.BackupSketch
import dev.borisochieng.artio.ui.screens.dialog.Menus.Delete
import dev.borisochieng.artio.ui.screens.dialog.Menus.Rename
import dev.borisochieng.artio.ui.screens.home.HomeActions
import dev.borisochieng.artio.utils.Extensions.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemMenuSheet(
	sketch: Sketch,
	backedUp: Boolean,
	userIsLoggedIn: Boolean,
	action: (HomeActions) -> Unit,
	onPromptToLogin: () -> Unit,
	onDismiss: () -> Unit
) {
	val sheetState = rememberModalBottomSheetState(true)
	val openRenameDialog = remember { mutableStateOf(false) }
	val openDeleteDialog = remember { mutableStateOf(false) }

	ModalBottomSheet(
		onDismissRequest = onDismiss,
		sheetState = sheetState,
		dragHandle = { DragHandle(sketch) }
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.windowInsetsPadding(WindowInsets.navigationBars)
		) {
			Menus.entries.forEach { menu ->
				ItemMenu(
					menu = menu,
					backedUp = backedUp,
					onClick = {
						when (menu) {
							BackupSketch -> {
								if (userIsLoggedIn) {
									action(HomeActions.BackupSketch(sketch))
								} else onPromptToLogin()
								onDismiss()
							}
							Rename -> openRenameDialog.value = true
							Delete -> openDeleteDialog.value = true
						}
					}
				)
			}
		}
	}

	if (openRenameDialog.value) {
		NameSketchDialog(
			currentName = sketch.name,
			onNamed = { newName ->
				val renamedSketch = Sketch(
					id = sketch.id,
					name = newName,
					dateCreated = sketch.dateCreated,
					lastModified = sketch.lastModified,
					pathList = sketch.pathList,
					textList = sketch.textList
				)
				action(HomeActions.RenameSketch(renamedSketch))
				openRenameDialog.value = false; onDismiss()
			},
			onDismiss = { openRenameDialog.value = false }
		)
	}

	if (openDeleteDialog.value) {
		DeleteDialog(
			onDeleteSketch = {
				action(HomeActions.DeleteSketch(sketch))
				openDeleteDialog.value = false; onDismiss()
			},
			onDismiss = { openDeleteDialog.value = false }
		)
	}
}

@Composable
private fun ItemMenu(
	menu: Menus,
	backedUp: Boolean,
	modifier: Modifier = Modifier,
	onClick: () -> Unit
) {
	val changeUtils = backedUp && menu == BackupSketch
	val title = stringResource(if (changeUtils) R.string.sketch_is_backed_up else menu.titleRes)
	val icon = if (changeUtils) Icons.Rounded.CheckCircle else menu.icon
	val iconTint = if (changeUtils) Color.Green else Color.Unspecified

	Row(
		modifier = modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp)
			.clip(MaterialTheme.shapes.large)
			.clickable(!changeUtils) { onClick() }
			.padding(16.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Icon(icon, title, tint = iconTint)
		Text(title, Modifier.padding(start = 16.dp))
	}
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun DragHandle(
	sketch: Sketch,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier
			.fillMaxWidth()
			.padding(10.dp)
			.clip(BottomSheetDefaults.ExpandedShape)
			.background(MaterialTheme.colorScheme.background)
			.padding(horizontal = 16.dp)
	) {
		Text(
			text = sketch.name,
			modifier = Modifier
				.padding(top = 10.dp)
				.basicMarquee(),
			fontWeight = FontWeight.Bold,
			softWrap = false
		)
		Text(
			text = "Last modified on: " + sketch.lastModified.formatDate(),
			modifier = Modifier
				.padding(bottom = 10.dp)
				.alpha(0.8f),
			fontSize = 14.sp
		)
	}
}

private enum class Menus(
	@StringRes val titleRes: Int,
	val icon: ImageVector
) {
	Rename(R.string.rename, Icons.Rounded.Edit),
	BackupSketch(R.string.backup_sketch, Icons.Rounded.Backup),
	Delete(R.string.delete, Icons.Rounded.Delete)
}
