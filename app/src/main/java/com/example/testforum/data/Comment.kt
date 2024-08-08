package com.example.testforum.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference

data class Comment (
//    val postId: String = "",
    val commentContent: String = "",
    val creationDate: Timestamp? = null,
    val userReference: DocumentReference? = null
)