package dev.borisochieng.artio.ui.screens.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult.ActionPerformed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import dev.borisochieng.database.database.Sketch
import dev.borisochieng.artio.ui.components.Header
import dev.borisochieng.artio.ui.components.HomeTopBar
import dev.borisochieng.artio.ui.navigation.Screens
import dev.borisochieng.artio.ui.screens.dialog.ItemMenuSheet
import dev.borisochieng.artio.utils.ShimmerBoxItem
import dev.borisochieng.artio.utils.VOID_ID
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    bottomPadding: Dp,
    uiState: HomeUiState,
    actions: (HomeActions) -> Unit,
    navigate: (Screens) -> Unit
) {
    val (localSketches, remoteSketches, userIsLoggedIn, isLoading, feedback, fetchedFromRemoteDb, user) = uiState
    val selectedSketch = remember { mutableStateOf<dev.borisochieng.database.database.Sketch?>(null) }
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { actions(HomeActions.CheckIfUserIsLogged) }

    Scaffold(
        topBar = {
            user?.let {
                Header(it) { navigate(Screens.ProfileScreen) }
            } ?: HomeTopBar()
        },
        snackbarHost = { SnackbarHost(snackBarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navigate(
                        Screens.SketchPad(
                            sketchId = VOID_ID,
                            userId = FirebaseAuth.getInstance().uid ?: VOID_ID,
                            isFromCollabUrl = false
                        )
                    )
                },
                modifier = Modifier.padding(bottom = bottomPadding),
                containerColor = colorScheme.primary,
                contentColor = colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Rounded.Brush,
                    contentDescription = "New sketch"
                )
            }
        }
    ) { paddingValues ->
        PullRefreshContainer(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            pullEnabled = !isLoading && fetchedFromRemoteDb && userIsLoggedIn,
            onRefresh = { actions(HomeActions.Refresh) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .animateContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isLoading && !fetchedFromRemoteDb) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 10.dp, end = 10.dp, bottom = 2.dp)
                            .clip(MaterialTheme.shapes.medium)
                    )
                }
                when {
                    isLoading -> LoadingScreen()
                    localSketches.isNotEmpty() && !isLoading -> {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(150.dp),
                            modifier = Modifier.padding(start = 10.dp),
                            contentPadding = PaddingValues(bottom = 100.dp)
                        ) {
                            items(localSketches.size) { index ->
                                val sketch = localSketches[index]
                                SketchPoster(
                                    sketch = sketch,
                                    modifier = Modifier.animateItemPlacement(),
                                    onClick = {
                                        navigate(
                                            Screens.SketchPad(
                                                sketchId = sketch.id,
                                                userId = FirebaseAuth.getInstance().currentUser?.uid
                                                    ?: VOID_ID,
                                                isFromCollabUrl = sketch.isBackedUp
                                            )
                                        )
                                    },
                                    onMenuClicked = { selectedSketch.value = it }
                                )
                            }
                        }
                    }

                    else -> {
                        EmptyScreen(Modifier.padding(bottom = bottomPadding))
                    }
                }
            }
        }

        if (selectedSketch.value != null) {
            ItemMenuSheet(
                sketch = selectedSketch.value!!,
                backedUp = selectedSketch.value in remoteSketches,
                userIsLoggedIn = userIsLoggedIn,
                action = actions,
                onPromptToLogin = {
                    scope.launch {
                        val action = snackBarHostState.showSnackbar(
                            message = "Sign up to avail backup feature",
                            actionLabel = "SIGN UP", duration = SnackbarDuration.Short
                        )
                        if (action != ActionPerformed) return@launch
                        navigate(Screens.LoginScreen)
                    }
                },
                onDismiss = { selectedSketch.value = null }
            )
        }
    }

    LaunchedEffect(feedback) {
        if (feedback == null) return@LaunchedEffect
        scope.launch { snackBarHostState.showSnackbar(feedback) }
    }

    DisposableEffect(Unit) {
        onDispose { actions(HomeActions.ClearFeedback) }
    }
}

@Composable
private fun LoadingScreen(
    modifier: Modifier = Modifier,
    isLoading: Boolean = true
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(150.dp),
        modifier = modifier.padding(start = 10.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        items(10) {
            ShimmerBoxItem(isLoading)
        }
    }
}

@Composable
private fun EmptyScreen(modifier: Modifier = Modifier) {
    val displayText = "No drawings saved"

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(30.dp)
            .alpha(0.7f),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Rounded.History,
            contentDescription = displayText,
            modifier = Modifier
                .padding(bottom = 20.dp)
                .size(100.dp)
        )
        Text(
            text = displayText,
            fontSize = 24.sp,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}
