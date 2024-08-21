package dev.borisochieng.sketchpad.database.repository

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import dev.borisochieng.sketchpad.database.MessageModel
import dev.borisochieng.sketchpad.database.MessageType
import dev.borisochieng.sketchpad.database.Sketch
import dev.borisochieng.sketchpad.database.SketchDao
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

const val TAG = "AppTag"
const val TEST_PROJECT_ID = "TestProjectId"

class SketchRepositoryImpl: SketchRepository, KoinComponent {

	private val sketchDao by inject<SketchDao>()

	override fun getAllSketches(): Flow<List<Sketch>> {
		return sketchDao.getAllSketches()
	}

	override fun getSketch(sketchId: String): Flow<Sketch> {
		return sketchDao.getSketch(sketchId)
	}

	override suspend fun saveSketch(sketch: Sketch) {
		return sketchDao.saveSketch(sketch)
	}

	override suspend fun refreshDatabase(sketches: List<Sketch>) {
		sketchDao.clearDatabase()
		return sketchDao.insertSketches(sketches)
	}

	override suspend fun updateSketch(sketch: Sketch) {
		return sketchDao.updateSketch(sketch)
	}

	override suspend fun deleteSketch(sketch: Sketch) {
		return sketchDao.deleteSketch(sketch)
	}
	private val currentUser = FirebaseAuth.getInstance().currentUser
	private val firestore = FirebaseFirestore.getInstance()
	override suspend fun createChats(message1: String,boardId: String): Flow<Boolean> {
		val messageId = firestore.collection("projects").document(boardId).collection("chats").document().id // Generate message ID

		val message = MessageModel(
			senderName = currentUser?.displayName ?: "",
			senderId = "${currentUser?.uid}",
			message = message1,
			projectId = boardId,
			messageId = messageId,
			timestamp = System.currentTimeMillis(),
			audioUrl = null,
			messageType = MessageType.MESSAGE
		)


		return flow {
			// Add the message to the collection
			try {
				firestore.collection("projects")
					.document(boardId)
					.collection("chats")
					.document(messageId) // Use message ID for the document
					.set(message)
					.await() // Suspend until the task is complete
				emit(true) // Emit true as a flow value
			} catch (e: Exception) {
				Log.w(TAG, "Error adding message", e)
				emit(false) // Emit false as a flow value
			}
		}

	}

	override suspend fun getChats(boardId: String): Flow<List<MessageModel>> {
		val messages = mutableStateListOf<MessageModel?>()

		return callbackFlow {


			firestore.collection("projects")
				.document(boardId)
				.collection("chats")
				.orderBy("timestamp", Query.Direction.ASCENDING)
				.get()
				.addOnSuccessListener { querySnapshot ->
					for (document in querySnapshot.documents) {
						val model = document.toObject<MessageModel>()
						messages.add(model)
					}

					// Send the list of messages through the flow
					trySend(messages.toList().filterNotNull())

					// Log the messages
					Log.d(TAG, "Messages: $messages")
				}
				.addOnFailureListener { exception ->
					// Handle the failure
					Log.e(TAG, "Error getting messages: $exception")

				}

			// Note: Don't close the flow here

			awaitClose()

			// The flow will be closed when the coroutine is canceled
		}

	}

	override suspend fun loadChats(boardId: String): Flow<List<MessageModel>> {
		val messages = mutableStateListOf<MessageModel?>()

		return callbackFlow {


			val listener = firestore.collection("projects")
				.document(boardId)
				.collection("chats")
				.orderBy("timestamp", Query.Direction.ASCENDING)
				.addSnapshotListener { querySnapshot, exception ->
					if (exception != null) {
						// Handle the error
						return@addSnapshotListener
					}

					if (querySnapshot != null) {
						messages.clear() // Clear the existing list before adding new data

						// Loop through the query snapshot to get each document
						for (documentSnapshot in querySnapshot.documents) {
							// Convert the document data to your model class
							val messageModel = documentSnapshot.toObject(MessageModel::class.java)
							if (messageModel != null) {
								messages.add(messageModel)
							}
						}

						// Send the updated list of messages through the flow
						trySend(messages.toList().filterNotNull())
					}
				}

			awaitClose {
				// Remove the listener when the flow is cancelled
				listener.remove()
			}
		}
	}


	override suspend fun updateTypingStatus(isTyping: Boolean, boardId: String) {
		val projectRef = firestore.collection("projects").document(boardId)

		projectRef.get().addOnSuccessListener { document ->
			if (document != null&& document.exists()) {
				// Field exists, proceed with update
				if (isTyping) {
					projectRef.update("typingUsers", FieldValue.arrayUnion(currentUser?.displayName ?: ""))
				} else {
					projectRef.update("typingUsers", FieldValue.arrayRemove(currentUser?.displayName ?: ""))
				}
			} else {
				// Field doesn't exist, create it with an empty array
				val initialData = hashMapOf("typingUsers" to emptyList<String>())
				projectRef.set(initialData).addOnSuccessListener {
					// After creating the field, proceed with the update
					if (isTyping) {
						projectRef.update("typingUsers", FieldValue.arrayUnion(currentUser?.displayName ?: ""))
					} else {
						projectRef.update("typingUsers", FieldValue.arrayRemove(currentUser?.displayName ?: ""))
					}
				}
			}
		}
	}
	// Function to listen for typing status changes
	override suspend fun listenForTypingStatuses(boardId: String): Flow<List<String>> = callbackFlow {
		val projectRef = firestore.collection("projects").document(boardId)
		val listener = projectRef.addSnapshotListener { snapshot, error ->
			if (error != null) {
				// Handle error
				close(error) // Close the flow with the error
				return@addSnapshotListener
			}

			if (snapshot != null && snapshot.exists()) {
				val typingUsers = snapshot.get("typingUsers") as? List<String> ?: emptyList()
				trySend(typingUsers) // Emit the list of typing users
			}
		}

		awaitClose {
			listener.remove() // Remove the listener when the flow is closed
		}
	}



}
