package com.example.testforum

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.testforum.data.Comment
import com.example.testforum.data.CommentWithUser
import com.example.testforum.data.PhotoData
import com.example.testforum.data.Post
import com.example.testforum.data.PostWithUser
import com.example.testforum.data.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DataViewModel : ViewModel() {
    private val db: FirebaseFirestore = Firebase.firestore

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _filteredUsers = MutableStateFlow<List<User>>(emptyList())
    val filteredUsers: StateFlow<List<User>> = _filteredUsers

    private val _commentsWithUsers = MutableStateFlow<List<CommentWithUser>>(emptyList())
    val commentsWithUsers: StateFlow<List<CommentWithUser>> get() = _commentsWithUsers

    private val _photoList = MutableStateFlow<List<PhotoData>>(emptyList())
    val photoList: StateFlow<List<PhotoData>> get() = _photoList

    private val _postsWithUsers = MutableStateFlow<List<PostWithUser>>(emptyList())
    val postsWithUsers: StateFlow<List<PostWithUser>> get() = _postsWithUsers

    fun fetchComments(postId: String) {
        val commentsList = mutableListOf<CommentWithUser>()
        db.collection("posts").document(postId).collection("comments")
            .orderBy("creationDate", Query.Direction.ASCENDING).get()
            .addOnSuccessListener { result ->
                commentsList.clear()
                _commentsWithUsers.value = commentsList
                result.documents.map { document ->
                    val comment =
                        document.toObject(Comment::class.java)?.copy(commentId = document.id) ?: return@addOnSuccessListener
                    val userReference = comment.userReference

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

    fun getPhotos(postId: String) {
        val photosList = mutableListOf<PhotoData>()
        db.collection("posts").document(postId).collection("photos")
            .get()
            .addOnSuccessListener { result ->
                photosList.clear()
                _photoList.value = photosList
                result.documents.map { document ->
                    val photo =
                        document.toObject(PhotoData::class.java) ?: return@addOnSuccessListener
                    photosList.add(photo)
                    if (photosList.size == result.size()) {
                        _photoList.value = photosList
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.w("CommentViewModel", "Error getting comments", e)
            }
    }

    fun getFilteredPostsWithUsers(query: String, boolean: Boolean) {
        val postsList = mutableListOf<PostWithUser>()
        db.collection("posts").orderBy("creationDate", Query.Direction.DESCENDING)
            .addSnapshotListener { result, e ->

                e?.let {
                    Log.w("PostViewModel", "Error getting posts", e)
                    return@addSnapshotListener
                }

                postsList.clear()
                _postsWithUsers.value = postsList
                result?.let { snapshot ->
                    snapshot.documents.map { document ->
                        val post = document.toObject(Post::class.java)?.copy(postId = document.id)
                        val userReference = post?.userReference
                        post?.let {
                            if (post.postContent.contains(query, ignoreCase = true)) {
                                userReference?.let {
                                    it.addSnapshotListener { userSnapshot, e ->
                                        e?.let {
                                            Log.w("PostViewModel", "Error getting user data", e)
                                            return@addSnapshotListener
                                        }
                                        val user = userSnapshot?.toObject(User::class.java)
                                        if (user != null) {
                                            postsList.add(PostWithUser(post, user))
                                            if (postsList.size == result.size()) {
                                                _postsWithUsers.value = postsList
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
    }

    fun getPosts(userEmail: String) {
        val postsList = mutableListOf<PostWithUser>()
        db.collection("posts").orderBy("creationDate", Query.Direction.DESCENDING)
            .addSnapshotListener { result, e ->

                e?.let {
                    Log.w("PostViewModel", "Error getting posts", e)
                    return@addSnapshotListener
                }

                postsList.clear()
                _postsWithUsers.value = postsList
                result?.let { snapshot ->
                    snapshot.documents.map { document ->
                        val post = document.toObject(Post::class.java)?.copy(postId = document.id)
                        val userReference = post?.userReference

                        userReference?.let {
                            it.addSnapshotListener { userSnapshot, e ->
                                e?.let {
                                    Log.w("PostViewModel", "Error getting user data", e)
                                    return@addSnapshotListener
                                }
                                val user = userSnapshot?.toObject(User::class.java)
                                user?.let {
                                    if (userEmail == "" || user.email == userEmail) {
                                        postsList.add(PostWithUser(post, user))
                                        if (postsList.size == result.size()) {
                                            _postsWithUsers.value = postsList
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
    }

    fun getPosts(userEmail: String, topicName: String? = null) {
        val postsList = mutableListOf<PostWithUser>()
        var query: Query =
            db.collection("posts").orderBy("creationDate", Query.Direction.DESCENDING)

        if (topicName != null) {
            query = query.whereEqualTo("topicName", topicName)
        }

        query.addSnapshotListener { result, e ->

            e?.let {
                Log.w("PostViewModel", "Error getting posts", e)
                return@addSnapshotListener
            }

            postsList.clear()
            _postsWithUsers.value = postsList
            result?.let { snapshot ->
                snapshot.documents.map { document ->
                    val post = document.toObject(Post::class.java)?.copy(postId = document.id)
                    val userReference = post?.userReference

                    userReference?.let {
                        it.addSnapshotListener { userSnapshot, e ->
                            e?.let {
                                Log.w("PostViewModel", "Error getting user data", e)
                                return@addSnapshotListener
                            }
                            val user = userSnapshot?.toObject(User::class.java)
                            user?.let {
                                if (userEmail == "" || user.email == userEmail) {
                                    postsList.add(PostWithUser(post, user))
                                    if (postsList.size == result.size()) {
                                        _postsWithUsers.value = postsList
                                    }
                                }
                            }
                        }
                    }
                }
            }
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

    fun addPost(newPostText: String, email: String, topic: String, selectedImagePaths: List<String>) {
        val postReference = db.collection("posts").document()
//        db.collection("posts").add(
//            Post(
//                postContent = newPostText,
//                userReference = db.collection("users").document(email),
//                creationDate = Timestamp.now(),
//                topicName = topic
//            )
//        )
        postReference.set(Post(postContent = newPostText, userReference = db.collection("users").document(email), topicName = topic, creationDate = Timestamp.now()))
            .addOnSuccessListener {
                for(imagePath in selectedImagePaths) {
//                    val photoData = hashMapOf("imagePath" to imagePath)
                    postReference.collection("photos").add(PhotoData(imagePath))
                }
            }
    }

    fun addComment(postId: String, newCommentText: String, email: String) {
        db.collection("posts").document(postId).collection("comments").add(
            Comment(
                commentContent = newCommentText,
                userReference = db.collection("users").document(email),
                creationDate = Timestamp.now()
            )
        )
    }

    fun removePost(postId: String) {
        val postReference = db.collection("posts").document(postId)
        postReference.collection("comments").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    document.reference.delete()
                }
            }
        postReference.collection("photos").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    document.reference.delete()
                }
            }
        postReference.delete()
    }



    fun getFilteredUsers(query: String) {
        val filteredUsersList = mutableListOf<User>()
        db.collection("users")
            .addSnapshotListener { result, e ->

                e?.let {
                    Log.w("FilteredUserViewModel", "Error getting users", e)
                    return@addSnapshotListener
                }

                filteredUsersList.clear()
                _filteredUsers.value = filteredUsersList
                result?.let { snapshot ->
                    snapshot.documents.map { document ->
                        val user = document.toObject(User::class.java)
                        user?.let {
                            if ((user.username?.contains(query, ignoreCase = true) == true)  ||
                                        (user.displayName?.contains(query, ignoreCase = true) == true)
                            ) {
                                filteredUsersList.add(user)
                                if (filteredUsersList.size == result.size()) {
                                    _filteredUsers.value = filteredUsersList
                                }
                            }
                        }
                    }
                }
            }

    }

    fun removeComment(commentId: String, postId: String) {
        val commentReference = db.collection("posts").document(postId).collection("comments").document(commentId)
        commentReference.delete()
    }
}