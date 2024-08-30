package dev.borisochieng.artio.ui.screens.onboarding

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
import androidx.compose.ui.unit.dp
import dev.borisochieng.artio.ui.navigation.Screens
import dev.borisochieng.artio.ui.screens.auth.AuthViewModel
import dev.borisochieng.artio.ui.theme.AppTypography
import dev.borisochieng.artio.ui.theme.lightScheme
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnBoardingScreen(navigate: (Screens) -> Unit, viewModel: AuthViewModel = koinViewModel()) {
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
                    viewModel.saveLaunchStatus()
                    navigate(Screens.HomeScreen)
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