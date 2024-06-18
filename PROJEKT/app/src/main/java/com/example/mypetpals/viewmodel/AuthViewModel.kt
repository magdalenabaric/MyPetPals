package com.example.mypetpals.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.tasks.await


class AuthViewModel: ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val currentUser: FirebaseUser? get() = FirebaseAuth.getInstance().currentUser




    fun signIn(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    onResult(task.isSuccessful)
                }
        }
    }

    fun register(
        name: String,
        surname: String,
        email: String,
        password: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName("$name $surname")
                            .build()

                        user?.updateProfile(profileUpdates)
                            ?.addOnCompleteListener { profileUpdateTask ->
                                if (profileUpdateTask.isSuccessful) {
                                    val userId = user.uid
                                    val userMap = hashMapOf(
                                        "name" to name,
                                        "surname" to surname,
                                        "email" to email
                                    )
                                    firestore.collection("users").document(userId)
                                        .set(userMap)
                                        .addOnCompleteListener { firestoreTask ->
                                            onResult(firestoreTask.isSuccessful)
                                        }
                                } else {
                                    onResult(false)
                                }
                            }
                    } else {
                        onResult(false)
                    }
                }
        }
    }



    fun uploadProfileImage(imageUri: Uri, onResult: (String) -> Unit) {
        val userId = currentUser?.uid ?: return
        val storageRef = FirebaseStorage.getInstance().reference.child("profile_images/$userId.jpg")

        storageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setPhotoUri(uri)
                        .build()
                    currentUser?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                firestore.collection("users").document(userId)
                                    .update("profileImageUrl", downloadUrl)
                                    .addOnCompleteListener { firestoreTask ->
                                        onResult(if (firestoreTask.isSuccessful) downloadUrl else "")
                                    }
                            } else {
                                onResult("")
                                Log.e("AuthViewModel", "Failed to update user profile: ${task.exception?.message}")
                            }
                        }
                }.addOnFailureListener {
                    onResult("")
                    Log.e("AuthViewModel", "Failed to get download URL: ${it.message}")
                }
            }
            .addOnFailureListener {
                onResult("")
                if (it is StorageException && it.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                    Log.e("AuthViewModel", "Object does not exist at location: ${it.message}")
                } else {
                    Log.e("AuthViewModel", "Failed to upload image: ${it.message}")
                }
            }
    }

    fun updateProfile(name: String, email: String, onResult: (Boolean) -> Unit) {
        val user = currentUser
        if (user != null) {
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()

            user.updateProfile(profileUpdates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    user.updateEmail(email).addOnCompleteListener { emailTask ->
                        if (emailTask.isSuccessful) {
                            val userId = user.uid
                            val userMap = hashMapOf(
                                "name" to name,
                                "email" to email
                            ) as Map<String, Any>
                            firestore.collection("users").document(userId)
                                .update(userMap)
                                .addOnCompleteListener { firestoreTask ->
                                    onResult(firestoreTask.isSuccessful)
                                }
                        } else {
                            onResult(false)
                        }
                    }
                } else {
                    onResult(false)
                }
            }
        } else {
            onResult(false)
        }
    }

    fun logout() {
        auth.signOut()
    }
}
