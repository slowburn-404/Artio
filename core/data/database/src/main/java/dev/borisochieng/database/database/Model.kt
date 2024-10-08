package dev.borisochieng.database.database

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.borisochieng.artio.ui.screens.drawingboard.data.PathProperties
import dev.borisochieng.artio.ui.screens.drawingboard.data.TextProperties
import java.util.Calendar
import java.util.Date
import java.util.UUID

@Entity
data class Sketch(
	@PrimaryKey
	val id: String = UUID.randomUUID().toString(),
	val name: String,
	val dateCreated: Date = Calendar.getInstance().time,
	val lastModified: Date = Calendar.getInstance().time,
	val pathList: List<PathProperties>,
	val textList: List<TextProperties>,
	val isBackedUp: Boolean = false
)

// Data class for message model

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
	val messageType : MessageType,
	val senderPhotoUrl: String?
) {
	constructor() : this("", "", "","", "",null, null, MessageType.MESSAGE,"")
}
