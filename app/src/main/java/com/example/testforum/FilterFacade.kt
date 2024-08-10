package com.example.testforum

class FilterFacade(
    private val dataViewModel: DataViewModel,
    private val topicViewModel: TopicViewModel
) {
    fun setSearchQuery(query: String) {
        dataViewModel.getFilteredPostsWithUsers(query, true)
        dataViewModel.getFilteredUsers(query)
    }
}