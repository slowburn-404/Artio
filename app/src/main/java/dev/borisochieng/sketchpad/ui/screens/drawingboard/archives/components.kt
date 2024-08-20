package dev.borisochieng.sketchpad.ui.screens.drawingboard.archives

import android.content.Intent
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import dev.borisochieng.sketchpad.R

@Composable
fun ShareButton(uriToShare: String?) {
    val context = LocalContext.current
    IconButton(onClick = {
        uriToShare?.let {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, it)
                type = "text/html"
            }
            context.startActivity(Intent.createChooser(sendIntent, null))
        }
    },
        enabled = uriToShare != null
    ) {
        Icon(
            painter = painterResource(R.drawable.collaboration),
            contentDescription = "Export sketch",
            modifier = Modifier.scale(2f)
        )
    }


}