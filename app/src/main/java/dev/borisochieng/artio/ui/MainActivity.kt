package dev.borisochieng.artio.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import dev.borisochieng.artio.R
import dev.borisochieng.firebase.database.domain.DeepLinkData
import dev.borisochieng.artio.ui.components.NavBar
import dev.borisochieng.artio.ui.navigation.AppRoute
import dev.borisochieng.artio.ui.navigation.NavActions
import dev.borisochieng.artio.ui.navigation.Screens
import dev.borisochieng.artio.ui.screens.drawingboard.archives.Root
import dev.borisochieng.artio.ui.screens.drawingboard.utils.activityChooser
import dev.borisochieng.artio.ui.screens.drawingboard.utils.checkAndAskPermission
import dev.borisochieng.artio.ui.screens.drawingboard.utils.saveImage
import dev.borisochieng.artio.ui.screens.drawingboard.utils.savePdf
import dev.borisochieng.artio.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private lateinit var navActions: NavActions
    private var pendingDeepLink: dev.borisochieng.firebase.database.domain.DeepLinkData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Artio)
        super.onCreate(savedInstanceState)
        setContent {
            Root(window = window) {
                val navController = rememberNavController()
                navActions = NavActions(navController)

                handleDeepLink(intent)

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

            //handle deeplink after composition
            LaunchedEffect(Unit) {
                pendingDeepLink?.let { deepLink ->
                    navActions.navigate(
                        Screens.SketchPad(
                            sketchId = deepLink.boardId,
                            userId = deepLink.userId,
                            isFromCollabUrl = true
                        )
                    )

                    pendingDeepLink = null
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
        setIntent(intent) //update current intent with new one
        handleDeepLink(intent)
    }

    override fun onResume() {
        super.onResume()
        handleDeepLink(intent)
        if (::navActions.isInitialized && pendingDeepLink != null) {
            navActions.navigate(
                Screens.SketchPad(
                    sketchId = pendingDeepLink!!.boardId,
                    userId = pendingDeepLink!!.userId,
                    isFromCollabUrl = true
                )
            )
            pendingDeepLink = null
        }
    }

    private fun handleDeepLink(intent: Intent) {
        val data: Uri? = intent.data

        if (intent.action != Intent.ACTION_VIEW || data == null) return

        val userId = data.getQueryParameter("user_id")
        val boardId = data.getQueryParameter("board_id")

        if (userId != null && boardId != null) {
            pendingDeepLink = dev.borisochieng.firebase.database.domain.DeepLinkData(
                boardId = boardId,
                userId = userId
            )
        }


//        if(::navActions.isInitialized) {
//            navActions.navigate(
//                Screens.SketchPad(
//                    sketchId = boardId!!,
//                    isFromCollabUrl = true,
//                    userId = userId!!
//                )
//            )
//        }
        intent.data = null //prevent other intents from consuming the wrong data

        val message = "User id: $userId \n BoardId: $boardId"
        Log.d("DeepLink", message)
    }
}