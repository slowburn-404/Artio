package dev.borisochieng.artio.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import dev.borisochieng.artio.ui.theme.AppTheme

@Composable
fun Header() {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

    }

}

@Preview(showBackground = true)
@Composable
fun HeaderPreview() {
    AppTheme {
        Header()
    }
}