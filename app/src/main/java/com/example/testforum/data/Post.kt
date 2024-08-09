package com.example.testforum.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import java.io.Serializable

data class Post (
    val postId: String = "",
    val postContent: String = "",
    val userReference: DocumentReference? = null,
    val creationDate: Timestamp? = null
): Serializable