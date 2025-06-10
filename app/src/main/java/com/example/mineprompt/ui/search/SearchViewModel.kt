package com.example.mineprompt.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mineprompt.ui.common.adapter.PromptCardItem

class SearchViewModel : ViewModel() {

    private val _recentSearches = MutableLiveData<List<String>>()
    val recentSearches: LiveData<List<String>> = _recentSearches

    private val _popularSearches = MutableLiveData<List<PopularSearchItem>>()
    val popularSearches: LiveData<List<PopularSearchItem>> = _popularSearches

    private val _searchResults = MutableLiveData<List<PromptCardItem>>()
    val searchResults: LiveData<List<PromptCardItem>> = _searchResults

    private val _isSearching = MutableLiveData<Boolean>()
    val isSearching: LiveData<Boolean> = _isSearching

    private val _currentSearchQuery = MutableLiveData<String>()
    val currentSearchQuery: LiveData<String> = _currentSearchQuery

    private val _activeFilters = MutableLiveData<List<SearchFilter>>()
    val activeFilters: LiveData<List<SearchFilter>> = _activeFilters

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        // 최근 검색어 로드 (실제로는 SharedPreferences나 DB에서)
        _recentSearches.value = listOf(
            "영어 회화", "일본어 회화", "중국어 회화", "검색어1", "검색어2", "검색어검색어검색어검색어", "검색어"
        )

        // 인기 검색어 로드
        _popularSearches.value = listOf(
            PopularSearchItem(1, "콘텐츠 제작", TrendType.UP),
            PopularSearchItem(2, "콘텐츠 제작", TrendType.UP),
            PopularSearchItem(3, "콘텐츠 제작", TrendType.DOWN),
            PopularSearchItem(4, "콘텐츠 제작", TrendType.UP),
            PopularSearchItem(5, "콘텐츠 제작", TrendType.UP),
            PopularSearchItem(6, "콘텐츠 제작", TrendType.UP),
            PopularSearchItem(7, "콘텐츠 제작", TrendType.DOWN),
            PopularSearchItem(8, "콘텐츠 제작", TrendType.UP),
            PopularSearchItem(9, "콘텐츠 제작", TrendType.UP),
            PopularSearchItem(10, "콘텐츠 제작", TrendType.DOWN)
        )

        _isSearching.value = false
        _activeFilters.value = emptyList()
    }

    fun performSearch(query: String) {
        _currentSearchQuery.value = query
        _isSearching.value = true

        // 최근 검색어에 추가
        addToRecentSearches(query)

        // 실제 검색 수행 (목업 데이터)
        val mockResults = listOf(
            PromptCardItem(
                id = 1,
                title = "단계별 코드 작성 연습 프롬프트",
                content = "학습/강의목적를 한 줄씩 자상하게, 단계별 의식스로운 연습을 수 있는 피드백 시스템입니다",
                creatorName = "세성",
                createdDate = "2025년 5월 21일",
                likeCount = 12,
                viewCount = 245,
                categories = listOf("개발", "학습"),
                isLiked = false
            ),
            PromptCardItem(
                id = 2,
                title = "단계별 코드 작성 연습 프롬프트",
                content = "학습/강의목적를 한 줄씩 자상하게, 단계별 의식스로운 연습을 수 있는 피드백 시스템입니다",
                creatorName = "세성",
                createdDate = "2025년 5월 21일",
                likeCount = 12,
                viewCount = 245,
                categories = listOf("개발", "학습"),
                isLiked = true
            ),
            PromptCardItem(
                id = 3,
                title = "단계별 코드 작성 연습 프롬프트",
                content = "학습/강의목적를 한 줄씩 자상하게, 단계별 의식스로운 연습을 수 있는 피드백 시스템입니다",
                creatorName = "세성",
                createdDate = "2025년 5월 21일",
                likeCount = 12,
                viewCount = 245,
                categories = listOf("개발", "학습"),
                isLiked = false
            )
        )

        _searchResults.value = mockResults

        // 기본 필터 설정
        _activeFilters.value = listOf(
            SearchFilter("전체", FilterType.CATEGORY, isActive = true),
            SearchFilter("개발", FilterType.CATEGORY, isActive = true),
            SearchFilter("생산성", FilterType.CATEGORY, isActive = true),
            SearchFilter("자기계발", FilterType.CATEGORY, isActive = true)
        )
    }

    fun addToRecentSearches(query: String) {
        val currentSearches = _recentSearches.value?.toMutableList() ?: mutableListOf()

        // 중복 제거
        currentSearches.remove(query)
        // 맨 앞에 추가
        currentSearches.add(0, query)

        // 최대 10개까지만 유지
        if (currentSearches.size > 10) {
            currentSearches.removeAt(currentSearches.size - 1)
        }

        _recentSearches.value = currentSearches
    }

    fun removeRecentSearch(query: String) {
        val currentSearches = _recentSearches.value?.toMutableList() ?: return
        currentSearches.remove(query)
        _recentSearches.value = currentSearches
    }

    fun clearAllRecentSearches() {
        _recentSearches.value = emptyList()
    }

    fun removeFilter(filter: SearchFilter) {
        val currentFilters = _activeFilters.value?.toMutableList() ?: return
        currentFilters.remove(filter)
        _activeFilters.value = currentFilters

        // 필터 변경 시 재검색
        _currentSearchQuery.value?.let { query ->
            if (query.isNotEmpty()) {
                performSearch(query)
            }
        }
    }

    fun togglePromptLike(promptId: Long) {
        val currentResults = _searchResults.value?.toMutableList() ?: return
        val updatedList = currentResults.map { prompt ->
            if (prompt.id == promptId) {
                prompt.copy(
                    isLiked = !prompt.isLiked,
                    likeCount = if (prompt.isLiked) prompt.likeCount - 1 else prompt.likeCount + 1
                )
            } else {
                prompt
            }
        }
        _searchResults.value = updatedList
    }
}

// 데이터 클래스들
data class PopularSearchItem(
    val rank: Int,
    val keyword: String,
    val trend: TrendType
)

data class SearchFilter(
    val name: String,
    val type: FilterType,
    val isActive: Boolean = false
)

enum class TrendType {
    UP, DOWN, SAME
}

enum class FilterType {
    CATEGORY, SORT, LENGTH, STYLE
}