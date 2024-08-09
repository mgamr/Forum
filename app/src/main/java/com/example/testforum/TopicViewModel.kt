package com.example.testforum

import androidx.lifecycle.ViewModel

class TopicViewModel: ViewModel() {
    private val topicRepository = TopicRepository()
    suspend fun getAllTopicNames(): List<String> {
        return topicRepository.getAllTopicNames()
    }
}