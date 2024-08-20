package dev.borisochieng.sketchpad.auth.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.RemoveRedEye
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import dev.borisochieng.sketchpad.auth.presentation.state.UiEvent
import dev.borisochieng.sketchpad.auth.presentation.viewmodels.AuthViewModel
import dev.borisochieng.sketchpad.ui.navigation.Screens
import dev.borisochieng.sketchpad.ui.theme.AppTypography
import dev.borisochieng.sketchpad.ui.theme.lightScheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = koinViewModel(),
    navigate: (Screens) -> Unit
) {
    val title = buildAnnotatedString {
        append("Sketch")

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
    var emailError by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var passwordError by remember {
        mutableStateOf("")
    }

    var showProgressIndicator by remember {
        mutableStateOf(false)
    }
    var isPasswordVisible by remember {
        mutableStateOf(false)
    }

    val snackBarHostState = remember { SnackbarHostState() }
    val uiEvent by viewModel.uiEvent.collectAsState(initial = null)
    val uiState = viewModel.uiState.collectAsState()
    showProgressIndicator = uiState.value.isLoading

    //listen for error messages
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

    //navigate when login is successful
    LaunchedEffect(uiState.value) {
        if (!uiState.value.isLoading && uiState.value.error.isEmpty() && uiState.value.isLoggedIn) {
            navigate(Screens.HomeScreen)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = title,
                textAlign = TextAlign.Center,
                style = AppTypography.displayMedium,
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(4.dp)
            )

            Text(
                text = "Login",
                textAlign = TextAlign.Center,
                style = AppTypography.headlineLarge,
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(4.dp)
            )

//            OutlinedTextField(
//                modifier = Modifier.fillMaxWidth(),
//                value = username,
//                onValueChange = {
//                    username = it
//                },
//                singleLine = true,
//                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
//                placeholder = {
//                    Text(
//                        text = "Enter a your username",
//                        style = AppTypography.labelSmall,
//                        color = Color.LightGray
//                    )
//                },
//                leadingIcon = {
//                    Icon(
//                        imageVector = Icons.Rounded.AccountBox,
//                        contentDescription = "Email",
//                    )
//                },
//                colors = TextFieldDefaults.colors(
//                    focusedIndicatorColor = lightScheme.primary,
//                    unfocusedContainerColor = Color.Transparent,
//                    focusedContainerColor = Color.Transparent,
//                    cursorColor = lightScheme.primary
//                ),
//                shape = RoundedCornerShape(50.dp)
//            )

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
                isError = emailError.isNotEmpty(),
                supportingText = {
                    if (emailError.isNotEmpty()) {
                        Text(
                            text = emailError,
                            color = lightScheme.error,
                            style = AppTypography.labelMedium
                        )
                    }
                }
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = password,
                onValueChange = {
                    password = it
                    passwordError = "Password".checkIfInputFieldsAreEmpty(password)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = Icons.Rounded.RemoveRedEye,
                            contentDescription = "Toggle Password Visibility"
                        )

                    }

                },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = lightScheme.primary,
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    cursorColor = lightScheme.primary
                ),
                shape = RoundedCornerShape(50.dp),
                isError = passwordError.isNotEmpty(),
                supportingText = {
                    if (passwordError.isNotEmpty()) {
                        Text(
                            text = passwordError,
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
                    if (!uiState.value.isLoading) {
                        showProgressIndicator = true
                        viewModel.loginUser(email, password)
                    }
                },
                enabled = enableLoginButton(email, password)
            ) {

                if (showProgressIndicator) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = lightScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Login",
                        style = AppTypography.labelLarge
                    )
                }

            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                Text(
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable(onClick = {
                            navigate(Screens.ResetPasswordScreen)
                        }),
                    text = "Forgot password?",
                    style = AppTypography.labelLarge,
                    textDecoration = TextDecoration.Underline
                )

                Text(
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable(onClick = { navigate(Screens.SignUpScreen) }),
                    text = "Create Account",
                    style = AppTypography.labelLarge,
                    textDecoration = TextDecoration.Underline
                )
            }
        }
    }

}

fun enableLoginButton(email: String, password: String): Boolean {
    return password.isNotEmpty() &&
            email.isNotEmpty() &&
            isValidEmail(email)
}
