package com.example.testforum.pages

import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.testforum.AuthState
import com.example.testforum.AuthViewModel
import com.example.testforum.DataViewModel
import com.example.testforum.MainActivity
import com.example.testforum.data.Post
import com.example.testforum.data.PostWithUser
import com.example.testforum.data.User
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel, dataViewModel: DataViewModel, googleSignInClient: GoogleSignInClient) {
    DisplayAndAdd(text = "Post", modifier = modifier, isForum = true, authViewModel = authViewModel, dataViewModel = dataViewModel, navController = navController, googleSignInClient = googleSignInClient)
}

@Composable
fun DisplayAndAdd(text: String, isForum: Boolean, topicName: String ?= null, modifier: Modifier, authViewModel: AuthViewModel, dataViewModel: DataViewModel, navController: NavController, googleSignInClient: GoogleSignInClient) {
    val user by authViewModel.user.observeAsState()
    val authState = authViewModel.authState.observeAsState()
    val postsWithUsersList by dataViewModel.postsWithUsers.collectAsState()

    DisposableEffect(Unit) {
        dataViewModel.getPosts("", topicName)
        onDispose {  }
    }

    var addPost by remember { mutableStateOf(false) }
    var newPostText by remember { mutableStateOf("") }
    val context = LocalContext.current
    var selectedImageUris by remember {
        mutableStateOf<List<Uri?>>(emptyList())
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
//                            viewModel.addNote(newPostText)
//                            val post = user?.let { Post(postContent = newPostText, userReference = it) }
//                            if (post != null) {
////                                user?.username?.let { posts.add(post) }
//                                posts.add(post)
//                            }
                            user?.let {
                                dataViewModel.addPost(newPostText, user!!.email);
                            }
                            Toast.makeText(context, "Added $text", Toast.LENGTH_SHORT).show()
                            newPostText = ""
                            addPost = false
//                            navController.navigate("home") {
//                                popUpTo("home") { inclusive = true }
//                                launchSingleTop = true
//                            }
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
                if(isForum) {
                    TopBar("Forum", navController = navController, authViewModel = authViewModel, googleSignInClient = googleSignInClient)
                    Button(onClick = { navController.navigate("topics") }) {
                        Text(text = "View topics")
                    }
                    ViewPosts(modifier = Modifier.padding(innerPadding), postsWithUsersList, navController, authViewModel = authViewModel, dataViewModel = dataViewModel)
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
//                                Icon(Icons.Filled.Add, contentDescription = null)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(text: String, navController: NavController, authViewModel: AuthViewModel, googleSignInClient: GoogleSignInClient) {
    val authState = authViewModel.authState.observeAsState()
    val user by authViewModel.user.observeAsState()
    TopAppBar(
        title = {
            Text(
                text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = {
                if(!navController.popBackStack()) {
                    val activity: MainActivity = MainActivity()
                    // on below line we are finishing activity.
                    activity.finish()
                    java.lang.System.exit(0)
                }
            }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
            }
        },
        actions = {
            if (authState.value is AuthState.Unauthenticated) {
                Button(
                    onClick = { navController.navigate("login") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(text = "Log In")
                }
            }
            if (authState.value is AuthState.Authenticated) {
                IconButton(onClick = {
                    user?.email?.let { email ->
                        navController.currentBackStackEntry?.savedStateHandle?.set(
                            "userEmail",
                            email
                        )
                        navController.navigate("profile")
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null
                    )
                }
                IconButton(onClick = { authViewModel.signout(googleSignInClient) }) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null
                    )
                }
            }
        }
    )
}

@Composable
fun ViewPosts(modifier: Modifier = Modifier, postsWithUsersList: List<PostWithUser>, navController: NavController, authViewModel: AuthViewModel, dataViewModel: DataViewModel) {
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
                    if(authState.value is AuthState.Authenticated) {
                        user?.let {
                            if(it.moderator and isUnacceptable(postWithUser.post.postContent)) deletable = true
                            if(it.email == postWithUser.user.email) deletable = true
                        }
                    }
                    SinglePostMainPart(modifier, postWithUser = postWithUser, navController = navController, deletable = deletable, dataViewModel = dataViewModel)
                    androidx.compose.foundation.text.ClickableText(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(4.dp),
                        text = AnnotatedString("view comments"), onClick = {
                            navController.currentBackStackEntry?.savedStateHandle?.let {
                                it["postWithUser"] = postWithUser
//                                postWithUser.user.email.let { email ->
//                                    it["email"] = email
//                                }
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

@Composable
fun SinglePostMainPart(modifier: Modifier, postWithUser: PostWithUser,  navController: NavController, deletable: Boolean, dataViewModel: DataViewModel) {
    val context = LocalContext.current
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                postWithUser.user.email.let { email ->
                    navController.navigate("userProfile/$email")
                }
            }) {
                Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null)
            }
            Text(
                text = postWithUser.user.displayName ?: postWithUser.user.username ?: "Unknown",
                color = Color.Gray
            )
            if(deletable) {
                IconButton(onClick = {
                    dataViewModel.removePost(postWithUser.post.postId)
                    Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show()
//                    navController.navigate("home")
                }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                }
            }

        }

        ClickableText(
            text = postWithUser.post.postContent
        )
    }

}

@Composable
fun ClickableText(text: String) {
    val uriHandler = LocalUriHandler.current
    val annotatedString = buildAnnotatedString {
        val urlPattern = Patterns.WEB_URL
        val matcher = urlPattern.matcher(text)
        var lastIndex = 0
        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()
            append(text.substring(lastIndex, start))
            pushStringAnnotation(tag = "URL", annotation = matcher.group())
            withStyle(style = SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline)) {
                append(matcher.group())
            }
            pop()
            lastIndex = end
        }
        append(text.substring(lastIndex, text.length))
    }

    androidx.compose.foundation.text.ClickableText(
        modifier = Modifier.padding(start = 12.dp),
        text = annotatedString,
        onClick = { offset ->

            annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()?.let {
                        stringAnnotation ->
                    uriHandler.openUri(stringAnnotation.item)
                }
        }
    )
}

//@Preview(showBackground = true)
//@Composable
//fun ForumPreview() {
//    ForumPage()
//}