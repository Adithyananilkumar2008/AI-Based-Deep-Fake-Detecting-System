package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreScanRepository(
    private val db: FirebaseFirestore
) {
    // Secondary constructor resolving named database ID from string resources
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            FirebaseApp.getInstance(),
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    fun observeUserScans(userId: String): Flow<List<ScanRecord>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val scansRef = db.collection("users")
            .document(userId)
            .collection("scans")
            .orderBy("createdAt", Query.Direction.DESCENDING)

        val registration = scansRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Error listening to user scans for $userId", error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.documents.map { ScanRecord.fromSnapshot(it) }
                trySend(list)
            }
        }

        awaitClose {
            registration.remove()
        }
    }

    suspend fun saveScan(record: ScanRecord) {
        if (record.userId.isBlank()) return
        try {
            db.collection("users")
                .document(record.userId)
                .collection("scans")
                .document(record.id)
                .set(record.toMap())
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save scan record ${record.id} to Firestore", e)
        }
    }

    suspend fun deleteScan(userId: String, scanId: String) {
        if (userId.isBlank() || scanId.isBlank()) return
        try {
            db.collection("users")
                .document(userId)
                .collection("scans")
                .document(scanId)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete scan record $scanId from Firestore", e)
        }
    }

    companion object {
        private const val TAG = "FirestoreScanRepo"
    }
}
