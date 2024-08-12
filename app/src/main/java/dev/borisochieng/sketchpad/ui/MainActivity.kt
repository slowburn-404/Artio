package dev.borisochieng.sketchpad.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import dev.borisochieng.sketchpad.R
import dev.borisochieng.sketchpad.ui.components.NavBar
import dev.borisochieng.sketchpad.ui.navigation.AppRoute
import dev.borisochieng.sketchpad.ui.navigation.NavActions
import dev.borisochieng.sketchpad.ui.screens.drawingboard.Root
import dev.borisochieng.sketchpad.ui.screens.drawingboard.data.activityChooser
import dev.borisochieng.sketchpad.ui.screens.drawingboard.data.checkAndAskPermission
import dev.borisochieng.sketchpad.ui.screens.drawingboard.data.saveImage
import dev.borisochieng.sketchpad.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_SketchPad)
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContent {
            Root(window = window) {
                val navController = rememberNavController()
                val navActions = NavActions(navController)
                AppTheme {
                    Scaffold(
                        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                        bottomBar = { NavBar(navController) }
                    ) { innerPadding ->
                        AppRoute(
                            paddingValues = innerPadding,
                            navActions = navActions,
                            navController = navController,
                            saveImage = {
                                checkAndAskPermission {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        val uri = saveImage(it)
                                        withContext(Dispatchers.Main) {
                                            startActivity(activityChooser(uri))
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}