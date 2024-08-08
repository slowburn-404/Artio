package dev.borisochieng.sketchpad.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.borisochieng.sketchpad.ui.navigation.Screens
import dev.borisochieng.sketchpad.ui.theme.AppTypography
import dev.borisochieng.sketchpad.ui.theme.lightScheme
import java.util.UUID.randomUUID
import kotlin.math.roundToInt

@Composable
fun HomeScreen(navigate: (Screens) -> Unit) {

    Scaffold(
        topBar = {
            Text(
                modifier = Modifier.padding(8.dp),
                text = "Canvas management",
                style = AppTypography.headlineMedium,
                textAlign = TextAlign.Center
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navigate(Screens.SketchPad((Math.random() * 10).roundToInt()))
                },
                containerColor = lightScheme.primary,
                contentColor = lightScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Rounded.Brush,
                    contentDescription = "Draw"
                )

            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding),
        )

        //TODO(Display a list of canvases)
    }
}