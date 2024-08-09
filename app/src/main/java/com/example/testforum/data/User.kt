package com.example.testforum.data

import java.io.Serializable

data class User(
    var email: String = "",
    var username: String? = null,
    var displayName: String? = null,
    var profilePicture: String? = null,
    var moderator: Boolean = false
): Serializable