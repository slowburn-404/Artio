package dev.borisochieng.artio.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.borisochieng.artio.auth.domain.model.User
import dev.borisochieng.artio.ui.theme.AppTypography
import dev.borisochieng.artio.ui.theme.lightScheme
import java.util.Calendar

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Header(
    user: User,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .clip(CircleShape)
                .size(40.dp)
                .border(
                    width = 2.dp,
                    color = lightScheme.primary,
                    shape = CircleShape
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            if (user.imageUrl == null) {
                Text(
                    text = "${user.email[0].uppercaseChar()}",
                    modifier = Modifier.align(Alignment.Center),
                    style = AppTypography.titleLarge,
                    color = lightScheme.onBackground
                )
            } else {
                AsyncImage(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .align(Alignment.Center)
                        .size(40.dp),
                    contentDescription = "Avatar",
                    model = ImageRequest
                        .Builder(context)
                        .data(user.imageUrl)
                        .build(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Text(
            text = greetingText(user.displayName),
            modifier = Modifier.basicMarquee(),
            fontSize = 20.sp,
            softWrap = false,
            style = AppTypography.titleSmall
        )
    }
}

private fun greetingText(username: String?): String {
    val calendar = Calendar.getInstance()
    val currentHour = calendar.get(Calendar.HOUR_OF_DAY)

    return when (currentHour) {
        in 5..11 -> "Good morning, ${username ?: ""}"
        in 12..16 -> "Good afternoon, ${username ?: ""}"
        in 17..20 -> "Good evening, ${username ?: ""}"
        else -> "Hello, ${username ?: ""}"
    }
}
