package dev.borisochieng.sketchpad.ui.screens.drawingboard.alt

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import dev.borisochieng.sketchpad.database.Sketch
import dev.borisochieng.sketchpad.ui.navigation.Screens
import dev.borisochieng.sketchpad.ui.screens.dialog.NameSketchDialog
import dev.borisochieng.sketchpad.ui.screens.dialog.SavePromptDialog
import dev.borisochieng.sketchpad.ui.screens.dialog.Sizes
import dev.borisochieng.sketchpad.ui.screens.drawingboard.CanvasUiEvents
import dev.borisochieng.sketchpad.ui.screens.drawingboard.CanvasUiState
import dev.borisochieng.sketchpad.ui.screens.drawingboard.SketchPadActions
import dev.borisochieng.sketchpad.ui.screens.drawingboard.SketchPadViewModel
import dev.borisochieng.sketchpad.ui.screens.drawingboard.data.ExportOption
import dev.borisochieng.sketchpad.ui.screens.drawingboard.data.rememberDrawController
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun DrawingBoard(
	sketch: Sketch?,
	uiState: CanvasUiState, // passing the state in the way fixes state management issue
	exportSketch: (Bitmap) -> Unit,
	actions: (SketchPadActions) -> Unit,
	exportSketchAsPdf: (Bitmap) -> Unit,
	navigate: (Screens) -> Unit,
	onBroadCastUrl: (Uri) -> Unit,
	viewModel: SketchPadViewModel = koinViewModel()
) {
	var exportOption by remember { mutableStateOf(ExportOption.PNG) }
	val drawController = rememberDrawController()
	val absolutePaths = remember { mutableStateListOf<PathProperties>() }
	var paths by remember { mutableStateOf<List<PathProperties>>(emptyList()) }
	var drawMode by remember { mutableStateOf(DrawMode.Draw) }
	var pencilSize by remember { mutableFloatStateOf(Sizes.Small.strokeWidth) }
	var color by remember { mutableStateOf(Color.Black) }
	var scale by remember { mutableFloatStateOf(1f) }
	var offset by remember { mutableStateOf(Offset.Zero) }
	val openNameSketchDialog = rememberSaveable { mutableStateOf(false) }
	val openSavePromptDialog = rememberSaveable { mutableStateOf(false) }
	val snackbarHostState = remember { SnackbarHostState() }
	val scope = rememberCoroutineScope()
	val context = LocalContext.current
	val save: (String?) -> Unit = { name ->
		val action = if (name == null) {
			SketchPadActions.UpdateSketch(paths)
		} else {
			openNameSketchDialog.value = false
			val newSketch = Sketch(name = name, pathList = paths)
			SketchPadActions.SaveSketch(newSketch)
		}
		actions(action)
		Toast.makeText(context, "Sketch saved", Toast.LENGTH_SHORT).show()
		navigate(Screens.Back)
	}

	val uiEvents by viewModel.uiEvents.collectAsState(initial = null)

	//listen for path changes
	LaunchedEffect(uiState.paths) {
		if (!uiState.userIsLoggedIn) return@LaunchedEffect
		absolutePaths.clear()
		paths = uiState.paths
		absolutePaths.addAll(paths)
	}

	LaunchedEffect(uiEvents) {
		uiEvents?.let { event ->
			when (event) {
				is CanvasUiEvents.SnackBarEvent -> {
					// Showing Snackbar with the message
					snackbarHostState.showSnackbar(event.message)
				}
				// Handle other events if any
			}
		}
	}

	LaunchedEffect(Unit) { actions(SketchPadActions.CheckIfUserIsLoggedIn) }

	Scaffold(
		topBar = {
			PaletteTopBar(
				canSave = paths != sketch?.pathList,
				canUndo = paths.isNotEmpty(),
				canRedo = paths.size < absolutePaths.size,
				onSaveClicked = {
					if (sketch == null) {
						openNameSketchDialog.value = true
					} else {
						save(null)
					}
				},
				unUndoClicked = { paths -= paths.last() },
				unRedoClicked = {
					val nextPath = absolutePaths[paths.size]
					paths += nextPath
				},
				onExportClicked = { drawController.saveBitmap() },
				onBroadCastUrl = {
					Log.d("Credentials", "User id: ${uiState.boardDetails?.userId} \n Board id: ${uiState.boardDetails?.boardId}")
					if (uiState.userIsLoggedIn) {
						if (!uiState.sketchIsBackedUp || uiState.collabUrl == null) {
							scope.launch { snackbarHostState.showSnackbar("Sketch is not backed up yet") }
							return@PaletteTopBar
						}
						onBroadCastUrl(uiState.collabUrl)
					} else {
						scope.launch {
							val action = snackbarHostState.showSnackbar(
								message = "Sign up to avail collaborative feature",
								actionLabel = "SIGN UP", duration = SnackbarDuration.Short
							)
							if (action != SnackbarResult.ActionPerformed) return@launch
							navigate(Screens.OnBoardingScreen)
						}
					}
				},
				onExportClickedAsPdf = {
					exportOption = ExportOption.PDF
					drawController.saveBitmap()
				},
			)
		},
		bottomBar = {
			PaletteMenu(
				drawMode = drawMode,
				selectedColor = color,
				pencilSize = pencilSize,
				onColorChanged = { color = it },
				onSizeChanged = { pencilSize = it },
				onDrawModeChanged = { drawMode = it },
			)
		},
		snackbarHost = { SnackbarHost(snackbarHostState) },
		containerColor = Color.White
	) { paddingValues ->
		LaunchedEffect(sketch) {
			if (sketch == null) return@LaunchedEffect
			absolutePaths.clear(); paths = emptyList()
			absolutePaths.addAll(sketch.pathList)
			paths = sketch.pathList
		}

		BoxWithConstraints(
			modifier = Modifier
				.fillMaxSize()
				.padding(paddingValues),
			contentAlignment = Alignment.BottomCenter
		) {
			val state = rememberTransformableState { zoomChange, panChange, _ ->
				if (drawMode != DrawMode.Touch) return@rememberTransformableState
				scale = (scale * zoomChange).coerceIn(1f, 5f)

				val extraWidth = (scale - 1) * constraints.maxWidth
				val extraHeight = (scale - 1) * constraints.maxHeight

				val maxX = extraWidth / 2
				val maxY = extraHeight / 2

				offset = Offset(
					x = (offset.x + scale * panChange.x).coerceIn(-maxX, maxX),
					y = (offset.y + scale * panChange.y).coerceIn(-maxY, maxY)
				)
			}

			AndroidView(
				factory = {
					ComposeView(context).apply {
						setContent {

							LaunchedEffect(drawController) {
								drawController.trackBitmaps(this@apply, this, onCaptured = { imageBitmap, error ->
									imageBitmap?.let { bitmap ->
										when (exportOption) {
											ExportOption.PNG -> exportSketch(bitmap.asAndroidBitmap())
											ExportOption.PDF -> exportSketchAsPdf(bitmap.asAndroidBitmap())
										}
									}
                                })
							}

							Canvas(
								modifier = Modifier
									.fillMaxSize()
									.background(Color.White)
									.graphicsLayer {
										scaleX = scale
										scaleY = scale
										translationX = offset.x
										translationY = offset.y
									}
									.transformable(state)
									.pointerInput(true) {
										if (drawMode == DrawMode.Touch) return@pointerInput
										detectDragGestures { change, dragAmount ->
											change.consume()
											val eraseMode = drawMode == DrawMode.Erase
											val path = PathProperties(
												color = when (drawMode) {
													DrawMode.Erase -> Color.White
													DrawMode.Draw -> color
													else -> Color.Transparent
												},
												eraseMode = eraseMode,
												start = change.position - dragAmount,
												end = change.position,
												strokeWidth = pencilSize
											)

											paths += path
											absolutePaths.clear()
											absolutePaths.addAll(paths)

											viewModel.updatePathInDb(paths)
										}
										//update remote db with new paths drawn
										viewModel.updatePathInDb(paths)
									}
							) {
								paths.forEach { path ->
									drawLine(
										color = path.color,
										start = path.start,
										end = path.end,
										strokeWidth = path.strokeWidth,
										cap = StrokeCap.Round
									)
								}

								viewModel.updatePathInDb(paths)
							}
						}
					}
				},
				modifier = Modifier.fillMaxSize()
			)
		}

		if (openNameSketchDialog.value) {
			NameSketchDialog(
				onNamed = { name -> save(name) },
				onDismiss = { openNameSketchDialog.value = false }
			)
		}

		if (openSavePromptDialog.value) {
			val sketchIsNew = sketch == null
			SavePromptDialog(
				sketchIsNew = sketchIsNew,
				onSave = {
					if (sketchIsNew) {
						openNameSketchDialog.value = true
					} else {
						save(null)
					}
				},
				onDiscard = { navigate(Screens.Back) },
				onDismiss = { openSavePromptDialog.value = false }
			)
		}

		DisposableEffect(Unit) {
			onDispose { actions(SketchPadActions.SketchClosed) }
		}

		// onBackPress, if canvas has new lines drawn, prompt user to save sketch or changes
		if (paths.isNotEmpty() && paths != sketch?.pathList) {
			BackHandler { openSavePromptDialog.value = true }
		}
	}
}