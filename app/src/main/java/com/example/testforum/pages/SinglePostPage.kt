package com.example.testforum.pages

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.testforum.viewmodels.AuthState
import com.example.testforum.viewmodels.AuthViewModel
import com.example.testforum.viewmodels.DataViewModel
import com.example.testforum.data.Comment
import com.example.testforum.data.CommentWithUser
import com.example.testforum.data.PostWithUser
import com.google.android.gms.auth.api.signin.GoogleSignInClient


@Composable
fun SinglePostPage(modifier: Modifier, navController: NavController, dataViewModel: DataViewModel, authViewModel: AuthViewModel, googleSignInClient: GoogleSignInClient) {
    val postWithUser = navController.previousBackStackEntry?.savedStateHandle?.get<PostWithUser>("postWithUser")
    val commentWithUser by dataViewModel.commentsWithUsers.collectAsState()

    val user by authViewModel.user.observeAsState()
    val authState = authViewModel.authState.observeAsState()

    var addPost by remember { mutableStateOf(false) }
    var newPostText by remember { mutableStateOf("") }
    val context = LocalContext.current

    if (addPost) {
        AlertDialog(
            onDismissRequest = { addPost = false },
            title = { Text(text = "Add Comment") },
            text = {
                TextField(
                    value = newPostText,
                    onValueChange = { newPostText = it },
                    label = { Text(text = "Comment Content") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPostText.isNotBlank()) {
                            user?.let {
                                postWithUser?.let { dataViewModel.addComment(postId = postWithUser.post.postId, newCommentText = newPostText, email = user!!.email); }

                            }
                            Toast.makeText(context, "Added Comment", Toast.LENGTH_SHORT).show()
                            newPostText = ""
                            addPost = false
                            navController.currentBackStackEntry?.savedStateHandle?.let {
                                it["postWithUser"] = postWithUser
                            }
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

    LaunchedEffect(postWithUser?.post) {
        postWithUser?.post?.let {
            dataViewModel.fetchComments(it.postId)
        }
    }
    Box() {
        LazyColumn(modifier = modifier.fillMaxSize()) {
            postWithUser?.let {
                item {
                    Column() {
                        TopBar("", navController, authViewModel = authViewModel, googleSignInClient = googleSignInClient)
                        var deletable = false
                        if(authState.value is AuthState.Authenticated) {
                            user?.let {
                                if(it.moderator and isUnacceptable(postWithUser.post.postContent)) deletable = true
                                if(it.email == postWithUser.user.email) deletable = true
                            }
                        }
                        SinglePostMainPart(modifier = modifier, postWithUser = postWithUser, navController = navController, deletable = deletable, dataViewModel = dataViewModel)
                    }
                }
            }

            items(commentWithUser) {commentWithUser ->
                var deletable2 = false
                if(authState.value is AuthState.Authenticated) {
                    user?.let {
                        if(it.moderator and isUnacceptable(commentWithUser.comment.commentContent)) deletable2 = true
                        if(it.email == commentWithUser.user.email) deletable2 = true
                    }
                }
                CommentItem(commentWithUser = commentWithUser, navController = navController, deletable = deletable2)
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
            Text("Add Comment")
        }
    }
}

@Composable
fun CommentItem(commentWithUser: CommentWithUser, navController: NavController, deletable: Boolean) {
    Row() {
        IconButton(onClick = {
            commentWithUser.user.email.let { email ->
                navController.navigate("profile/$email")
            }
        }) {
            Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null)
        }
        Column() {
            var text = "Unknown"
            commentWithUser.user.displayName?.let { text = it
            } ?: run { commentWithUser.user.username?.let { text = it } }
            Text(
                text = text,
                color = Color.Gray
            )
            ClickableText(text = commentWithUser.comment.commentContent)
        }
        if(deletable) {
            IconButton(onClick = {
                commentWithUser.user.email.let { email ->
                    navController.navigate("profile/$email")
                }
            }) {
                Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null)
            }
        }

    }
}

@Composable
fun DisplayComment(modifier: Modifier, comment: Comment, dataViewModel: DataViewModel, navController: NavController) {
    val user by dataViewModel.user.collectAsState()

    Row(modifier = modifier) {
        dataViewModel.getUserByReference(comment.userReference)
        IconButton(onClick = {
            user?.email.let { email ->
                navController.currentBackStackEntry?.savedStateHandle?.set(
                    "userEmail",
                    email
                )
                navController.navigate("profile")
            }
        }) {
            Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null)
        }
        Column() {
            var text = "Unknown"
            user?.displayName?.let { text = it
            } ?: run { user?.username?.let { text = it } }
            Text(
                text = text,
                color = Color.Gray
            )
            Text(modifier = modifier, text = comment.commentContent)
        }

    }

}

@Composable
fun SinglePostMainPart(
    modifier: Modifier,
    postWithUser: PostWithUser,
    navController: NavController,
    deletable: Boolean,
    dataViewModel: DataViewModel
) {
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
            if (deletable) {
                IconButton(onClick = {
                    dataViewModel.removePost(postWithUser.post.postId)
                    Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show()
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
            withStyle(
                style = SpanStyle(
                    color = Color.Blue,
                    textDecoration = TextDecoration.Underline
                )
            ) {
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
                .firstOrNull()?.let { stringAnnotation ->
                    uriHandler.openUri(stringAnnotation.item)
                }
        }
    )
}
