package com.example.testforum.data

data class Post (
    val postContent: String = "",
    val user: User = User()
)