package dev.borisochieng.sketchpad.auth.presentation.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountBox
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.borisochieng.sketchpad.auth.presentation.state.UiEvent
import dev.borisochieng.sketchpad.auth.presentation.viewmodels.AuthViewModel
import dev.borisochieng.sketchpad.ui.navigation.Screens
import dev.borisochieng.sketchpad.ui.theme.AppTheme
import dev.borisochieng.sketchpad.ui.theme.AppTypography
import dev.borisochieng.sketchpad.ui.theme.lightScheme
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateProfileScreen(navigate: (Screens) -> Unit, viewModel: AuthViewModel = koinViewModel()) {

    val uiState by viewModel.uiState.collectAsState()
    val uiEvent by viewModel.uiEvent.collectAsState(initial = null)

    var photoUri: Uri? by remember {
        mutableStateOf(null)
    }
    LaunchedEffect(uiState.user?.imageUrl) {
        photoUri = uiState.user?.imageUrl
    }

    val imagePicker =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()) { uri ->
            photoUri = uri
        }

    var username by remember {
        mutableStateOf("")
    }

    val snackBarHostState = remember {
        SnackbarHostState()
    }
    var showProgressIndicator by remember {
        mutableStateOf(false)
    }

    showProgressIndicator = uiState.isLoading



    LaunchedEffect(uiEvent) {
        uiEvent?.let { event ->
            when (event) {
                is UiEvent.SnackBarEvent -> {
                    // Showing Snackbar with the message
                    snackBarHostState.showSnackbar(event.message)
                }
                // Handle other events if any
            }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Update profile",
                        style = AppTypography.headlineMedium,
                        modifier = Modifier.padding(8.dp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navigate(Screens.Back)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Navigate up"
                        )
                    }
                })
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }

    ) { innerPadding ->


        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .clip(CircleShape)
                    .size(150.dp)
                    .clickable {
                        imagePicker.launch(
                            PickVisualMediaRequest(
                                mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }
                    .border(
                        width = 1.dp,
                        color = lightScheme.primary,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (photoUri == null) {

                    Text(
                        text = "Select profile photo",
                        style = AppTypography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(4.dp)
                            .wrapContentHeight()
                            .wrapContentHeight()
                    )

                } else {
                    AsyncImage(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .align(Alignment.Center)
                            .size(150.dp),
                        contentDescription = "Avatar",
                        model = ImageRequest
                            .Builder(LocalContext.current)
                            .data(photoUri)
                            .build(),
                        contentScale = ContentScale.Crop
                    )
                }

            }

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                text = "Username",
                style = AppTypography.labelLarge,
                color = Color.Gray
            )

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                value = username,
                onValueChange = {
                    username = it
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                placeholder = {
                    Text(
                        text = "Enter a your username",
                        style = AppTypography.labelSmall,
                        color = Color.LightGray
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.AccountBox,
                        contentDescription = "Email",
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = lightScheme.primary,
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    cursorColor = lightScheme.primary
                ),
                shape = RoundedCornerShape(50.dp)
            )


            Spacer(modifier = Modifier.weight(1f))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                onClick = {
                    if (uiState.error.isEmpty() && photoUri != null) {
                        viewModel.uploadImageAndUpdateProfile(uri = photoUri!!, username = username)
                    }
                },
                enabled = username.isNotEmpty() && photoUri != null
            ) {

                if (showProgressIndicator) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = lightScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {

                    Text(
                        text = "Update Profile",
                        style = AppTypography.labelLarge,
                        color = lightScheme.onPrimary,
                    )
                }
            }


        }
    }

}


@Preview(showBackground = true)
@Composable
fun UpdateProfileScreenPreview() {
    AppTheme {
        UpdateProfileScreen({}, viewModel())
    }
}