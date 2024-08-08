package com.example.testforum.pages

import android.content.ContentValues.TAG
import android.util.Log
import android.util.Patterns
import android.widget.Toast
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
import androidx.compose.runtime.LaunchedEffect
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
import com.example.testforum.data.Post
import com.example.testforum.data.User
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel, googleSignInClient: GoogleSignInClient) {
    val authState = authViewModel.authState.observeAsState()
    val user by authViewModel.user.observeAsState()

    val db = Firebase.firestore
    val posts = db.collection("posts")
    val postsList = remember { mutableStateListOf<Post>() }

//    LaunchedEffect(authState.value) {
//        when(authState.value){
//            is AuthState.Unauthenticated -> navController.navigate("login")
//            else -> Unit
//        }
//    }

    LaunchedEffect(Unit) {
        posts.get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val post = document.toObject(Post::class.java).copy(postId = document.id)
                    postsList.add(post)
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
    }

    var addPost by remember { mutableStateOf(false) }
    var newPostText by remember { mutableStateOf("") }
    val context = LocalContext.current

    if (addPost) {
        AlertDialog(
            onDismissRequest = { addPost = false },
            title = { Text(text = "Add Post") },
            text = {
                TextField(
                    value = newPostText,
                    onValueChange = { newPostText = it },
                    label = { Text(text = "Post Content") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPostText.isNotBlank()) {
//                            viewModel.addNote(newPostText)
                            val post = user?.let { Post(postContent = newPostText, user = it) }
                            if (post != null) {
//                                user?.username?.let { posts.add(post) }
                                posts.add(post)
                            }
                            Toast.makeText(context, "Added Post", Toast.LENGTH_SHORT).show()
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
                TopAppBar(
                    title = {
                        Text(
                            "Forum",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
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

                ViewPosts(modifier = Modifier.padding(innerPadding), postsList, navController, user)
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
                Text("Add Post")
//                                Icon(Icons.Filled.Add, contentDescription = null)
            }
        }
    }
}

@Composable
fun ViewPosts(modifier: Modifier = Modifier, postsList: List<Post>,  navController: NavController, user: User?) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        content = {
            items(postsList) { post ->
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

                    SinglePostMainPart(modifier, post = post, navController = navController)
                    androidx.compose.foundation.text.ClickableText(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(4.dp),
                        text = AnnotatedString("view comments"), onClick = {


                            navController.currentBackStackEntry?.savedStateHandle?.let {
                                it.set("post", post)
                                user?.email?.let { email ->
                                    it["email"] = email
                                } ?: run { it["email"] = "" }
                            }
                            navController.navigate("singlePost")
                    })
                }
            }
            if (postsList.isEmpty()) {
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

@Composable
fun SinglePostMainPart(modifier: Modifier, post: Post,  navController: NavController) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                post.user.email.let { email ->
                    navController.currentBackStackEntry?.savedStateHandle?.set(
                        "userEmail",
                        email
                    )
                    navController.navigate("profile")
                }
            }) {
                Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null)
            }
            Text(
                text = post.user.displayName ?: post.user.username ?: "Unknown",
                color = Color.Gray
            )
        }

        ClickableText(
            text = post.postContent
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