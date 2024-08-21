package dev.borisochieng.sketchpad.database.repository

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.auth.FirebaseAuth
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
	override suspend fun createChats(message1: String): Flow<Boolean> {
		val messageId = firestore.collection("projects").document(TEST_PROJECT_ID).collection("chats").document().id // Generate message ID

		val message = MessageModel(
			senderName = currentUser?.displayName ?: "",
			senderId = "${currentUser?.uid}",
			message = message1,
			projectId = TEST_PROJECT_ID ,
			messageId = messageId,
			timestamp = System.currentTimeMillis(),
			audioUrl = null,
			messageType = MessageType.MESSAGE
		)


		return flow {
			// Add the message to the collection
			try {
				firestore.collection("projects")
					.document(TEST_PROJECT_ID)
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

	override suspend fun getChats(): Flow<List<MessageModel>> {
		val messages = mutableStateListOf<MessageModel?>()

		return callbackFlow {


			firestore.collection("projects")
				.document(TEST_PROJECT_ID)
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

	override suspend fun loadChats(): Flow<List<MessageModel>> {
		val messages = mutableStateListOf<MessageModel?>()

		return callbackFlow {


			val listener = firestore.collection("projects")
				.document(TEST_PROJECT_ID)
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



}
