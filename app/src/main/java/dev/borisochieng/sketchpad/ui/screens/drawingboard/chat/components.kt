package dev.borisochieng.sketchpad.ui.screens.drawingboard.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import dev.borisochieng.sketchpad.R
import dev.borisochieng.sketchpad.database.MessageModel
import dev.borisochieng.sketchpad.ui.screens.drawingboard.SketchPadViewModel
import dev.borisochieng.sketchpad.ui.theme.AppTheme
import dev.borisochieng.sketchpad.ui.theme.lightScheme

// view type
const val SENDER_VIEW_TYPE = 1
const val RECEIVER_VIEW_TYPE = 2

@Composable
fun ChatEditText(
    text: String,
    onValueChange: (newValue: String) -> Unit,
    onSendActionClicked: () -> Unit,
    viewModel: SketchPadViewModel,
    projectId: String
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val emojiOn = remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .imePadding(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedContainerColor = Color(0xFFF2F2F2),
                unfocusedContainerColor = Color(0xFFF2F2F2),
            ),
            maxLines = 2,
            value = text,
            onValueChange = {
                onValueChange(it)
            },
            placeholder = { Text("Type a message") },
            shape = RoundedCornerShape(40.dp),
            modifier = Modifier
                .padding(8.dp)
                .weight(1f),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Send,
                keyboardType = if (emojiOn.value) KeyboardType.Ascii else KeyboardType.Text,
            ),
            keyboardActions = KeyboardActions { onSendActionClicked() }
        )
        IconButton(
            onClick = {
                // Send the message if the icon is send


                viewModel.onMessageSent(projectId)
                keyboardController?.hide()
                viewModel.messageState.value =
                    viewModel.messageState.value.copy(message = "")


            },
            modifier = Modifier
                .background(color = Color.White, shape = CircleShape)
//                .width(48.dp)
//                .height(48.dp),

        ) {

            Icon(
                painter = painterResource(id = R.drawable.send_icon),
                contentDescription = "Record voice or send message",
                tint = Color(0xff808080)
            )
        }
    }
}


@Composable
fun SenderChat(
    message: String,
    backgroundColor: Color,
    textColor: Color,
    senderCount: MutableState<Boolean>,
    time: Long,
    senderName: String
) {
    val shape = if (senderCount.value) 10.dp else 0.dp
    val timeInSeconds = time / 1000
    val dateTime = org.threeten.bp.LocalDateTime.ofEpochSecond(
        timeInSeconds,
        0,
        org.threeten.bp.ZoneOffset.UTC
    )
    val timex = dateTime.toLocalTime()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.End,
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = colorScheme.primary,
                    shape = RoundedCornerShape(10.dp, 10.dp, shape, 10.dp)
                )
                .padding(8.dp)
                .wrapContentWidth()
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (!senderCount.value) {
                    Text(
                        text = "You",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = lightScheme.onSurfaceVariant,
                            textAlign = TextAlign.Right
                        ),
                    )
                }
                Text(
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = textColor,
                    ),
                    text = message,
                    textAlign = TextAlign.Left
                )
                Text(
                    text = timex.format(
                        org.threeten.bp.format.DateTimeFormatter.ofPattern(
                            "hh:mm a"
                        )
                    ),
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontWeight = FontWeight(300),
                        color = Color(0xFFBEBEBE),
                        textAlign = TextAlign.Right,
                    )
                )
            }

        }
        if (!senderCount.value) {
            Image(
                painter = painterResource(id = R.drawable.placeholder_foreground),
                contentDescription = "Profile picture",
                modifier = Modifier
                    // Set image size to 40 dp
                    .size(40.dp)
                    // Clip image to be shaped as a circle
                    .clip(CircleShape)

            )
        }

    }

}


@Composable
fun ReceiverChat(
    message: String,
    backgroundColor: Color,
    textColor: Color,
    receiverCount: MutableState<Boolean>,
    time: Long,
    receiverName: String,
) {
    val shape = if (receiverCount.value) 10.dp else 0.dp
    val timeInSeconds = time / 1000
    val dateTime = org.threeten.bp.LocalDateTime.ofEpochSecond(
        timeInSeconds,
        0,
        org.threeten.bp.ZoneOffset.UTC
    )
    val timex = dateTime.toLocalTime()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        if (!receiverCount.value) {
            Image(
                painter = painterResource(id = R.drawable.placeholder_foreground),
                contentDescription = "Profile picture",
                modifier = Modifier
                    // Set image size to 40 dp
                    .size(40.dp)
                    // Clip image to be shaped as a circle
                    .clip(CircleShape)
                    // Align image to bottom end of row
                    .align(Alignment.Bottom)

            )
        }
            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .background(
                        color = backgroundColor,
                        shape = RoundedCornerShape(10.dp, 10.dp, 10.dp, shape)
                    )
                    .padding(8.dp)
            )
            {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (!receiverCount.value) {
                        Text(
                            text = receiverName,
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = lightScheme.onBackground
                            ),
                            textAlign = TextAlign.Left
                        )
                    }
                    Text(
                        text = message,
                        color = textColor,
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight(400)),
                        textAlign = TextAlign.Left,
                    )

                    Text(
                        text = timex.format(org.threeten.bp.format.DateTimeFormatter.ofPattern("hh:mm a")),
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight(300),
                            color = Color(0xFFBEBEBE),
                            textAlign = TextAlign.Left,
                        )
                    )
                }
            }

    }
}

fun checkNextSame(position: Int, messageModels: List<MessageModel?>): Boolean? {
    // check that position is valid
    if (position < 1 || position >= messageModels.size) {
        return null // invalid position
    }
    for (i in 0 until messageModels.size - 1) {
        if (messageModels[position]?.senderId == messageModels[position - 1]?.senderId) {
            return true // same items found
        }
    }
    return false // no same items found
}

fun getItemViewType(position: Int, messageModels: List<MessageModel?>): Int {


    // val messageModelModels : List<MessageModel> = emptyList()
    return if (messageModels[position]?.senderId == FirebaseAuth.getInstance().uid) {

        SENDER_VIEW_TYPE
    } else {
        RECEIVER_VIEW_TYPE
    }
}