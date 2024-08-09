package com.example.testforum

import android.util.Log
import com.example.testforum.data.Topic
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
}
