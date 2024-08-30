package dev.borisochieng.artio.ui.screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.borisochieng.artio.database.Sketch
import dev.borisochieng.artio.ui.screens.drawingboard.components.MovableTextBox
import dev.borisochieng.artio.utils.Extensions.formatDate

typealias SketchId = String

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SketchPoster(
	sketch: Sketch,
	modifier: Modifier = Modifier,
	onClick: (SketchId) -> Unit,
	onMenuClicked: (Sketch) -> Unit
) {
	Column(
		modifier = modifier
			.padding(end = 10.dp, bottom = 10.dp)
			.clip(MaterialTheme.shapes.large)
			.background(MaterialTheme.colorScheme.surfaceContainer)
			.clickable { onClick(sketch.id) },
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.background(Color.White)
				.height(140.dp),
			contentAlignment = Alignment.Center
		) {
			Box(Modifier
				.fillMaxSize()
				.scale(0.1f)
			) {
				Canvas(Modifier.fillMaxSize()) {
					sketch.pathList.forEach { path ->
						drawLine(
							color = path.color,
							start = path.start,
							end = path.end,
							strokeWidth = path.strokeWidth,
							cap = StrokeCap.Round
						)
					}
				}
				sketch.textList.forEach { property ->
					MovableTextBox(
						properties = property,
						active = false,
						onRemove = {}
					)
				}
			}
			IconButton(
				onClick = { onMenuClicked(sketch) },
				modifier = Modifier.align(Alignment.TopEnd)
			) {
				Icon(
					imageVector = Icons.Rounded.MoreHoriz,
					contentDescription = "Open menu"
				)
			}
		}
		Text(
			text = sketch.name,
			modifier = Modifier
				.padding(start = 10.dp, top = 10.dp, end = 10.dp)
				.basicMarquee(),
			softWrap = false
		)
		Text(
			text = sketch.lastModified.formatDate(),
			modifier = Modifier
				.padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
				.alpha(0.8f),
			fontSize = 14.sp
		)
	}
}