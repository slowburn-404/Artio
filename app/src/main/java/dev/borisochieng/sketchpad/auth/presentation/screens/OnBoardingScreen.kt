package dev.borisochieng.sketchpad.auth.presentation.screens

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.borisochieng.sketchpad.R
import dev.borisochieng.sketchpad.auth.presentation.components.HorizontalPagerIndicator
import dev.borisochieng.sketchpad.auth.presentation.components.HorizontalPagerItem
import dev.borisochieng.sketchpad.ui.navigation.Screens
import dev.borisochieng.sketchpad.ui.theme.AppTheme
import dev.borisochieng.sketchpad.ui.theme.AppTypography
import dev.borisochieng.sketchpad.ui.theme.lightScheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnBoardingScreen(navigate: (Screens) -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            beyondBoundsPageCount = pagerState.pageCount
        ) { page ->
            val pagerItem = PagerItems.entries[page]
            HorizontalPagerItem(pagerItem = pagerItem)
        }

        HorizontalPagerIndicator(
            pagerState = pagerState
        )

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = lightScheme.primary,
                contentColor = lightScheme.onPrimary
            ),
            onClick = {
                if (pagerState.currentPage < 2) {
                    scope.launch {
                        pagerState.animateScrollToPage(
                            page = pagerState.currentPage + 1,
                            animationSpec = tween(500)
                        )
                    }
                } else {
                    navigate(Screens.SignUpScreen)
                }
            }
        ) {

            Text(
                text = if (pagerState.currentPage < 2) "Next" else "Get Started",
                style = AppTypography.labelLarge
            )

        }
    }
}

enum class PagerItems(
    @DrawableRes val imageId: Int,
    val title: String,
    val body: String
) {
    Draw(
        imageId = R.drawable.draw,
        title = "Draw on a canvas",
        body = "Bring your ideas to life"
    ),
    Collaborate(
        imageId = R.drawable.collaborate,
        title = "Collaborate",
        body = "Do not work by yourself, let other join in on the fun"
    ),
    CloudSync(
        imageId = R.drawable.cloud_sync,
        title = "Cloud sync",
        body = "Take your canvas with you no matter the device"
    )
}

@Preview(showBackground = true)
@Composable
fun OnBoardingScreenPreview() {
    AppTheme {
        OnBoardingScreen({})
    }
}