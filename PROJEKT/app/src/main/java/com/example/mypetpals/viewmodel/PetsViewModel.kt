package com.example.mypetpals.viewmodel

import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mypetpals.model.Pet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch


class PetsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance().reference
    val currentUser = auth.currentUser
    private val notificationViewModel = NotificationViewModel()


    private val _pets = MutableLiveData<List<Pet>>()
    val pets: LiveData<List<Pet>> = _pets

    fun updatePet(petId: String, name: String, species: String, age: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val petUpdates = mapOf(
                    "name" to name,
                    "species" to species,
                    "age" to age
                )

                db.collection("pets").document(petId)
                    .update(petUpdates)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            notificationViewModel.updatePetNameInNotifications(petId, name) { success ->
                                if (success) {
                                    Log.d("PetsViewModel", "Notifications updated successfully")
                                } else {
                                    Log.e("PetsViewModel", "Failed to update notifications")
                                }
                            }
                            onResult(true)
                        } else {
                            onResult(false)
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("PetsViewModel", "Failed to update pet: ${exception.message}")
                        onResult(false)
                    }
            } catch (e: Exception) {
                Log.e("PetsViewModel", "Error updating pet", e)
                onResult(false)
            }
        }
    }


    fun addPet(pet: Pet, imageUri: Uri?, onResult: (Boolean) -> Unit) {
        val userId = currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val user = auth.currentUser ?: throw Exception("User not authenticated")
                val petWithOwnerId = pet.copy(ownerID = user.uid)

                if (imageUri != null) {
                    val imageRef = storage.child("pet_images/${user.uid}/${pet.name}.jpg")
                    imageRef.putFile(imageUri).await()
                    val imageUrl = imageRef.downloadUrl.await().toString()
                    petWithOwnerId.imageUrl = imageUrl
                }

                val documentReference = db.collection("pets")
                    .add(petWithOwnerId)
                    .await()

                val updatedPet = petWithOwnerId.copy(id = documentReference.id)
                documentReference.set(updatedPet).await()

                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun getUserPets2() {
        viewModelScope.launch {
            try {
                val user = auth.currentUser ?: throw Exception("User not authenticated")
                val petsList = db.collection("pets")
                    .whereEqualTo("ownerID", user.uid)
                    .get()
                    .await()
                    .toObjects(Pet::class.java)
                _pets.postValue(petsList)
            } catch (e: Exception) {
                _pets.postValue(emptyList())
            }
        }
    }

    fun getUserPets(onResult: (List<Pet>) -> Unit) {
        viewModelScope.launch {
            try {
                val user = auth.currentUser ?: throw Exception("User not authenticated")
                val pets = db.collection("pets")
                    .whereEqualTo("ownerID", user.uid)
                    .get()
                    .await()
                    .toObjects(Pet::class.java)
                onResult(pets)
            } catch (e: Exception) {
                onResult(emptyList())
            }
        }
    }


    fun getPetById(petId: String, onResult: (Pet?) -> Unit) {
        viewModelScope.launch {
            try {
                Log.d("PetsViewModel", "Fetching pet with id: $petId")
                val documentSnapshot = db.collection("pets")
                    .document(petId)
                    .get()
                    .await()

                if (documentSnapshot.exists()) {
                    Log.d("PetsViewModel", "Document snapshot data: ${documentSnapshot.data}")
                    val pet = documentSnapshot.toObject(Pet::class.java)
                    Log.d("PetsViewModel", "Pet fetched: $pet")
                    onResult(pet)
                } else {
                    Log.d("PetsViewModel", "No pet found with id: $petId")
                    onResult(null)
                }
            } catch (e: Exception) {
                Log.e("PetsViewModel", "Error fetching pet", e)
                onResult(null)
            }
        }
    }

    fun uploadPetImage(petId: String, imageUri: Uri, onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val user = auth.currentUser ?: throw Exception("User not authenticated")
                val storageRef = storage.child("pet_images/${user.uid}/$petId.jpg")

                storageRef.putFile(imageUri).await()
                val downloadUrl = storageRef.downloadUrl.await().toString()

                db.collection("pets").document(petId)
                    .update("imageUrl", downloadUrl)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onResult(downloadUrl)
                        } else {
                            onResult("")
                            Log.e("PetsViewModel", "Failed to update pet image URL: ${task.exception?.message}")
                        }
                    }
            } catch (e: Exception) {
                onResult("")
                Log.e("PetsViewModel", "Error uploading pet image", e)
            }
        }
    }

    fun deletePet(petId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                db.collection("pets").document(petId).delete().await()
                onComplete(true)
            } catch (e: Exception) {
                Log.e("PetsViewModel", "Error deleting pet", e)
                onComplete(false)
            }
        }
    }

}


