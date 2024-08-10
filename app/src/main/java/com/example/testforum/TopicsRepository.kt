package com.example.testforum

import android.util.Log
import com.example.testforum.data.Topic
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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

    suspend fun getAllTopicNames(query: String): List<String> {
        val topicNames = mutableListOf<String>()

        val topLevelTopics = topicsRef.get().await()
        topicsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val topic = snapshot.getValue(Topic::class.java)
                // Do something with the topic data
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors.
            }
        })
        topLevelTopics.children.forEach { topicSnapshot ->
            val topic = topicSnapshot.getValue<Topic>()
            topic?.let {
                collectNamesRec(it, topicNames, query)
            }
        }

        return topicNames
    }


    private fun collectNamesRec(currentTopic: Topic, topicNames: MutableList<String>, query: String) {
        if(query == "" || currentTopic.name.contains(query, ignoreCase = true)) topicNames.add(currentTopic.name)
        currentTopic.subtopics?.values?.forEach { subtopic ->
            collectNamesRec(subtopic, topicNames, query)
        }
    }

    suspend fun getFilteredTopics(query: String) {
        getAllTopicNames(query)
    }
}
