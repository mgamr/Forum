package com.example.testforum.pages

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.testforum.DataViewModel
import com.example.testforum.TopicRepository
import com.example.testforum.data.Topic


@Composable
fun ExpandableTopicList(navController: NavController,) {
    val topicRepository = TopicRepository()
    var topics by remember { mutableStateOf(emptyList<Topic>()) }

    LaunchedEffect(Unit) {
        try {
            topics = topicRepository.fetchTopics()
        } catch (e: Exception) {
            Log.e("ExpandableList", "Error fetching topics", e)
        }
    }

    if (topics.isNotEmpty()) {
        LazyColumn {
            items(topics) { topic ->
                ExpandableTopicItem(topic){ topicName ->
                    navController.navigate("posts/$topicName")
                }
            }
        }
    } else {
        Text("No topics available")
    }
}


@Composable
fun ExpandableTopicItem(topic: Topic,onTopicClick: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val isExpandable = topic.subtopics?.isNotEmpty() == true

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isExpandable) {
                IconButton(
                    onClick = { expanded = !expanded }
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ArrowDropDown else Icons.Default.KeyboardArrowRight,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
            } else {
                Box(modifier = Modifier.size(46.dp))
            }

            Text(
                text = topic.name,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
                    .clickable { onTopicClick(topic.name) }
            )
        }

        if (expanded && isExpandable) {
            topic.subtopics?.let { subtopicsMap ->
                Column(modifier = Modifier.padding(start = 32.dp)) {
                    subtopicsMap.forEach { (_, subtopic) ->
                        ExpandableTopicItem(subtopic, onTopicClick)
                    }
                }
            }
        }
    }
}
