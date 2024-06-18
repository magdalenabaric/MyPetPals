package com.example.mypetpals.model

import java.util.UUID

data class Pet (
        var id: String = "",
        val name: String = "",
        val animal: String = "",
        val age: Int = 0,
        val ownerID: String = "",
        var imageUrl: String = ""
)