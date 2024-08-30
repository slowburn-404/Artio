package dev.borisochieng.artio.ui.screens.drawingboard.utils

import android.graphics.Bitmap
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach

class DrawController internal constructor() {
    private val _bitmapGenerators = MutableSharedFlow<Bitmap.Config>(extraBufferCapacity = 1)
    private val bitmapGenerators = _bitmapGenerators.asSharedFlow()

    fun saveBitmap(config: Bitmap.Config = Bitmap.Config.ARGB_8888) =
        _bitmapGenerators.tryEmit(config)

    fun trackBitmaps(
        it: View,
        coroutineScope: CoroutineScope,
        onCaptured: (ImageBitmap?, Throwable?) -> Unit
    ) = bitmapGenerators
        .mapNotNull { config -> it.drawBitmapFromView(it.context, config) }
        .onEach { bitmap -> onCaptured(bitmap.asImageBitmap(), null) }
        .catch { error -> onCaptured(null, error) }
        .launchIn(coroutineScope)
}

@Composable
fun rememberDrawController(): DrawController {
    return remember { DrawController() }
}