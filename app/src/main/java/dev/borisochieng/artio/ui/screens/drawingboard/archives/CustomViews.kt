package dev.borisochieng.artio.ui.screens.drawingboard.archives

import android.view.Window
import android.widget.SeekBar
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import dev.borisochieng.artio.R
import dev.borisochieng.artio.ui.theme.AppTheme
import dev.borisochieng.artio.ui.theme.StatusBarConfig
import dev.borisochieng.artio.ui.theme.lightScheme

@Composable
fun ControlsBar(
//    drawController: DrawController,
    onDownloadClick: () -> Unit,
    onColorClick: () -> Unit,
    onBgColorClick: () -> Unit,
    onSizeClick: () -> Unit,
    undoVisibility: MutableState<Boolean>,
    redoVisibility: MutableState<Boolean>,
    colorValue: MutableState<Color>,
    bgColorValue: MutableState<Color>,
    sizeValue: MutableState<Int>
) {
    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceAround) {
        MenuItems(
            resId = R.drawable.ic_download,
            desc = "download",
            colorTint = if (undoVisibility.value) lightScheme.onBackground else lightScheme.primary
        ) {
            if (undoVisibility.value) onDownloadClick()
        }
        MenuItems(
            resId = R.drawable.ic_undo,
            desc = "undo",
            colorTint = if (undoVisibility.value) lightScheme.primary else lightScheme.inversePrimary
        ) {
//            if (undoVisibility.value) drawController.unDo()
        }
        MenuItems(
            resId = R.drawable.ic_redo,
            desc = "redo",
            colorTint = if (redoVisibility.value) lightScheme.primary else lightScheme.inversePrimary
        ) {
//            if (redoVisibility.value) drawController.reDo()
        }
        MenuItems(
            resId = R.drawable.ic_refresh,
            desc = "reset",
            colorTint = if (redoVisibility.value || undoVisibility.value) lightScheme.primary else lightScheme.inversePrimary
        ) {
//            drawController.reset()
        }
        MenuItems(
            resId = R.drawable.icons8_color_wheel_24,
            desc = "background color",
            colorTint = bgColorValue.value,
            border = bgColorValue.value == lightScheme.background
        ) {
            onBgColorClick()
        }
        MenuItems(
            resId = R.drawable.palette_2,
            desc = "stroke color",
            colorTint = colorValue.value
        ) {
            onColorClick()
        }
        MenuItems(
            resId = R.drawable.pen,
            desc = "stroke size",
            colorTint = lightScheme.primary
        ) {
            onSizeClick()
        }
    }
}

@Composable
fun RowScope.MenuItems(
    @DrawableRes resId: Int,
    desc: String,
    colorTint: Color,
    border: Boolean = false,
    onClick: () -> Unit
) {
    val modifier = Modifier.size(24.dp)
    IconButton(
        onClick = onClick, modifier = Modifier.weight(1f, true)
    ) {
        Icon(
            painterResource(id = resId),
            contentDescription = desc,
            tint = colorTint,
            modifier = if (border) modifier.border(
                0.5.dp,
                Color.Red,
                shape = CircleShape
            ) else modifier
        )
    }
}

@Composable
fun CustomSeekbar(
    isVisible: Boolean,
    max: Int = 200,
    progress: Int = max,
    progressColor: Int,
    thumbColor: Int,
    onProgressChanged: (Int) -> Unit
) {
    val density = LocalDensity.current
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically {
            // Slide in from 40 dp from the top.
            with(density) { -40.dp.roundToPx() }
        } + expandVertically(
            // Expand from the top.
            expandFrom = Alignment.Top
        ) + fadeIn(
            // Fade in with the initial alpha of 0.3f.
            initialAlpha = 0.3f
        ),
        exit = slideOutVertically() + shrinkVertically() + fadeOut()
    ) {
        val context = LocalContext.current
        Column(
            modifier = Modifier
                .height(100.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(text = "Stroke Width", modifier = Modifier.padding(12.dp, 0.dp, 0.dp, 0.dp))
            AndroidView(
                { SeekBar(context) },
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                it.progressDrawable.colorFilter =
                    BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                        progressColor,
                        BlendModeCompat.SRC_ATOP
                    )
                it.thumb.colorFilter =
                    BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                        thumbColor,
                        BlendModeCompat.SRC_ATOP
                    )
                it.max = max
                it.progress = progress
                it.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {

                    }

                    override fun onStartTrackingTouch(p0: SeekBar?) {}
                    override fun onStopTrackingTouch(p0: SeekBar?) {
                        onProgressChanged(p0?.progress ?: it.progress)
                    }
                })
            }
        }
    }
}

@Composable
fun Root(window: Window, content: @Composable () -> Unit) {
    val isDark = false // isSystemInDarkThemeCustom()
    AppTheme(isDark) {
        window.StatusBarConfig(isDark)
        Surface(color = lightScheme.surface) {
            content.invoke()
        }
    }
}