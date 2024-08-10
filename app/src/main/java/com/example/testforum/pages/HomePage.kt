package com.example.testforum.pages

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.testforum.viewmodels.AuthState
import com.example.testforum.viewmodels.AuthViewModel
import com.example.testforum.viewmodels.DataViewModel
import com.example.testforum.MainActivity
import com.example.testforum.viewmodels.TopicViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import kotlin.system.exitProcess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    dataViewModel: DataViewModel,
    topicViewModel: TopicViewModel,
    googleSignInClient: GoogleSignInClient
) {
    TopicsPage(modifier, navController, authViewModel, topicViewModel, googleSignInClient)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    text: String,
    navController: NavController,
    authViewModel: AuthViewModel,
    googleSignInClient: GoogleSignInClient
) {
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
                if (!navController.popBackStack()) {
                    val activity: MainActivity = MainActivity()
                    activity.finish()
                    exitProcess(0)
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

