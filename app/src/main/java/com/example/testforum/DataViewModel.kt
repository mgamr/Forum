package com.example.testforum

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.testforum.data.Comment
import com.example.testforum.data.CommentWithUser
import com.example.testforum.data.User
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DataViewModel : ViewModel() {
    private val db: FirebaseFirestore = Firebase.firestore

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> get() = _comments

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _commentsWithUsers = MutableStateFlow<List<CommentWithUser>>(emptyList())
    val commentsWithUsers: StateFlow<List<CommentWithUser>> get() = _commentsWithUsers

    fun fetchComments(postId: String) {
        val commentsList = mutableListOf<CommentWithUser>()

        db.collection("posts").document(postId).collection("comments").orderBy("creationDate", Query.Direction.ASCENDING).get()
            .addOnSuccessListener { result ->
                val commentTasks = result.documents.map { document ->
                    val comment = document.toObject(Comment::class.java) ?: return@addOnSuccessListener
                    val userReference = comment.userReference // Assume Comment has a userId field

                    // Fetch user data for each comment
                    userReference?.let {
                       it.get()
                            .addOnSuccessListener { userSnapshot ->
                                val user = userSnapshot.toObject(User::class.java)
                                if (user != null) {
                                    commentsList.add(CommentWithUser(comment, user))
                                    if (commentsList.size == result.size()) {
                                        _commentsWithUsers.value = commentsList
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.w("CommentViewModel", "Error getting user data", e)
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.w("CommentViewModel", "Error getting comments", e)
            }
    }

    fun getComments(postId: String) {

        db.collection("posts").document(postId).collection("comments").orderBy("creationDate", Query.Direction.ASCENDING).get()
            .addOnSuccessListener {
                val commentsList: MutableList<Comment> = mutableListOf()
                for (document in it) {
                    commentsList.add(document.toObject(Comment::class.java))
                }
                _comments.value = commentsList
            }
            .addOnFailureListener { e ->
                Log.w("DataViewModel", "Error getting comments", e)
            }
    }

    fun getUserByReference(userReference: DocumentReference?) { // tu useri washlilia ra qnas
        userReference?.let { documentReference ->
            documentReference.get()
                .addOnSuccessListener {
                    _user.value = it.toObject(User::class.java)
                }
                .addOnFailureListener { e ->
                    Log.w("DataViewModel", "Error getting user by reference", e)
                }
        }
    }
}