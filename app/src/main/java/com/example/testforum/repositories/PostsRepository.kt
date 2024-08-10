package com.example.testforum.repositories

import android.util.Log
import com.example.testforum.data.*
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class PostsRepository {
    private val db: FirebaseFirestore = Firebase.firestore

    suspend fun getPosts(userEmail: String, topicNames: List<String>? = null): List<PostWithUser> {
        val postsList = mutableListOf<PostWithUser>()
        var query: Query = db.collection("posts").orderBy("creationDate", Query.Direction.DESCENDING)

        topicNames?.let {
            query = query.whereIn("topicName", it)
        }

        try {
            val result = query.get().await()
            for (document in result) {
                val post = document.toObject(Post::class.java)?.copy(postId = document.id)
                val userReference = post?.userReference
                userReference?.let { ref ->
                    val userSnapshot = ref.get().await()
                    val user = userSnapshot.toObject(User::class.java)
                    if (user != null && (userEmail.isEmpty() || user.email == userEmail)) {
                        postsList.add(PostWithUser(post, user))
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("PostsRepository", "Error getting posts", e)
        }

        return postsList
    }

    suspend fun addPost(newPostText: String, email: String, topic: String) {
        val newPost = Post(
            postContent = newPostText,
            userReference = db.collection("users").document(email),
            creationDate = Timestamp.now(),
            topicName = topic
        )
        db.collection("posts").add(newPost).await()
    }

    suspend fun removePost(postId: String) {
        val postReference = db.collection("posts").document(postId)
        try {
            val comments = postReference.collection("comments").get().await()
            for (document in comments) {
                document.reference.delete().await()
            }
            postReference.delete().await()
        } catch (e: Exception) {
            Log.w("PostsRepository", "Error deleting post and its comments", e)
        }
    }
}