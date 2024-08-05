package com.example.testforum.data

data class Comment (
    val postId: Int,
    val commentContent: String = "",
    val user: User = User()
)