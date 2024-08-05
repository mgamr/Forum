package com.example.testforum

import android.content.ContentValues.TAG
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun AddPost() {
    val db = Firebase.firestore

    var username by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }
    var postText by remember { mutableStateOf("") }

    val posts = db.collection("posts")

    Column(modifier = Modifier.padding(16.dp)){
        TextField(
            value = username,
            onValueChange = {newText -> username = newText},
            label = { Text("Name") }
        )
        TextField(
            value = topic,
            onValueChange = {newText -> topic = newText},
            label = { Text("Topic") }
        )
        TextField(
            value = postText,
            onValueChange = {newText -> postText = newText},
            label = { Text("Post") }
        )
        val context = LocalContext.current
        Button(
            onClick = {
                val post = Post(username, postText, topic)
                posts.document(username).set(post)
                Toast.makeText(context, "Added Post", Toast.LENGTH_SHORT).show()
                username  = ""
                topic = ""
                postText = ""
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Add")
        }
    }


    db.collection("posts")
        .get()
        .addOnSuccessListener { result ->
            for (document in result) {
                Log.d(TAG, "${document.id} => ${document.data}")
            }
        }
        .addOnFailureListener { exception ->
            Log.d(TAG, "Error getting documents: ", exception)
        }

}

data class Post(val user: String, val content: String, val topic: String)
