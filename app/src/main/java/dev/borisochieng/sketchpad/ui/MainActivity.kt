package dev.borisochieng.sketchpad.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import dev.borisochieng.sketchpad.toby.Root
import dev.borisochieng.sketchpad.toby.data.activityChooser
import dev.borisochieng.sketchpad.toby.data.checkAndAskPermission
import dev.borisochieng.sketchpad.toby.data.saveImage
import dev.borisochieng.sketchpad.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_SketchPad)
        super.onCreate(savedInstanceState)
        //   enableEdgeToEdge()
        setContent {
            Root(window = window) {
                val navController = rememberNavController()
                val navActions = NavActions(navController)
                AppTheme {
                    Scaffold(
                        bottomBar = { NavBar(navController) },
                    ) { paddingValues ->
                        AppRoute(
                            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                            navActions = navActions,
                            navController = navController,
                            paddingValues = paddingValues,
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