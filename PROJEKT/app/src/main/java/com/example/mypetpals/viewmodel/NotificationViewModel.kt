package com.example.mypetpals.viewmodel
import android.content.Context
import android.provider.ContactsContract
import androidx.lifecycle.ViewModel
import com.example.mypetpals.model.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue

import androidx.work.*
import com.example.mypetpals.workers.NotificationWorker
import java.util.concurrent.TimeUnit

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.mypetpals.NotificationReceiver
import com.example.mypetpals.model.Pet
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class NotificationViewModel: ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val db = FirebaseFirestore.getInstance()

    fun getUserNotifications(callback: (List<Notification>) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("notifications")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val notifications = result.map { document ->
                    document.toObject(Notification::class.java).apply { id = document.id }
                }
                callback(notifications)
            }
            .addOnFailureListener { exception ->
                Log.e("NotificationViewModel", "Error fetching notifications", exception)

            }
    }

    fun addNotification(notification: Notification, petId: String, callback: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        val newNotification = notification.copy(userId = userId, petId = petId)
        db.collection("notifications")
            .add(newNotification)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    fun updateNotification(notification: Notification, callback: (Boolean) -> Unit) {
        db.collection("notifications").document(notification.id)
            .set(notification)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }


    fun updatePetNameInNotifications(petId: String, newPetName: String, onComplete: (Boolean) -> Unit) {
        db.collection("notifications")
            .whereEqualTo("petId", petId)
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = db.batch()
                for (document in snapshot.documents) {
                    val notificationRef = db.collection("notifications").document(document.id)
                    batch.update(notificationRef, "petName", newPetName)
                }
                batch.commit()
                    .addOnSuccessListener {
                        Log.d("NotificationViewModel", "Successfully updated notifications.")
                        onComplete(true)
                    }
                    .addOnFailureListener { e ->
                        Log.e("NotificationViewModel", "Error updating notifications", e)
                        onComplete(false)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("NotificationViewModel", "Error fetching notifications", e)
                onComplete(false)
            }
    }

    fun deleteNot(notification: Notification, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                db.collection("notifications").document(notification.id).delete().await()
                onComplete(true)
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "Error deleting notification", e)
                onComplete(false)
            }
        }
    }

}