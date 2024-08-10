package com.example.testforum.data

import java.io.Serializable

data class WholePost (
    val post: Post,
    val user: User,
    val imagePaths: List<PhotoData>
): Serializable