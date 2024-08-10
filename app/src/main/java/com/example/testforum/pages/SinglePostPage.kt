package com.example.testforum.pages

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.magnifier
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.testforum.AuthState
import com.example.testforum.AuthViewModel
import com.example.testforum.DataViewModel
import com.example.testforum.R
import com.example.testforum.data.Comment
import com.example.testforum.data.CommentWithUser
import com.example.testforum.data.PostWithUser
import com.google.android.gms.auth.api.signin.GoogleSignInClient


@Composable
fun SinglePostPage(modifier: Modifier, navController: NavController, dataViewModel: DataViewModel, authViewModel: AuthViewModel, googleSignInClient: GoogleSignInClient) {
    val postWithUser = navController.previousBackStackEntry?.savedStateHandle?.get<PostWithUser>("postWithUser")
//    val userEmail = navController.previousBackStackEntry?.savedStateHandle?.get<PostWithUser>("userEmail")
    val commentWithUser by dataViewModel.commentsWithUsers.collectAsState()
    val photos by dataViewModel.photoList.collectAsState()

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
//                            navController.currentBackStackEntry?.savedStateHandle?.let {
//                                it["postWithUser"] = postWithUser
//                            }
//                            navController.navigate("singlePost")
//                            {
//                                popUpTo("singlePost") { inclusive = true }
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

    LaunchedEffect(postWithUser?.post) {
        postWithUser?.post?.let {
            dataViewModel.fetchComments(it.postId)
        }
    }
    LaunchedEffect(postWithUser?.post) {
        postWithUser?.post?.let {
            dataViewModel.getPhotos(postWithUser.post.postId)
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
                        Row() {
                            for (photo in photos) {
                                val painter = if (photo.imagePath.isNullOrEmpty()) {
                                    painterResource(id = R.drawable.default_empty_profile)
                                } else {
                                    rememberAsyncImagePainter(model = photo.imagePath)
                                }
                                Image(
                                    painter = painter,
                                    contentDescription = "Profile picture",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .padding(start = 14.dp, top = 8.dp)
                                        .clip(CircleShape)
                                )
                            }
                        }

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
                CommentItem(commentWithUser = commentWithUser, dataViewModel = dataViewModel, navController = navController, deletable = deletable2, postId = postWithUser!!.post.postId)
//            DisplayComment(modifier, comment = comment, dataViewModel = dataViewModel, navController = navController)
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
//                                Icon(Icons.Filled.Add, contentDescription = null)
        }
    }


}

@Composable
fun CommentItem(
    commentWithUser: CommentWithUser,
    navController: NavController,
    deletable: Boolean,
    dataViewModel: DataViewModel,
    postId: String
) {
    val context = LocalContext.current
//    Column(modifier = Modifier.padding(8.dp)) {
//        Text(text = commentWithUser.user.displayName, fontWeight = FontWeight.Bold)
//        Text(text = commentWithUser.comment.)
//    }
    Row(modifier = Modifier.fillMaxWidth()
        .padding(14.dp)
        .border(
            width = 1.dp,
            color = Color.DarkGray,
            shape = RoundedCornerShape(8.dp)
        )
    ) {
        val painter = if (commentWithUser.user.profilePicture.isNullOrEmpty()) {
            painterResource(id = R.drawable.default_empty_profile)
        } else {
            rememberAsyncImagePainter(model = commentWithUser.user.profilePicture)
        }
        Image(
            painter = painter,
            contentDescription = "Profile picture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(35.dp)
                .padding(start = 14.dp, top = 8.dp)
                .clip(CircleShape)
                .clickable {
                    commentWithUser.user.email.let { email ->
                        navController.navigate("userProfile/$email")
                    }
                }
        )
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
                dataViewModel.removeComment(commentWithUser.comment.commentId, postId)
                Toast.makeText(context, "Comment deleted", Toast.LENGTH_SHORT).show()

            }) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null)
            }
        }
//        if(deletable) {
//            IconButton(onClick = {
//                commentWithUser.user.email.let { email ->
//                    navController.navigate("userProfile/$email")
//                }
//            }) {
//                Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null)
//            }
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