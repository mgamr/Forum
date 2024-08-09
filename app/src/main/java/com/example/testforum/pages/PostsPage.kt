package com.example.testforum.pages

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.testforum.AuthViewModel
import com.example.testforum.DataViewModel
import com.example.testforum.TopicViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInClient

@Composable
fun PostsPage(modifier: Modifier = Modifier, topicName: String?= null, navController: NavController, authViewModel: AuthViewModel, dataViewModel: DataViewModel, googleSignInClient: GoogleSignInClient, topicViewModel: TopicViewModel) {
    DisplayAndAdd(text = "Post", modifier = modifier, topicName = topicName, isForum = true, authViewModel = authViewModel, dataViewModel = dataViewModel, navController = navController, googleSignInClient = googleSignInClient, topicViewModel = topicViewModel)
}