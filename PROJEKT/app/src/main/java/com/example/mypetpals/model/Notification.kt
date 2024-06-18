package com.example.mypetpals.model

import com.google.firebase.Timestamp
import com.google.type.DateTime

data class Notification (
    var id: String ="",
    val petName: String="",
    val description: String="",
    val time: Timestamp? = null,
    val userId: String="",
    var petId: String = ""

)