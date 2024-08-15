package dev.borisochieng.sketchpad.auth.presentation.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.outlinedButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.borisochieng.sketchpad.auth.presentation.viewmodels.AuthViewModel
import dev.borisochieng.sketchpad.ui.navigation.Screens
import dev.borisochieng.sketchpad.ui.theme.AppTypography
import dev.borisochieng.sketchpad.ui.theme.lightScheme
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    bottomPadding: Dp,
    navigate: (Screens) -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.padding(bottom = bottomPadding),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        style = AppTypography.headlineMedium,
                        modifier = Modifier.padding(8.dp)
                    )
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier.
                    padding(16.dp)
                    .clip(CircleShape)
                    .size(150.dp)
                    .border(
                        width = 1.dp,
                        color = lightScheme.primary,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (uiState.user?.imageUrl == null) {
                    Text(
                        text = "No profile photo",
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
                            .data(uiState.user!!.imageUrl)
                            .build(),
                        contentScale = ContentScale.Crop
                    )
                }

            }

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                text = "Username",
                style = AppTypography.labelLarge,
                color = Color.Gray
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                text = uiState.user?.displayName ?: "No username found",
                style = AppTypography.bodyLarge,
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                text = "Email",
                style = AppTypography.labelLarge,
                color = Color.Gray
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                text = uiState.user?.email ?: "No email found",
                style = AppTypography.bodyLarge,
            )

            Spacer(modifier = Modifier.weight(1f))

            if (uiState.isLoggedIn) {
                OutlinedButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    onClick = { navigate(Screens.UpdateProfileScreen) },
                    enabled = !uiState.isLoading,
                    colors = outlinedButtonColors(contentColor = colorScheme.primary)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            text = "Update Profile",
                            style = AppTypography.labelLarge
                        )

                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = "Update profile"
                        )
                    }
                }
                OutlinedButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    onClick = { viewModel.logoutUser() },
                    enabled = !uiState.isLoading
                ) {
                    Text(
                        text = "Logout",
                        style = AppTypography.labelLarge,
                        color = lightScheme.primary,
                    )
                }
            } else {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    onClick = { navigate(Screens.OnBoardingScreen) }
                ) {
                    Text("Login", style = AppTypography.labelLarge)
                }
            }
        }
    }
}
