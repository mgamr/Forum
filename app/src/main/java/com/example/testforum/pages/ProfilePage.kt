package com.example.testforum.pages

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.testforum.AuthState
import com.example.testforum.AuthViewModel
import com.example.testforum.R
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePage(
    modifier: Modifier = Modifier,
    userEmail: String,
    navController: NavController,
    authViewModel: AuthViewModel,
    googleSignInClient: GoogleSignInClient
) {
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
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    var editUser by remember { mutableStateOf(false) }
    var editUsername by remember { mutableStateOf<String?>(user?.username) }
    var editDisplayName by remember { mutableStateOf<String?>(user?.displayName) }

    LaunchedEffect(user) {
        editUsername = user?.username
        editDisplayName = user?.displayName
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

        user?.let {
            UserProfileImage(user = it) { updatedUser ->
                user = updatedUser
            }
        }

        user?.username?.let {
            Text(
                text = "Username: $it",
                textAlign = TextAlign.Center,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
        user?.displayName?.let {
            Text(
                text = "Display name: $it",
                textAlign = TextAlign.Center,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
        Button(onClick = { editUser = true }) {
            Text(text = "Edit")
        }
        Button(onClick = { navController.navigate("userProfile/" + user?.email) }) {
            Text(text = "View my profile as guest")
        }
    }
}


@Composable
fun UserProfileImage(user: User, onUpdateUser: (User) -> Unit) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val db = Firebase.firestore
    val users = db.collection("users")

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                uploadImageToFirebase(uri, context, user.email) { downloadUrl ->
                    user.profilePicture = downloadUrl
                    users.document(user.email)
                        .set(user, SetOptions.merge())
                        .addOnSuccessListener {
                            onUpdateUser(user)
                            Toast.makeText(
                                context,
                                "Profile updated successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                context,
                                "Failed to update profile: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
        }

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
            .size(150.dp)
            .clip(CircleShape)
            .clickable {
                launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
            .padding(vertical = 16.dp)
    )
}

fun uploadImageToFirebase(
    uri: Uri?,
    context: Context,
    userEmail: String,
    onSuccess: (String) -> Unit
) {
    val storage = FirebaseStorage.getInstance()
    val storageReference = storage.reference
//    val uniqueID = UUID.randomUUID().toString()
//    val imageReference = storageReference.child("profileImages/$uniqueID")
    val imageReference = storageReference.child("profileImages/$userEmail")

    val uploadTask = uri.let { imageReference.putFile(it!!) }

    uploadTask.addOnSuccessListener {
        imageReference.downloadUrl.addOnSuccessListener { downloadUrl ->
            onSuccess(downloadUrl.toString())
        }
    }.addOnFailureListener {
        Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
    }
}

