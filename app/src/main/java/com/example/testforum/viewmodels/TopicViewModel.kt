package com.example.testforum.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testforum.repositories.TopicRepository
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

    suspend fun fetchTopics(): List<Topic> {
        return topicRepository.fetchTopics()
    }

    fun collectTopicNames(topic: Topic): List<String> {
        val topicNames = mutableListOf<String>()


        fun collectNamesRecursively(currentTopic: Topic) {
            topicNames.add(currentTopic.name)
            currentTopic.subtopics?.values?.forEach { subtopic ->
                collectNamesRecursively(subtopic)
            }
        }

        collectNamesRecursively(topic)
        return topicNames
    }
}