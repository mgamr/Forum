package com.example.testforum.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.testforum.DataViewModel
import com.example.testforum.data.Comment
import com.example.testforum.data.CommentWithUser
import com.example.testforum.data.Post

@Composable
fun SinglePostPage(modifier: Modifier, navController: NavController, dataViewModel: DataViewModel) {
    val post = navController.previousBackStackEntry?.savedStateHandle?.get<Post>("post")
    val comments by dataViewModel.comments.collectAsState()
    val commentWithUser by dataViewModel.commentsWithUsers.collectAsState()
    LaunchedEffect(post) {
        post?.let {
            dataViewModel.fetchComments(it.postId)
        }
    }

    LazyColumn(modifier = modifier.fillMaxSize()) {
        post?.let {
            item {
                SinglePostMainPart(modifier = modifier, post = post, navController = navController)
            }
        }

        items(commentWithUser) {commentWithUser ->
            CommentItem(commentWithUser = commentWithUser, navController = navController)
//            DisplayComment(modifier, comment = comment, dataViewModel = dataViewModel, navController = navController)
        }

    }

}

@Composable
fun CommentItem(commentWithUser: CommentWithUser, navController: NavController) {
//    Column(modifier = Modifier.padding(8.dp)) {
//        Text(text = commentWithUser.user.displayName, fontWeight = FontWeight.Bold)
//        Text(text = commentWithUser.comment.)
//    }
    Row() {
        IconButton(onClick = {
            commentWithUser.user.email.let { email ->
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
            commentWithUser.user.displayName?.let { text = it
            } ?: run { commentWithUser.user.username?.let { text = it } }
            Text(
                text = text,
                color = Color.Gray
            )
            Text(text = commentWithUser.comment.commentContent)
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