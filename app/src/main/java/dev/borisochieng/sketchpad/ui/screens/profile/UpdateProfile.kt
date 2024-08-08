package dev.borisochieng.sketchpad.ui.screens.profile

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
import androidx.compose.material.icons.rounded.AccountBox
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import dev.borisochieng.sketchpad.auth.presentation.viewmodels.AuthViewModel
import dev.borisochieng.sketchpad.ui.navigation.Screens
import dev.borisochieng.sketchpad.ui.theme.AppTheme
import dev.borisochieng.sketchpad.ui.theme.AppTypography
import dev.borisochieng.sketchpad.ui.theme.lightScheme
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
fun UpdateProfileScreen(navigate: (Screens) -> Unit, viewModel: AuthViewModel = koinViewModel()) {

    var photoUri: Uri? by remember {
        mutableStateOf(null)
    }

    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()) { uri ->
            photoUri = uri

            photoUri?.let { viewModel.uploadImage(it) }
        }

    var username by remember {
        mutableStateOf("")
    }

    val uiState by viewModel.uiState.collectAsState()

    val snackBarHostState = remember {
        SnackbarHostState()
    }

    LaunchedEffect(uiState) {
        viewModel.uiEvent.collectLatest {
            snackBarHostState.showSnackbar(uiState.error)
        }

    }

    LaunchedEffect(uiState) {
        viewModel.uiEvent.collectLatest {
            snackBarHostState.showSnackbar(it.toString())
        }

    }


    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState)}

    ) { innerPadding ->


        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .clip(CircleShape)
                    .size(150.dp)
                    .clickable {
                        launcher.launch(
                            PickVisualMediaRequest(
                                mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }
                    .border(width = 1.dp, color = lightScheme.primary),
            ) {
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

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                text = "Username",
                style = AppTypography.labelLarge,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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
                    if(uiState.error.isNotEmpty() && photoUri != null) {
                    viewModel.updateProfile(imageUrl = photoUri!!, username = username )
                    navigate(Screens.ProfileScreen)}
                },
                enabled = username.isNotEmpty()
            ) {


                Text(
                    text = "Update Profile",
                    style = AppTypography.labelLarge,
                    color = lightScheme.onPrimary,
                )
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