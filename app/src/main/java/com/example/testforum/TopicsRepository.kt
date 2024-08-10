package com.example.testforum

import android.util.Log
import com.example.testforum.data.Topic
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class TopicRepository {
    private val realtimeDb: FirebaseDatabase = Firebase.database
    private val topicsRef = realtimeDb.reference.child("topics")

    suspend fun fetchTopics(): List<Topic> {
        val snapshot = topicsRef.get().await()
        val fetchedData = snapshot.children.mapNotNull { it.getValue<Topic>() }
        Log.d("Fetched Data", fetchedData.toString())
        return fetchedData
    }

    suspend fun addTopic(topic: Topic, parentTopicId: String? = null) {
        try {
//            if (parentTopicId == null) {
//                val newTopicRef = topicsRef.push() // Generates a new unique key
//                topic.id = newTopicRef.key ?: ""
//                newTopicRef.setValue(topic).await()
//            } else {
//                val parentTopicRef = topicsRef.child(parentTopicId).child("subtopics").push()
//                topic.id = parentTopicRef.key ?: ""
//                parentTopicRef.setValue(topic).await()
//            }

            val newTopicRef = if (parentTopicId == null) {
                // If no parent ID is provided, add as a top-level topic
                topicsRef.push().also { topic.id = it.key ?: "" }
            } else {
                // If a parent ID is provided, find the correct parent topic
                val parentTopicRef = findSubtopicReference(topicsRef, parentTopicId)
                parentTopicRef?.child("subtopics")?.push()?.also { topic.id = it.key ?: "" }
            }

            newTopicRef?.setValue(topic)?.await()
        } catch (e: Exception) {
            println("Error writing in testTopic")
            e.printStackTrace()
        }
    }

    private suspend fun findSubtopicReference(currentRef: DatabaseReference, targetId: String): DatabaseReference? {
        val snapshot = currentRef.get().await()
        snapshot.children.forEach { childSnapshot ->
            val topic = childSnapshot.getValue(Topic::class.java)
            if (topic?.id == targetId) {
                return childSnapshot.ref // Return the DatabaseReference of the matched subtopic
            } else {
                topic?.subtopics?.let {
                    val subtopicRef =
                        findSubtopicReference(childSnapshot.ref.child("subtopics"), targetId)
                    if (subtopicRef != null) return subtopicRef
                }
            }
        }
        return null
    }

      
    suspend fun getAllTopicNames(): List<String> {
        val topicNames = mutableListOf<String>()

        val topLevelTopics = topicsRef.get().await()

        topLevelTopics.children.forEach { topicSnapshot ->
            val topic = topicSnapshot.getValue<Topic>()
            topic?.let {
                collectNamesRec(it, topicNames)
            }
        }

        return topicNames
    }


    private fun collectNamesRec(currentTopic: Topic, topicNames: MutableList<String>) {
        topicNames.add(currentTopic.name)
        currentTopic.subtopics?.values?.forEach { subtopic ->
            collectNamesRec(subtopic, topicNames)
        }
    }
}
