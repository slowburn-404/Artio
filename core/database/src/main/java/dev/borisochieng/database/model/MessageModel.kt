package dev.borisochieng.database.model

/*
    Data class for message model
*/


enum class MessageType {
    RECORDING,
    MESSAGE,
    PICTURE
}

data class MessageModel(
    val senderId: String,
    val projectId: String,
    val messageId: String,
    val message: String,
    val senderName: String?,
    val timestamp: Long?,
    val audioUrl: String?,
    val messageType: MessageType,
    val senderPhotoUrl: String?
) {
    constructor() : this("", "", "", "", "", null, null, MessageType.MESSAGE, "")
}
