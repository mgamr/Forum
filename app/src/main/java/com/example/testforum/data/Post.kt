package com.example.testforum.data

import java.io.Serializable

data class Post (
    val postId: String = "",
    val postContent: String = "",
    val user: User = User()
): Serializable