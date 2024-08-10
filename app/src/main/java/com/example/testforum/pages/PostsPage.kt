package com.example.testforum.pages

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.testforum.viewmodels.AuthState
import com.example.testforum.viewmodels.AuthViewModel
import com.example.testforum.viewmodels.DataViewModel
import com.example.testforum.viewmodels.TopicViewModel
import com.example.testforum.data.PostWithUser
import com.google.android.gms.auth.api.signin.GoogleSignInClient

@Composable
fun PostsPage(modifier: Modifier = Modifier, topicNames: List<String>?= null, navController: NavController, authViewModel: AuthViewModel, dataViewModel: DataViewModel, topicViewModel: TopicViewModel, googleSignInClient: GoogleSignInClient) {
    DisplayAndAdd(text = "Post", modifier = modifier, topicNames = topicNames, isForum = true, authViewModel = authViewModel, dataViewModel = dataViewModel, navController = navController, googleSignInClient = googleSignInClient, topicViewModel = topicViewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayAndAdd(
    text: String,
    isForum: Boolean,
    topicNames: List<String>? = null,
    modifier: Modifier,
    authViewModel: AuthViewModel,
    dataViewModel: DataViewModel,
    topicViewModel: TopicViewModel,
    navController: NavController,
    googleSignInClient: GoogleSignInClient
) {
    val user by authViewModel.user.observeAsState()
    val authState = authViewModel.authState.observeAsState()
    val postsWithUsersList by dataViewModel.postsWithUsers.collectAsState()

    DisposableEffect(Unit) {
        dataViewModel.getPosts("", topicNames)
        onDispose { }
    }

    var addPost by remember { mutableStateOf(false) }
    var newPostText by remember { mutableStateOf("") }
    val context = LocalContext.current
    var selectedImageUris by remember {
        mutableStateOf<List<Uri?>>(emptyList())
    }
    var topics by remember { mutableStateOf(listOf("Choose a topic")) }
    LaunchedEffect(Unit) {
        try {
            topics = topicViewModel.getAllTopicNames()
        } catch (e: Exception) {
            Log.e("topic dropdownmenu", "Error fetching topic names", e)
        }
    }

    var isExpanded by remember {
        mutableStateOf(false)
    }
    var selectedTopic by remember {
        mutableStateOf(topics[0])
    }

    val multiplePhotosPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = {
            selectedImageUris = it
        }
    )
    if (addPost) {
        AlertDialog(
            onDismissRequest = { addPost = false },
            title = { Text(text = "Add $text") },
            text = {
                Column() {

                    ExposedDropdownMenuBox(
                        expanded = isExpanded,
                        onExpandedChange = { isExpanded = !isExpanded }) {
                        TextField(
                            value = selectedTopic,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = isExpanded
                                )
                            },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = isExpanded,
                            onDismissRequest = { isExpanded = false }) {
                            topics.forEachIndexed { index, s ->
                                DropdownMenuItem(
                                    text = { Text(text = s) },
                                    onClick = {
                                        selectedTopic = topics[index]
                                        isExpanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        }
                    }

                    TextField(
                        value = newPostText,
                        onValueChange = { newPostText = it },
                        label = { Text(text = "$text Content") }
                    )
                    Button(onClick = {
                        multiplePhotosPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }) {
                        Text("upload images")
                    }
                }

            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPostText.isNotBlank()) {
                            user?.let {
                                dataViewModel.addPost(
                                    newPostText,
                                    user!!.email,
                                    topic = selectedTopic
                                );
                            }
                            Toast.makeText(context, "Added $text", Toast.LENGTH_SHORT).show()
                            newPostText = ""
                            addPost = false
                        }
                    }
                ) {
                    Text(text = "Add")
                }
            },
            dismissButton = {
                Button(onClick = { addPost = false }) {
                    Text(text = "Cancel")
                }
            }
        )
    }
    Scaffold() { innerPadding ->
        Box() {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isForum) {
                    TopBar(
                        "Forum",
                        navController = navController,
                        authViewModel = authViewModel,
                        googleSignInClient = googleSignInClient
                    )
                    ViewPosts(
                        modifier = Modifier.padding(innerPadding),
                        postsWithUsersList,
                        navController,
                        authViewModel = authViewModel,
                        dataViewModel = dataViewModel
                    )
                }
            }
            FloatingActionButton(
                onClick = {
                    if (authState.value is AuthState.Unauthenticated) {
                        navController.navigate("login")
                    } else {
                        addPost = true
                    }
                },
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 20.dp, end = 20.dp),
                containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
            ) {
                Text("Add $text")
            }
        }
    }
}

@Composable
fun ViewPosts(
    modifier: Modifier = Modifier,
    postsWithUsersList: List<PostWithUser>,
    navController: NavController,
    authViewModel: AuthViewModel,
    dataViewModel: DataViewModel
) {
    val user by authViewModel.user.observeAsState()
    val authState = authViewModel.authState.observeAsState()
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        content = {
            items(postsWithUsersList) { postWithUser ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                        .border(
                            width = 1.dp,
                            color = Color.DarkGray,
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    var deletable = false
                    if (authState.value is AuthState.Authenticated) {
                        user?.let {
                            if (it.moderator and isUnacceptable(postWithUser.post.postContent)) deletable =
                                true
                            if (it.email == postWithUser.user.email) deletable = true
                        }
                    }
                    SinglePostMainPart(
                        modifier,
                        postWithUser = postWithUser,
                        navController = navController,
                        deletable = deletable,
                        dataViewModel = dataViewModel
                    )
                    androidx.compose.foundation.text.ClickableText(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(4.dp),
                        text = AnnotatedString("view comments"), onClick = {
                            navController.currentBackStackEntry?.savedStateHandle?.let {
                                it["postWithUser"] = postWithUser
                            }
                            navController.navigate("singlePost")
                        })
                }
            }
            if (postsWithUsersList.isEmpty()) {
                item {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        text = "No items yet",
                        fontSize = 16.sp
                    )
                }
            }
        }
    )
}

fun isUnacceptable(postContent: String): Boolean {
    return postContent.contains("I'm a bully", true)
}