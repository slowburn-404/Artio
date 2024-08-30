package dev.borisochieng.artio.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.borisochieng.artio.ui.navigation.Screens
import dev.borisochieng.artio.ui.screens.auth.state.UiEvent
import dev.borisochieng.artio.ui.theme.AppTypography
import dev.borisochieng.artio.ui.theme.lightScheme
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    viewModel: AuthViewModel = koinViewModel(),
    navigate: (Screens) -> Unit
) {

    val uiState by viewModel.uiState.collectAsState()
    val uiEvents by viewModel.uiEvent.collectAsState(initial = null)
    val snackBarHostState = remember { SnackbarHostState() }
    var showProgressBar by remember {
        mutableStateOf(false)
    }
    showProgressBar = uiState.isLoading

    var email by remember {
        mutableStateOf("")
    }
    var emailError by remember {
        mutableStateOf("")
    }

    LaunchedEffect(uiEvents) {
        viewModel.uiEvent.collectLatest { event ->
            uiEvents?.let {
                when (event) {
                    is UiEvent.SnackBarEvent -> {
                        snackBarHostState.showSnackbar(event.message)
                    }
                }
            }

        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Reset password",
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
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(
                text = "Enter you email",
                style = AppTypography.titleLarge,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .wrapContentWidth()
                    .wrapContentHeight()
            )

            Text(
                text = "Enter the email associated with your account and we'll send you instructions on how to reset your password",
                style = AppTypography.bodyLarge,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = email,
                onValueChange = {
                    email = it
                    emailError = if (it.isEmpty()) {
                        "Email cannot be empty"
                    } else if (!isValidEmail(it)) {
                        "Invalid email address"
                    } else {
                        ""
                    }
                },

                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                placeholder = {
                    Text(
                        text = "example@domain.com",
                        style = AppTypography.labelSmall,
                        color = Color.LightGray
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Email,
                        contentDescription = "Email",
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = lightScheme.primary,
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    cursorColor = lightScheme.primary
                ),
                shape = RoundedCornerShape(50.dp),
                isError = email.isNotEmpty(),
                supportingText = {
                    if (email.isNotEmpty()) {
                        Text(
                            text = emailError,
                            color = lightScheme.error,
                            style = AppTypography.labelMedium
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.weight(1f))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = lightScheme.primary,
                    contentColor = lightScheme.onPrimary
                ),
                onClick = {
                    viewModel.resetPassword(email = email)
                },
                enabled = email.isNotEmpty() && isValidEmail(email)
            ) {

                if (showProgressBar) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = lightScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Send email",
                        style = AppTypography.labelLarge
                    )
                }

            }


        }

    }
}