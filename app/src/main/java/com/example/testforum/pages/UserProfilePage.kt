package com.example.testforum.pages

import android.content.ContentValues
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.testforum.AuthState
import com.example.testforum.AuthViewModel
import com.example.testforum.R
import com.example.testforum.data.Post
import com.example.testforum.data.User
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfilePage(
    modifier: Modifier = Modifier,
    userEmail: String,
    navController: NavController,
    authViewModel: AuthViewModel,
    googleSignInClient: GoogleSignInClient
) {
    val db = Firebase.firestore
    val users = db.collection("users")
    val posts = db.collection("posts")

    var user by remember { mutableStateOf<User?>(null) }
    val postsList = remember { mutableStateListOf<Post>() }

    LaunchedEffect(userEmail) {
        users.document(userEmail).get().addOnSuccessListener { documentSnapshot ->
            user = documentSnapshot.toObject<User>()
        }.addOnFailureListener { exception ->
            Log.e("ProfilePage", "Error fetching user data", exception)
        }
    }

    LaunchedEffect(userEmail) {
        posts.whereEqualTo("user.email", userEmail).get()
            .addOnSuccessListener { result ->
                postsList.clear()
                for (document in result) {
                    val post = document.toObject(Post::class.java)
                    postsList.add(post)
                }
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }

    val authState = authViewModel.authState.observeAsState()

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }


    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(
            title = {
                Text(
                    "User Profile",
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
                IconButton(onClick = { navController.navigate("home") }) {
                    Icon(imageVector = Icons.Default.Home, contentDescription = null)
                }
                IconButton(onClick = { authViewModel.signout(googleSignInClient) }) {
                    Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null)
                }
            }
        )

        user?.let { UserProfile(it) }
        ViewPosts(modifier = Modifier.padding(4.dp), postsList, navController, user)
    }
}

@Composable
fun UserProfile(user: User) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val painter = if (user.profilePicture.isNullOrEmpty()) {
            painterResource(id = R.drawable.default_empty_profile)
        } else {
            rememberAsyncImagePainter(model = user.profilePicture)
        }

        Image(
            painter = painter,
            contentDescription = "Profile picture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .padding(vertical = 16.dp)
        )

        Column {
            Text(
                text = "Username: " + user.username,
                textAlign = TextAlign.Left,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Display name: " + user.displayName,
                textAlign = TextAlign.Left,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

