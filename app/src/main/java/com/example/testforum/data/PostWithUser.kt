package com.example.testforum.data

import java.io.Serializable


data class PostWithUser (
    val post: Post,
    val user: User
): Serializable