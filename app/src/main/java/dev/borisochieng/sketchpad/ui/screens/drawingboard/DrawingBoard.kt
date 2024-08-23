package dev.borisochieng.sketchpad.ui.screens.drawingboard

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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import dev.borisochieng.sketchpad.ui.screens.drawingboard.chat.ChatDialog
import dev.borisochieng.sketchpad.ui.screens.drawingboard.components.MovableTextBox
import dev.borisochieng.sketchpad.ui.screens.drawingboard.components.PaletteMenu
import dev.borisochieng.sketchpad.ui.screens.drawingboard.components.PaletteTopBar
import dev.borisochieng.sketchpad.ui.screens.drawingboard.data.CanvasUiEvents
import dev.borisochieng.sketchpad.ui.screens.drawingboard.data.CanvasUiState
import dev.borisochieng.sketchpad.ui.screens.drawingboard.data.PathProperties
import dev.borisochieng.sketchpad.ui.screens.drawingboard.data.SketchPadActions
import dev.borisochieng.sketchpad.ui.screens.drawingboard.data.TextProperties
import dev.borisochieng.sketchpad.ui.screens.drawingboard.utils.DrawMode
import dev.borisochieng.sketchpad.ui.screens.drawingboard.utils.ExportOption
import dev.borisochieng.sketchpad.ui.screens.drawingboard.utils.rememberDrawController
import dev.borisochieng.sketchpad.utils.VOID_ID
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.util.UUID.randomUUID

@Composable
fun DrawingBoard(
    uiState: CanvasUiState, // passing the state in the way fixes state management issue
    exportSketch: (Bitmap) -> Unit,
    actions: (SketchPadActions) -> Unit,
    exportSketchAsPdf: (Bitmap) -> Unit,
    navigate: (Screens) -> Unit,
    onBroadCastUrl: (Uri) -> Unit,
    viewModel: SketchPadViewModel = koinViewModel(),
    boardId: String,
    userId: String,
    isFromCollabUrl: Boolean
) {
    val chatVisible = remember { mutableStateOf(false) }
    val chatEnabled = remember { mutableStateOf(false) }
    val (userIsLoggedIn, _, sketchIsBackedUp, _, sketch, collabUrl) = uiState
//    var currentTextInput by remember { mutableStateOf(TextInput()) }
    val drawController = rememberDrawController()
    var drawMode by remember { mutableStateOf(DrawMode.Draw) }
    var exportOption by remember { mutableStateOf(ExportOption.PNG) }

    val absolutePaths = remember { mutableStateListOf<PathProperties>() }
    var paths by remember { mutableStateOf<List<PathProperties>>(emptyList()) }

    val absoluteTexts = remember { mutableStateListOf<TextProperties>() }
    var texts by remember { mutableStateOf<List<TextProperties>>(emptyList()) }
    val showNewTextBox = remember { mutableStateOf(false) }

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
            SketchPadActions.UpdateSketch(paths, texts)
        } else {
            openNameSketchDialog.value = false
            val newSketch = Sketch(name = name, pathList = paths, textList = texts)
            SketchPadActions.SaveSketch(newSketch)
        }
        actions(action)
        Toast.makeText(context, "Sketch saved", Toast.LENGTH_SHORT).show()
        navigate(Screens.Back)
    }

    val addTextToPaths: (String) -> Unit = { textId ->
        val textPath = PathProperties(id = textId, textMode = true)
        paths += textPath
        absolutePaths.clear()
        absolutePaths.addAll(paths)
    }
    val removeTextFromPaths: (String) -> Unit = { textId ->
        val existingTextPath = paths.first { it.id == textId }
        paths -= existingTextPath
        absolutePaths -= existingTextPath
        // add it to undo/redo history
        absolutePaths.add(paths.size, existingTextPath)
    }

    val uiEvents by viewModel.uiEvents.collectAsState(initial = null)


    //listen for path changes
    LaunchedEffect(uiState.paths) {
        if (!userIsLoggedIn) return@LaunchedEffect
        //update local paths with collaborative paths
        absolutePaths.clear()
        paths = uiState.paths
        absolutePaths.addAll(paths)
    }

//    //update paths in db
//    LaunchedEffect(paths) {
//        if (!userIsLoggedIn) {
//            return@LaunchedEffect
//        } else if (uiState.sketchIsBackedUp) {
//            delay(300)
//            pathsBuffer.addAll(paths)
//            viewModel.updatePathInDb(paths = pathsBuffer, userId = userId, boardId = boardId)
//        }
//    }

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
                unUndoClicked = {
                    if (paths.last().textMode && texts.isNotEmpty()) {
                        texts -= texts.last()
                    }
                    paths -= paths.last()
                },
                unRedoClicked = {
                    val nextPath = absolutePaths[paths.size]
                    if (nextPath.textMode && texts.size != absoluteTexts.size) {
                        texts += absoluteTexts[texts.size]
                    }
                    paths += nextPath
                },
                onExportClicked = { drawController.saveBitmap() },
                onBroadCastUrl = {
                    if (userIsLoggedIn) {
                        sketch?.let {
                            if (!it.isBackedUp || collabUrl == null) {
                                scope.launch { snackbarHostState.showSnackbar("Sketch is not backed up yet") }
                                return@PaletteTopBar
                            }
                            //isCollabUrlShared = true
                            onBroadCastUrl(collabUrl)
                        }
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
                }
            )
        },
        bottomBar = {
            PaletteMenu(
                drawMode = drawMode,
                selectedColor = color,
                pencilSize = pencilSize,
                onColorChanged = { color = it },
                onSizeChanged = { pencilSize = it },
                onDrawModeChanged = {
                    drawMode = it
                    if (it == DrawMode.Text) showNewTextBox.value = true
                },
                chatEnabled = { chatEnabled.value = !chatEnabled.value }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White,
        floatingActionButton = {
            if (userIsLoggedIn && isFromCollabUrl) {
                FloatingActionButton(
                    onClick = {
                        chatVisible.value = true
                    },
                    content = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "Chat"
                        )
                    }
                )
            }
        }
    ) { paddingValues ->
        LaunchedEffect(sketch) {
            if (sketch == null) return@LaunchedEffect
            // paths
            absolutePaths.clear(); paths = emptyList()
            absolutePaths.addAll(sketch.pathList)
            paths = sketch.pathList
            // texts
            absoluteTexts.clear(); texts = emptyList()
            absoluteTexts.addAll(sketch.textList)
            texts = sketch.textList
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
                                drawController.trackBitmaps(
                                    this@apply, this,
                                    onCaptured = { imageBitmap, _ ->
                                        imageBitmap?.let { bitmap ->
                                            when (exportOption) {
                                                ExportOption.PNG -> exportSketch(bitmap.asAndroidBitmap())
                                                ExportOption.PDF -> exportSketchAsPdf(bitmap.asAndroidBitmap())
                                            }
                                        }
                                    }
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        translationX = offset.x
                                        translationY = offset.y
                                    }
                                    .transformable(state)
                            ) {
                                Canvas(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.White)
                                        .pointerInput(true) {
                                            if (drawMode == DrawMode.Touch) return@pointerInput
                                            detectDragGestures { change, dragAmount ->
                                                change.consume()
                                                val path = PathProperties(
                                                    id = randomUUID().toString(), //generate id for each new path
                                                    color = when (drawMode) {
                                                        DrawMode.Erase -> Color.White
                                                        DrawMode.Draw -> color
                                                        else -> Color.Transparent
                                                    },
                                                    textMode = false,
                                                    start = change.position - dragAmount,
                                                    end = change.position,
                                                    strokeWidth = pencilSize
                                                )

                                                //update paths locally
                                                paths += path
                                                absolutePaths.clear()
                                                absolutePaths.addAll(paths)

                                                //send path updates to Firebase for collab
                                                if (isFromCollabUrl && userId != VOID_ID && boardId != VOID_ID) {
                                                    scope.launch {
                                                        viewModel.updatePathInDb(
                                                            paths = paths,
                                                            userId = userId,
                                                            boardId = boardId
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                ) {
                                    paths
                                        .filterNot { it.textMode }
                                        .forEach { path ->
                                            drawLine(
                                                color = path.color,
                                                start = path.start,
                                                end = path.end,
                                                strokeWidth = path.strokeWidth,
                                                cap = StrokeCap.Round
                                            )
                                        }
                                }

                                texts.forEach { property ->
                                    MovableTextBox(
                                        properties = property,
                                        active = false,
                                        onRemove = { texts -= it; removeTextFromPaths(it.id) },
                                        onUpdate = { text ->
                                            removeTextFromPaths(text.id)
                                            val existingText = texts.first { it.id == text.id }
                                            texts -= existingText
                                            texts += text
                                            addTextToPaths(text.id)
                                        }
                                    )
                                }
                                if (showNewTextBox.value) {
                                    MovableTextBox(
                                        active = true,
                                        onRemove = {
                                            showNewTextBox.value = false
                                            drawMode = DrawMode.Draw
                                        },
                                        onFinish = {
                                            texts += it
                                            absoluteTexts.clear()
                                            absoluteTexts.addAll(texts)
                                            addTextToPaths(it.id)
                                            showNewTextBox.value = false
                                            drawMode = DrawMode.Draw
                                        }
                                    )
                                }
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
            onDispose {
                actions(SketchPadActions.SketchClosed)
                absolutePaths.clear()
                absoluteTexts.clear()
                paths = emptyList()
                texts = emptyList()
            }
        }

        // onBackPress, if canvas has new lines drawn or text written, prompt user to save sketch or changes
        // don't show dialog if in collab mode since update has already been done
        if (!isFromCollabUrl && (paths.isNotEmpty() && paths != sketch?.pathList) ||
            (texts.isNotEmpty() && texts != sketch?.textList)
        ) {
            BackHandler { openSavePromptDialog.value = true }
        }
    }

    if (chatVisible.value) {
        ChatDialog(
            projectId = boardId,
            viewModel = viewModel,
            onCancel = { chatVisible.value = false },
        )
    }
}

//data class TextInput(
//    val text: String = "",
//    val position: Offset = Offset.Zero,
//    val fontSize: Int = 16,
//    val fontColor: Color = Color.Black,
//    val fontStyle: FontStyle = FontStyle.Normal,
//    val fontWeight: FontWeight = FontWeight.Normal,
//    val fontFamily: FontFamily = FontFamily.Default
//)
