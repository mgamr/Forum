package com.example.testforum.data

data class Topic (
    val topicId: Int,
    val parentTopicId: Int,
    val topicName: String
)