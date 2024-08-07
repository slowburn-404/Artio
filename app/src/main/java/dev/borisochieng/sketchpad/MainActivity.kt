package dev.borisochieng.sketchpad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import dev.borisochieng.sketchpad.ui.components.NavBar
import dev.borisochieng.sketchpad.ui.navigation.AppRoute
import dev.borisochieng.sketchpad.ui.navigation.NavActions
import dev.borisochieng.sketchpad.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_SketchPad)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val navActions = NavActions(navController)

            AppTheme {
                Scaffold(
                    containerColor = colorScheme.background,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars),
                    bottomBar = { NavBar(navController) },
                ) { paddingValues ->
                    AppRoute(
                        navActions = navActions,
                        navController = navController,
                        paddingValues = paddingValues,
                    )
                }
            }
        }
    }
}