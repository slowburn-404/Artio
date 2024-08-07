package dev.borisochieng.sketchpad.auth.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBox
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Password
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.borisochieng.sketchpad.auth.presentation.SignUpViewModel
import dev.borisochieng.sketchpad.ui.navigation.Screens
import dev.borisochieng.sketchpad.ui.theme.AppTheme
import dev.borisochieng.sketchpad.ui.theme.AppTypography
import dev.borisochieng.sketchpad.ui.theme.lightScheme
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignUpScreen(
    navigate: (Screens) -> Unit,
    viewModel: SignUpViewModel = koinViewModel()
) {
    val title = buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                color = lightScheme.onBackground
            )
        ) {
            append("Sketch")
        }
        withStyle(
            style = SpanStyle(
                color = lightScheme.primary
            )
        ) {
            append("Pad")
        }
    }

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var confirmPassword by remember {
        mutableStateOf("")
    }
    var username by remember {
        mutableStateOf("")
    }
    var showProgressIndicator by remember {
        mutableStateOf(false)
    }
    val snackBarHostState = remember { SnackbarHostState() }
    val uiState  = viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest{ message ->
            snackBarHostState.showSnackbar(message = message.toString())
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState)}
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                textAlign = TextAlign.Center,
                style = AppTypography.headlineLarge,
                modifier = Modifier
                    .wrapContentWidth()
            )

            Text(
                text = "Sign Up",
                textAlign = TextAlign.Center,
                style = AppTypography.titleLarge,
                modifier = Modifier
                    .wrapContentWidth()
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = username,
                onValueChange = {
                    username = it
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = email,
                onValueChange = {
                    email = it
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
                shape = RoundedCornerShape(50.dp)
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = password,
                onValueChange = {
                    password = it
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                placeholder = {
                    Text(
                        text = "Enter you password",
                        style = AppTypography.labelSmall,
                        color = Color.LightGray
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = "Email",
                    )
                },
                trailingIcon = {

                },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = lightScheme.primary,
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    cursorColor = lightScheme.primary
                ),
                shape = RoundedCornerShape(50.dp)
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                placeholder = {
                    Text(
                        text = "Confirm your password",
                        style = AppTypography.labelSmall,
                        color = Color.LightGray
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = lightScheme.primary,
                    contentColor = lightScheme.onPrimary
                ),
                onClick = {
                    if(uiState.value.isLoading) {
                        showProgressIndicator = true
                        viewModel.signUpUser(email, password)
                        navigate(Screens.HomeScreenScreen)
                    }
                }) {

                if(showProgressIndicator) {
                    CircularProgressIndicator()
                } else {
                    Text(
                        text = "Sign Up",
                        style = AppTypography.labelLarge
                    )
                }

            }

            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                onClick = {
                }) {

                Text(
                    text = "Sign In",
                    style = AppTypography.labelLarge,
                    color = lightScheme.primary,
                )

            }

            Text(
                modifier = Modifier.padding(4.dp),
                text = "Continue as Guest",
                style = AppTypography.labelLarge,
                textDecoration = TextDecoration.Underline
            )
        }

    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    AppTheme {
        SignUpScreen({}, viewModel() )
    }
}