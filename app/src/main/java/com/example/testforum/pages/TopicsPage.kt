package com.example.testforum.pages

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.testforum.viewmodels.AuthState
import com.example.testforum.viewmodels.AuthViewModel
import com.example.testforum.viewmodels.TopicViewModel
import com.example.testforum.data.Topic
import com.google.android.gms.auth.api.signin.GoogleSignInClient


@Composable
fun TopicsPage(
    modifier: Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    topicViewModel: TopicViewModel,
    googleSignInClient: GoogleSignInClient
){
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        TopBar(
            "Topics",
            navController = navController,
            authViewModel = authViewModel,
            googleSignInClient = googleSignInClient
        )
        AddTopicButton(null, navController, authViewModel, topicViewModel)
        ExpandableTopicList(navController, authViewModel, topicViewModel)
    }
}

@Composable
fun AddTopicButton(
    parentTopicId: String? = null,
    navController: NavController,
    authViewModel: AuthViewModel,
    topicViewModel: TopicViewModel
) {
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    var addTopic by remember { mutableStateOf(false) }
    var newTopicName by remember { mutableStateOf("") }

    if (addTopic) {
        AlertDialog(
            onDismissRequest = { addTopic = false },
            title = { Text(text = "Add new topic") },
            text = {
                Column() {
                    TextField(
                        value = newTopicName,
                        onValueChange = { newTopicName = it },
                        label = { Text(text = "Topic Name") }
                    )
                }

            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTopicName.isNotBlank()) {
                            val newTopic = Topic(name = newTopicName)
                            topicViewModel.addNewTopic(newTopic, parentTopicId)
                            Toast.makeText(context, "Topic added", Toast.LENGTH_SHORT).show()
                        }
                        newTopicName = ""
                        addTopic = false
                    }
                ) {
                    Text(text = "Add")
                }
            },
            dismissButton = {
                Button(onClick = { addTopic = false }) {
                    Text(text = "Cancel")
                }
            }
        )
    }

    val onAddTopicClick: () -> Unit = {
        if (authState.value is AuthState.Unauthenticated) {
            navController.navigate("login")
        } else {
            addTopic = true
        }
    }

    if (parentTopicId != null) {
        AddSubtopic(onAddTopicClick)
    } else {
        AddMainTopic(onAddTopicClick)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMainTopic(onclick: () -> Unit) {
    Button(
        onClick = onclick,
        modifier = Modifier.padding(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
            contentColor = Color.Black
        ),
        shape = RoundedCornerShape(32.dp)
    ) {
        Text(text = "+ Add topic")
    }
}

@Composable
fun AddSubtopic(onclick: () -> Unit) {
    Button(
        onClick = onclick,
        modifier = Modifier
            .padding(end = 8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
            contentColor = Color.Black
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = "+ subtopic",
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
    }
}