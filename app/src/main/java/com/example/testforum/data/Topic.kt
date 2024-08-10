package com.example.testforum.data

data class Topic(
    var id: String = "",
    val name: String = "",
    val subtopics: Map<String, Topic>? = null
)