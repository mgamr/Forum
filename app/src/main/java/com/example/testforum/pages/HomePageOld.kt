package com.example.testforum.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.testforum.AuthState
import com.example.testforum.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInClient

@Composable
fun HomePageOld(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel, googleSignInClient: GoogleSignInClient) {
    val authState = authViewModel.authState.observeAsState()
    val user by authViewModel.user.observeAsState()

    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Home Page", fontSize = 32.sp)

        user?.let {
            Text(text = "Welcome ${it.username ?: "User"}", fontSize = 32.sp)
            Text(text = "Email: ${it.email}", fontSize = 20.sp)
            it.displayName?.let { displayName ->
                Text(text = "Display Name: $displayName", fontSize = 20.sp)
            }
            it.profilePicture?.let { profilePicture ->
                Image(
                    painter = rememberAsyncImagePainter(profilePicture),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(128.dp)
                        .clip(CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = {
            authViewModel.signout(googleSignInClient)
        }) {
            Text(text = "Sign out")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            user?.email?.let { email ->
                navController.currentBackStackEntry?.savedStateHandle?.set("userEmail", email)
                navController.navigate("profile")
            }
        }) {
            Text(text = "Go to Profile")
        }
    }

}