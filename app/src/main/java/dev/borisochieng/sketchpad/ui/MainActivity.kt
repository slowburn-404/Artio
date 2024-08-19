package dev.borisochieng.sketchpad.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import dev.borisochieng.sketchpad.ui.navigation.Screens
import dev.borisochieng.sketchpad.ui.screens.drawingboard.Root
import dev.borisochieng.sketchpad.ui.screens.drawingboard.data.activityChooser
import dev.borisochieng.sketchpad.ui.screens.drawingboard.data.checkAndAskPermission
import dev.borisochieng.sketchpad.ui.screens.drawingboard.data.saveImage
import dev.borisochieng.sketchpad.ui.screens.drawingboard.data.savePdf
import dev.borisochieng.sketchpad.ui.theme.AppTheme
import dev.borisochieng.sketchpad.utils.VOID_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private lateinit var navActions: NavActions

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_SketchPad)
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContent {
            Root(window = window) {
                val navController = rememberNavController()
                navActions = NavActions(navController)

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

    private fun shareCollaborateUrl(url: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, url.toString())
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

        if(action != Intent.ACTION_VIEW || data == null) return

        val userId = data.getQueryParameter("user_id")
        val boardId = data.getQueryParameter("board_id")

        //TODO(navigate to drawing board)
        navActions.navigate(Screens.SketchPad(boardId ?: VOID_ID))

        val message = "User id: $userId \n BoardId: $boardId"
        Log.d("DeepLink", message)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}