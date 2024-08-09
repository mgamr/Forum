package com.example.testforum

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.testforum.data.User
import com.example.testforum.pages.ExpandableTopicList
import com.example.testforum.pages.HomePage
import com.example.testforum.pages.LogInPage
import com.example.testforum.pages.PostsPage
import com.example.testforum.pages.ProfilePage
import com.example.testforum.pages.SignUpPage
import com.example.testforum.pages.SinglePostPage
import com.example.testforum.pages.UserProfilePage
import com.example.testforum.pages.ViewPosts
import com.google.android.gms.auth.api.signin.GoogleSignInClient

@Composable
fun MyAppNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    dataViewModel: DataViewModel,
    googleSignIn: () -> Unit,
    googleSignInClient: GoogleSignInClient
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home", builder = {
        composable("login") {
            LogInPage(modifier, navController, authViewModel, googleSignIn)
        }
        composable("signup") {
            SignUpPage(modifier, navController, authViewModel)
        }

        composable("home") {
            HomePage(modifier, navController, authViewModel, dataViewModel, googleSignInClient)
        }
        composable("profile") {
            val userEmail =
                navController.previousBackStackEntry?.savedStateHandle?.get<String>("userEmail")
            userEmail?.let {
                ProfilePage(modifier, it, navController, authViewModel, googleSignInClient)
            }
        }
        composable("singlePost") {
            SinglePostPage(
                modifier,
                navController,
                dataViewModel = dataViewModel,
                authViewModel = authViewModel,
                googleSignInClient = googleSignInClient
            )
        }
        composable(
            "userProfile/{userEmail}",
            arguments = listOf(navArgument("userEmail") { type = NavType.StringType })
        ) { backStackEntry ->
            val userEmail = backStackEntry.arguments?.getString("userEmail")
            userEmail?.let {
                UserProfilePage(
                    modifier,
                    it,
                    navController,
                    authViewModel,
                    googleSignInClient,
                    dataViewModel
                )
            }
        }
        composable("topics") {
            ExpandableTopicList(navController)
        }
        composable(
            "posts/{topicName}",
            arguments = listOf(navArgument("topicName") { type = NavType.StringType })
        ) { backStackEntry ->
            val topicName = backStackEntry.arguments?.getString("topicName")
            topicName?.let {
                PostsPage(modifier, it, navController, authViewModel, dataViewModel, googleSignInClient)
            }
        }
    })
}