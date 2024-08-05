package com.example.testforum.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.testforum.AuthViewModel
import com.example.testforum.data.Post
import com.example.testforum.data.User
import com.google.android.gms.auth.api.signin.GoogleSignInClient
//
//val user1 = User("test1@gmail.com", "test1", "boy", "https://png.pngtree.com/png-vector/20240310/ourmid/pngtree-cute-boy-with-glasses-looking-through-the-hole-in-wall-illustration-png-image_11892420.png")
//val user2 = User("test2@gmail.com", "test2", "girl", "https://images.creativefabrica.com/products/previews/2023/10/27/lAkc4qpxK/2XMhZ2mYG72ItgIPElZEFUUBR8K-mobile.jpg")
//val postsList = listOf(
//    Post("This is the first post content.", user1),
//    Post("Another post by Test User 2.", user2),
//    Post("Lorem ipsum dolor sit amet.", user1),
//    Post("Test User 2's third post.", user2),
//    Post("Final post for testing purposes.", user1)
//)
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ForumPage(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel, googleSignInClient: GoogleSignInClient) {
//    Scaffold(
//        bottomBar = {
//            BottomAppBar(
//                containerColor = Color.Transparent,
//                actions = {},
//                floatingActionButton = {
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(bottom = 16.dp)
//                            .padding(start = 16.dp)
//                            .padding(end = 4.dp),
//                        contentAlignment = Alignment.BottomEnd
//                    ) {
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.End
//                        ) {
//                            FloatingActionButton(
//                                onClick = { "add" },
//                                modifier = Modifier.size(120.dp),
//                                containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
//                                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
//                            ) {
//                                Text("Add Post")
////                                Icon(Icons.Filled.Add, contentDescription = null)
//                            }
//                        }
//                    }
//                }
//            )
//        },
//    ) { innerPadding ->
//
//        Column(modifier = modifier.fillMaxSize().padding(innerPadding)) {
//            TopAppBar(
//                title = {
//                    Text(
//                        "Forum",
//                        maxLines = 1,
//                        overflow = TextOverflow.Ellipsis
//                    )
//                },
//                navigationIcon = {
//                    IconButton(onClick = { navController.popBackStack() }) {
//                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
//                    }
//                },
//                actions = {
//                    IconButton(onClick = { navController.navigate("profile") }) {
//                        Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null)
//                    }
//                    IconButton(onClick = { authViewModel.signout(googleSignInClient) }) {
//                        Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null)
//                    }
//                }
//            )
//
//            ViewPosts(modifier = Modifier.padding(innerPadding))
//        }
//    }
//}
//
//@Composable
//fun ViewPosts(modifier: Modifier = Modifier) {
//    LazyColumn(
//        modifier = modifier.fillMaxSize(),
//        content = {
//            items(postsList) { post ->
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(8.dp)
//                ) {
//                    Text(
//                        text = post.postContent,
//                        modifier = Modifier.padding(bottom = 4.dp)
//                    )
//                    Text(
//                        text = "By: ${post.user.displayName ?: post.user.username ?: "Unknown"}",
//                        color = Color.Gray
//                    )
//                }
//            }
//            if (postsList.isEmpty()) {
//                item {
//                    Text(
//                        modifier = Modifier.fillMaxWidth(),
//                        textAlign = TextAlign.Center,
//                        text = "No items yet",
//                        fontSize = 16.sp
//                    )
//                }
//            }
//        }
//    )
//}


//@Preview(showBackground = true)
//@Composable
//fun ForumPreview() {
//    ForumPage()
//}