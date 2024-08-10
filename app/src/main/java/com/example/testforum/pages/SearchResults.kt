package com.example.testforum.pages

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.testforum.viewmodels.AuthViewModel
import com.example.testforum.viewmodels.DataViewModel
import com.example.testforum.viewmodels.TopicViewModel
import com.example.testforum.data.PostWithUser
import com.google.android.gms.auth.api.signin.GoogleSignInClient

@Composable
fun SearchResults(
    modifier: Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    dataViewModel: DataViewModel,
    topicViewModel: TopicViewModel,
    googleSignInClient: GoogleSignInClient,

    ) {
    val query = navController.previousBackStackEntry?.savedStateHandle?.get<String>("query")
    query?.let {
        val filteredPostsWithUsers by dataViewModel.postsWithUsers.collectAsState()
        val filteredUsers by dataViewModel.filteredUsers.collectAsState()

        var topics by remember { mutableStateOf(listOf("")) }

        var searchInput by remember { mutableStateOf("") }

        val tabTitles = listOf("users", "posts", "topics")
        var selectedTab by remember {
            mutableStateOf(0)
        }

        LaunchedEffect(query) {
            try {
                topics = topicViewModel.getAllTopicNames(query)
            } catch (e: Exception) {
                Log.e("search results", "Error fetching topics", e)
            }
        }

        LaunchedEffect(query) {
            try {
                dataViewModel.getFilteredPostsWithUsers(query, true)
            } catch (e: Exception) {
                Log.e("search results", "Error fetching users", e)
            }
        }

        LaunchedEffect(Unit) {
            try {
                dataViewModel.getFilteredUsers(query)
            } catch (e: Exception) {
                Log.e("search results", "Error fetching posts", e)
            }
        }


        Column(
//        modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
//            modifier = modifier.verticalScroll(rememberScrollState()),
        ) {
            TopBar(
                "Forum",
                navController = navController,
                authViewModel = authViewModel,
                googleSignInClient = googleSignInClient
            )
            Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))

            Box(
                modifier = androidx.compose.ui.Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(BottomAppBarDefaults.bottomAppBarFabColor)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = androidx.compose.ui.Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = searchInput,
                        onValueChange = {
                            searchInput = it
                        },
                        textStyle = LocalTextStyle.current.copy(color = Color.Black),
                        shape = RoundedCornerShape(percent = 50),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent
                        ),
                        placeholder = {
                            Text("Search...")
                        },
                        singleLine = true
                    )
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        modifier = androidx.compose.ui.Modifier
                            .size(30.dp)
                            .clickable {
                                navController.currentBackStackEntry?.savedStateHandle?.set(
                                    "query",
                                    searchInput
                                )
                                navController.navigate("searchResults")
                            }
                    )
                }
            }

            TabRow(selectedTabIndex = selectedTab) {
                tabTitles.forEachIndexed { index, s ->
                    Tab(selected = index == selectedTab, onClick = {
                        selectedTab = index
                    },
                        text = {
                            Text(text = s)
                        })
                }
            }
            if(selectedTab == 0) {
                Column(modifier = modifier.verticalScroll(rememberScrollState())) {
                    for( filteredUser in filteredUsers) {
                        Box(modifier = Modifier.clickable {
                            navController.navigate("userProfile/${filteredUser.email}")
                        }) {
                            UserProfile(filteredUser)
                        }

                    }
                }
            } else if (selectedTab == 1) {
                ViewPosts(modifier = Modifier, filteredPostsWithUsers, navController, authViewModel, dataViewModel)

            } else if(selectedTab == 2) {
                SearchResultsList(topics)
            }

        }
    }
}

@Composable
fun SearchResultsList(items: List<String>) {
    items.forEach { item ->
        Box(modifier = Modifier.clickable {

        }) {
            Text(
                text = item,
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

    }
}