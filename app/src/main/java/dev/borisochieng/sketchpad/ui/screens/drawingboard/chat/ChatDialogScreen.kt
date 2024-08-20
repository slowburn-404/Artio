package dev.borisochieng.sketchpad.ui.screens.drawingboard.chat

import android.util.Log
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.borisochieng.sketchpad.database.repository.TAG
import dev.borisochieng.sketchpad.ui.screens.drawingboard.SketchPadViewModel

@Composable
fun ChatDialog(
    onCancel: () -> Unit,
    onOk: () -> Unit,
    viewModel: SketchPadViewModel,
    projectId: String,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        viewModel.initialize()
    }

    Dialog(onDismissRequest = onCancel) {
        val messages by viewModel.messages.collectAsState()
        Log.d(TAG, "list of messages in chatsScreen $messages")
        Card(
            modifier = Modifier
                .size(width = 460.dp, height = 520.dp)
        ) {
            // Add your UI elements here
            Column(
                modifier = Modifier
                    .padding(16.dp)
                //      .fillMaxWidth()
            ) {

                LazyColumn(
                    modifier = Modifier.defaultMinSize(380.dp, 420.dp)
                ) {
                    items(messages.size) { index ->
                        val checkNextSame = checkNextSame(index, messages)
                        val viewType = getItemViewType(index, messages)

                        BoxWithConstraints(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val senderCount = remember { mutableStateOf(false) }
                            val receiverCount = remember { mutableStateOf(false) }

                            if (viewType == SENDER_VIEW_TYPE) {
                                if (checkNextSame == true) {
                                    senderCount.value = true
                                }
                                SenderChat(
                                    message = messages[index]!!.message,
                                    maxWidth = maxWidth,
                                    backgroundColor = Color(0xFF1EBE71),
                                    textColor = Color.White,
                                    senderCount = senderCount,
                                    time = messages[index]!!.timestamp!!,
                                    senderName = messages[index]?.senderName?:""
                                )
                                senderCount.value = false
                            } else {
                                if (checkNextSame == true) {
                                    receiverCount.value = true
                                }
                                ReceiverChat(
                                    message = messages[index]!!.message,
                                    maxWidth = maxWidth,
                                    backgroundColor = Color(0xFFF2F2F2),
                                    textColor = Color(0xFF000000),
                                    receiverCount = receiverCount,
                                    time = messages[index]!!.timestamp!!,
                                    receiverName = messages[index]?.senderName?:""
                                )
                                receiverCount.value = false
                            }

                        }
                    }
                }

                // A horizontal divider to separate the content and the footer
                Divider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                )
                Spacer(modifier = Modifier.size(4.dp))
                ChatEditText(
                    text = viewModel.messageState.value.message,
                    onValueChange = { text ->
                        viewModel.onMessageChange(text)
                    },
                    onSendActionClicked = {
                        viewModel.onMessageSent()
                        keyboardController?.hide()
                        viewModel.messageState.value =
                            viewModel.messageState.value.copy(message = "")
                    },
                    viewModel = viewModel,
                    projectId = projectId
                )
            }
        }
    }
}