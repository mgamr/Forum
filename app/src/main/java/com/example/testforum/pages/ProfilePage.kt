package com.example.testforum.pages

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.testforum.data.User
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import com.example.testforum.AuthState
import com.example.testforum.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePage(modifier: Modifier = Modifier, userEmail: String, navController: NavController, authViewModel: AuthViewModel, googleSignInClient: GoogleSignInClient) {
    val db = Firebase.firestore
    val users = db.collection("users")

    var user by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(userEmail) {
        users.document(userEmail).get().addOnSuccessListener { documentSnapshot ->
            user = documentSnapshot.toObject<User>()
        }.addOnFailureListener { exception ->
            Log.e("ProfilePage", "Error fetching user data", exception)
        }
    }

    val authState = authViewModel.authState.observeAsState()

    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    var editUser by remember { mutableStateOf(false) }
    var editUsername by remember { mutableStateOf<String?>(user?.username) }
    var editDisplayName by remember { mutableStateOf<String?>(user?.displayName) }
    var editProfilePicture by remember { mutableStateOf<String?>(user?.profilePicture) }

    LaunchedEffect(user) {
        editUsername = user?.username
        editDisplayName = user?.displayName
        editProfilePicture = user?.profilePicture
    }

    if (editUser) {
        AlertDialog(
            onDismissRequest = { editUser = false },
            title = { Text(text = "Edit User") },
            text = {
                Column {
                    TextField(
                        value = editUsername ?: "",
                        onValueChange = { editUsername = it },
                        label = { Text(text = "Username") }
                    )
                    TextField(
                        value = editDisplayName ?: "",
                        onValueChange = { editDisplayName = it },
                        label = { Text(text = "Display Name") }
                    )
                    TextField(
                        value = editProfilePicture ?: "",
                        onValueChange = { editProfilePicture = it },
                        label = { Text(text = "Profile Picture URL") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editUsername?.isNotBlank() == true) {
                            user?.username = editUsername
                        }
                        if (editDisplayName?.isNotBlank() == true) {
                            user?.displayName = editDisplayName
                        }
                        if (editProfilePicture?.isNotBlank() == true) {
                            user?.profilePicture = editProfilePicture
                        }
                        user?.let {
                            users.document(userEmail).set(it, SetOptions.merge())
                        }
                        editUser = false
                    }
                ) {
                    Text(text = "Done")
                }
            },
            dismissButton = {
                Button(onClick = { editUser = false }) {
                    Text(text = "Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(
            title = {
                Text(
                    "Profile",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() } ) {
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

        user?.profilePicture?.let {
            AsyncImage(
                model = it,
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        user?.username?.let {
            Text(
                text = "Username: $it",
                textAlign = TextAlign.Center,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        user?.displayName?.let {
            Text(
                text = "Display name: $it",
                textAlign = TextAlign.Center,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        Button(onClick = { editUser = true }) {
            Text(text = "Edit")
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun ProfilePreview() {
//    val user = User("test@gmail.com", "testUser", "test displayName", "https://static.vecteezy.com/system/resources/previews/019/900/322/non_2x/happy-young-cute-illustration-face-profile-png.png")
//    ProfilePage(userData = user)
//}


// https://developer.android.com/develop/ui/compose/components/app-bars-navigate
//NavHost(navController, startDestination = "home") {
//    composable("topBarNavigationExample") {
//        TopBarNavigationExample{ navController.popBackStack() }
//    }