package dev.borisochieng.sketchpad.auth.presentation.screens

import android.util.Patterns
import androidx.activity.compose.BackHandler
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
import dev.borisochieng.sketchpad.auth.presentation.viewmodels.AuthViewModel
import dev.borisochieng.sketchpad.ui.navigation.Screens
import dev.borisochieng.sketchpad.ui.theme.AppTypography
import dev.borisochieng.sketchpad.ui.theme.lightScheme
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignUpScreen(
    navigate: (Screens) -> Unit,
    viewModel: AuthViewModel = koinViewModel()
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

    val loginText = buildAnnotatedString {
        append("Already have an account?")

        withStyle(
            style = SpanStyle(
                color = lightScheme.primary,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append(" Login")
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

    var confirmPassword by remember {
        mutableStateOf("")
    }

    var confirmPasswordError by remember {
        mutableStateOf("")
    }
    var username by remember {
        mutableStateOf("")
    }
    var showProgressIndicator by remember {
        mutableStateOf(false)
    }
    var isPasswordVisible by remember {
        mutableStateOf(false)
    }
    var isConfirmPasswordVisible by remember {
        mutableStateOf(false)
    }

    val snackBarHostState = remember { SnackbarHostState() }
    val uiState = viewModel.uiState.collectAsState()
    showProgressIndicator = uiState.value.isLoading

    //listen for error or success messages
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { message ->
            snackBarHostState.showSnackbar(message = message.toString())
        }
    }

    //navigate when sign up is successful
    LaunchedEffect(uiState.value) {
        if (!uiState.value.isLoading && uiState.value.error.isEmpty() && uiState.value.isLoggedIn) {
            navigate(Screens.HomeScreen)
        }
    }

    //navigate to home screen if user is already logged in
    LaunchedEffect(uiState.value.isLoggedIn) {
        if (uiState.value.isLoggedIn) {
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
                text = "Sign Up",
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

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    confirmPasswordError = if (it != password) {
                        "Password do not match"
                    } else {
                        "Password".checkIfInputFieldsAreEmpty(it)
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
                trailingIcon = {
                    IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                        Icon(
                            imageVector = Icons.Rounded.RemoveRedEye,
                            contentDescription = "Toggle password visibility"
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
                isError = confirmPasswordError.isNotEmpty(),
                supportingText = {
                    if (confirmPasswordError.isNotEmpty()) {
                        Text(
                            text = confirmPasswordError,
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
                        viewModel.signUpUser(email, password)
                    }
                },
                enabled = enableSignInButton(email, password, confirmPassword)
            ) {

                if (showProgressIndicator) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = lightScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Sign Up",
                        style = AppTypography.labelLarge
                    )
                }

            }

//            OutlinedButton(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 16.dp),
//                onClick = {
//                }) {
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.SpaceEvenly
//                ) {
//
//                    Icon(
//                        painter = painterResource(id = R.drawable.ic_google),
//                        contentDescription = "Google logo"
//                    )
//
//                    Text(
//                        text = "Sign Up With Google",
//                        style = AppTypography.labelLarge,
//                        color = lightScheme.primary,
//                    )
//                }
//
//            }

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
                            viewModel.saveLaunchStatus()
                            navigate(Screens.HomeScreen)
                        }),
                    text = "Continue as Guest",
                    style = AppTypography.labelLarge,
                    textDecoration = TextDecoration.Underline
                )

                Text(
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable(onClick = { navigate(Screens.LoginScreen) }),
                    text = loginText,
                    style = AppTypography.labelLarge,
                )
            }
        }

        BackHandler {
            viewModel.saveLaunchStatus()
            navigate(Screens.HomeScreen)
        }
    }
}

fun String.checkIfInputFieldsAreEmpty(input: String): String {
    return if (input.isEmpty()) "$this cannot be empty" else ""
}

fun isValidEmail(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

private fun checkIfPasswordsMatch(password: String, confirmPassword: String): Boolean {
    return password == confirmPassword
}


private fun enableSignInButton(email: String, password: String, confirmPassword: String): Boolean {
    return checkIfPasswordsMatch(password, confirmPassword) &&
            password.isNotEmpty() &&
            confirmPassword.isNotEmpty() &&
            email.isNotEmpty() &&
            isValidEmail(email)
}