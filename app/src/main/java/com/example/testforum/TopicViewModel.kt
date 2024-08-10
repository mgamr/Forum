package com.example.testforum

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testforum.data.Topic
import kotlinx.coroutines.launch

class TopicViewModel : ViewModel() {

    private val topicRepository = TopicRepository()

    fun addNewTopic(topic: Topic, parentTopicId: String? = null) {
        viewModelScope.launch {
            topicRepository.addTopic(topic, parentTopicId)
        }
    }

    suspend fun getAllTopicNames(): List<String> {
        return topicRepository.getAllTopicNames()
    }
}