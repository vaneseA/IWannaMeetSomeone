package com.example.iwannameetsomeone.utils

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FirebaseRef {

    companion object {
        val database = Firebase.database
        val userInfoRef = database.getReference("users")
        val myLikeRef = database.getReference("myLike")
        val likeMeRef = database.getReference("LikeMe")
        val userMsgRef = database.getReference("userMsg")
    }
}