package com.example.testforum.repositories

import android.util.Log
import com.example.testforum.data.Comment
import com.example.testforum.data.CommentWithUser
import com.example.testforum.data.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class CommentsRepository {
    private val db: FirebaseFirestore = Firebase.firestore

    suspend fun fetchComments(postId: String): List<CommentWithUser> {
        val commentsList = mutableListOf<CommentWithUser>()
        try {
            val result = db.collection("posts").document(postId).collection("comments")
                .orderBy("creationDate", Query.Direction.ASCENDING).get().await()
            for (document in result) {
                val comment = document.toObject(Comment::class.java)
                val userReference = comment.userReference
                userReference?.let { ref ->
                    val userSnapshot = ref.get().await()
                    val user = userSnapshot.toObject(User::class.java)
                    if (user != null) {
                        commentsList.add(CommentWithUser(comment, user))
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("CommentsRepository", "Error getting comments", e)
        }

        return commentsList
    }

    suspend fun addComment(postId: String, newCommentText: String, email: String) {
        val newComment = Comment(
            commentContent = newCommentText,
            userReference = db.collection("users").document(email),
            creationDate = Timestamp.now()
        )
        db.collection("posts").document(postId).collection("comments").add(newComment).await()
    }
}