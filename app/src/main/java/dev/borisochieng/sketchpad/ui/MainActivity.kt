package dev.borisochieng.sketchpad.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import dev.borisochieng.sketchpad.ui.screens.drawingboard.data.savePdf
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
                            },
                            saveImageAsPdf = {
                                checkAndAskPermission {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        val pdfUri = savePdf(it)
                                        withContext(Dispatchers.Main) {
                                            startActivity(activityChooser(pdfUri))
                                        }
                                    }
                                }
                            },
                            broadCastUrl = { url ->
                                shareCollaborateUrl(url = url)
                            }
                        )
                    }
                }
            }
        }
    }

    private fun shareCollaborateUrl(url: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plan"
            putExtra(Intent.EXTRA_TEXT, url)
        }
        val chooser = Intent.createChooser(intent, "Invite collaborator via")
        startActivity(chooser)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent) {
        val action = intent.action
        val data: Uri? = intent.data

        if(action == Intent.ACTION_VIEW && data != null) {
            val url = data.toString()

            Log.d("DeepLink", "Received URL: $url")

            Toast.makeText(this, url, Toast.LENGTH_SHORT).show()
        }
    }
}