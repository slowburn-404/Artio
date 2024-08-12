package dev.borisochieng.sketchpad.auth.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.borisochieng.sketchpad.auth.presentation.screens.PagerItems
import dev.borisochieng.sketchpad.ui.theme.AppTheme
import dev.borisochieng.sketchpad.ui.theme.AppTypography

@Composable
fun HorizontalPagerItem(
    modifier: Modifier = Modifier,
    pagerItem: PagerItems,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .size(200.dp)
                .fillMaxWidth(),
            painter = painterResource(pagerItem.imageId),
            contentDescription = "Draw"
        )

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
            text = pagerItem.title,
            style = AppTypography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
            text = pagerItem.body,
            style = AppTypography.bodyLarge,
            textAlign = TextAlign.Center
        )

    }
}

@Preview(showBackground = true)
@Composable
fun HorizontalPagerItemPreview() {
    AppTheme {
        HorizontalPagerItem(
            pagerItem = PagerItems.Draw
        )
    }
}