package dev.borisochieng.sketchpad.ui.screens.drawingboard.data

import android.app.Activity
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import androidx.core.view.doOnLayout
import androidx.core.view.drawToBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object BitmapFactory {
	private val _bitmapGenerators = MutableSharedFlow<Bitmap.Config>(extraBufferCapacity = 1)
	private val bitmapGenerators = _bitmapGenerators.asSharedFlow()

	fun View.getBitmap(
		coroutineScope: CoroutineScope,
		onCaptured: (Bitmap?, Throwable?) -> Unit
	): Job {
//		_bitmapGenerators.tryEmit(Bitmap.Config.ARGB_8888) // causes terrible lag on canvas
		return bitmapGenerators
			.mapNotNull { config -> drawBitmapFromView(config) }
			.onEach { bitmap -> onCaptured(bitmap, null) }
			.catch { error -> onCaptured(null, error) }
			.launchIn(coroutineScope)
	}
}

private suspend fun View.drawBitmapFromView(
	config: Bitmap.Config,
): Bitmap = suspendCoroutine { continuation ->
	doOnLayout { view ->
		if (Build.VERSION_CODES.O > Build.VERSION.SDK_INT) {
			continuation.resume(view.drawToBitmap(config))
			return@doOnLayout
		}

		val window = (context as? Activity)?.window
			?: error("Can't get window from the Context")

		Bitmap.createBitmap(width, height, config).apply {
			val (x, y) = IntArray(2).apply { view.getLocationInWindow(this) }
			PixelCopy.request(
				/* source = */ window,
				/* srcRect = */ getRect(x, y),
				/* dest = */ this,
				/* listener = */ { copyResult ->
					if (copyResult == PixelCopy.SUCCESS) {
						continuation.resume(this)
					} else {
						continuation.resumeWithException(
							RuntimeException("Bitmap generation failed")
						)
					}
				},
				/* listenerThread = */ Handler(Looper.getMainLooper())
			)
		}
	}
}

private fun View.getRect(x: Int, y: Int): android.graphics.Rect {
	val viewWidth = this.width
	val viewHeight = this.height
	return android.graphics.Rect(x, y, viewWidth + x, viewHeight + y)
}

/*fun trackBitmaps(
	it: View,
	coroutineScope: CoroutineScope,
	onCaptured: (ImageBitmap?, Throwable?) -> Unit
) = bitmapGenerators
	.mapNotNull { config -> it.drawBitmapFromView(it.context, config) }
	.onEach { bitmap -> onCaptured(bitmap.asImageBitmap(), null) }
	.catch { error -> onCaptured(null, error) }
	.launchIn(coroutineScope)*/
