package dev.borisochieng.artio.ui.screens.auth

import android.util.Patterns
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import dev.borisochieng.artio.R
import dev.borisochieng.artio.ui.navigation.Screens
import dev.borisochieng.artio.ui.screens.auth.state.UiEvent
import dev.borisochieng.artio.ui.theme.AppTypography
import dev.borisochieng.artio.ui.theme.lightScheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignUpScreen(
    navigate: (Screens) -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
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
    val scrollState = rememberScrollState()
    //val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val uiEvent by viewModel.uiEvent.collectAsState(initial = null)
    val uiState by viewModel.uiState.collectAsState()
    showProgressIndicator = uiState.isLoading


    //listen for error or success messages
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

    //navigate if sign up is successful
    LaunchedEffect(uiState.error) {
        if (uiState.error.isEmpty() && !uiState.isLoading && uiState.isLoggedIn) {
            navigate(Screens.HomeScreen)
        }
    }

    //navigate to home screen if user is already logged in
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            navigate(Screens.HomeScreen)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(innerPadding)
                .padding(16.dp)
                .animateContentSize()
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Image(
                painterResource(id = R.drawable.ic_logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(100.dp)
            )

            Text(
                text = "Artio",
                textAlign = TextAlign.Center,
                style = AppTypography.displayMedium,
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
                    viewModel.signUpUser(email, password)
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
    return Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
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