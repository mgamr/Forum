package com.example.testforum

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.testforum.data.User
import com.example.testforum.pages.HomePage
import com.example.testforum.pages.LogInPage
import com.example.testforum.pages.ProfilePage
import com.example.testforum.pages.SignUpPage
import com.google.android.gms.auth.api.signin.GoogleSignInClient

@Composable
fun MyAppNavigation(modifier: Modifier = Modifier, authViewModel: AuthViewModel, googleSignIn: () -> Unit, googleSignInClient: GoogleSignInClient){
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login", builder = {
        composable("login"){
            LogInPage(modifier, navController, authViewModel, googleSignIn)
        }
        composable("signup"){
            SignUpPage(modifier, navController, authViewModel)
        }
        composable("home"){
            HomePage(modifier, navController, authViewModel, googleSignInClient)
        }
        composable("profile") {
            val userEmail = navController.previousBackStackEntry?.savedStateHandle?.get<String>("userEmail")
            userEmail?.let {
                ProfilePage(modifier, it, navController, authViewModel, googleSignInClient)
            }
        }
    })
}