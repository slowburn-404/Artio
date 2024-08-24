package dev.borisochieng.sketchpad.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullRefreshContainer(
	modifier: Modifier = Modifier,
	pullEnabled: Boolean,
	onRefresh: () -> Unit,
	content: @Composable () -> Unit
) {
	val isRefreshing = rememberSaveable { mutableStateOf(false) }
	val pullToRefreshState = rememberPullToRefreshState { pullEnabled }

	Box(
		modifier = modifier
			.nestedScroll(pullToRefreshState.nestedScrollConnection)
	) {
		content()

		if (pullToRefreshState.isRefreshing) {
			LaunchedEffect(true) {
				isRefreshing.value = true
				onRefresh()
				delay(1000)
				isRefreshing.value = false
			}
		}

		LaunchedEffect(isRefreshing.value) {
			if (isRefreshing.value) {
				pullToRefreshState.endRefresh()
			} else {
				pullToRefreshState.startRefresh()
			}
		}

		PullToRefreshContainer(
			state = pullToRefreshState,
			modifier = Modifier.align(Alignment.TopCenter)
		)
	}
}