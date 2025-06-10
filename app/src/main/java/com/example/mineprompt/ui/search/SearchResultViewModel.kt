package com.example.mineprompt.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mineprompt.ui.common.adapter.PromptCardItem

class SearchResultViewModel : ViewModel() {

    private val _searchResults = MutableLiveData<List<PromptCardItem>>()
    val searchResults: LiveData<List<PromptCardItem>> = _searchResults

    private val _activeFilters = MutableLiveData<List<SearchFilter>>()
    val activeFilters: LiveData<List<SearchFilter>> = _activeFilters

    private val _currentSortFilter = MutableLiveData<String>()
    val currentSortFilter: LiveData<String> = _currentSortFilter

    private var currentQuery: String = ""

    init {
        _currentSortFilter.value = "인기순"
        _activeFilters.value = listOf(
            SearchFilter("전체", FilterType.CATEGORY, isActive = true),
            SearchFilter("개발", FilterType.CATEGORY, isActive = true),
            SearchFilter("생산성", FilterType.CATEGORY, isActive = true),
            SearchFilter("자기계발", FilterType.CATEGORY, isActive = true)
        )
    }

    fun performSearch(query: String) {
        currentQuery = query

        // 실제 검색 수행 (목업 데이터)
        val mockResults = generateSearchResults(query)
        _searchResults.value = mockResults
    }

    private fun generateSearchResults(query: String): List<PromptCardItem> {
        // 검색어에 따른 목업 결과 생성
        return if (query.contains("코드") || query.contains("개발")) {
            listOf(
                PromptCardItem(
                    id = 1,
                    title = "단계별 코드 작성 연습 프롬프트",
                    content = "학습/강의목적을 한 줄씩 자상하게, 단계별 의식적으로운 연습을 수 있는 피드백 시스템입니다",
                    creatorName = "세성",
                    createdDate = "2025년 5월 21일",
                    likeCount = 12,
                    viewCount = 245,
                    categories = listOf("개발", "학습"),
                    isLiked = false
                ),
                PromptCardItem(
                    id = 2,
                    title = "코드 리뷰 자동화 프롬프트",
                    content = "코드의 품질을 자동으로 검토하고 개선사항을 제안하는 프롬프트입니다",
                    creatorName = "개발자",
                    createdDate = "2025년 5월 20일",
                    likeCount = 28,
                    viewCount = 189,
                    categories = listOf("개발", "생산성"),
                    isLiked = true
                ),
                PromptCardItem(
                    id = 3,
                    title = "알고리즘 문제 해결 가이드",
                    content = "복잡한 알고리즘 문제를 단계별로 해결하는 방법을 안내하는 프롬프트",
                    creatorName = "알고리즘러",
                    createdDate = "2025년 5월 19일",
                    likeCount = 45,
                    viewCount = 312,
                    categories = listOf("개발", "학습"),
                    isLiked = false
                )
            )
        } else if (query.contains("글쓰기") || query.contains("창작")) {
            listOf(
                PromptCardItem(
                    id = 4,
                    title = "창의적 글쓰기 프롬프트",
                    content = "상상력을 자극하고 창의성을 발휘할 수 있는 글쓰기 주제들을 제공합니다",
                    creatorName = "작가지망생",
                    createdDate = "2025년 5월 18일",
                    likeCount = 67,
                    viewCount = 423,
                    categories = listOf("글쓰기/창작", "자기계발"),
                    isLiked = false
                ),
                PromptCardItem(
                    id = 5,
                    title = "소설 캐릭터 개발 가이드",
                    content = "매력적이고 입체적인 소설 캐릭터를 만드는 방법을 안내합니다",
                    creatorName = "소설가",
                    createdDate = "2025년 5월 17일",
                    likeCount = 89,
                    viewCount = 567,
                    categories = listOf("글쓰기/창작", "콘텐츠 제작"),
                    isLiked = true
                )
            )
        } else {
            // 일반적인 검색 결과
            listOf(
                PromptCardItem(
                    id = 6,
                    title = "검색 결과 프롬프트 1",
                    content = "검색어 '$query'와 관련된 프롬프트입니다. 다양한 상황에서 활용할 수 있습니다.",
                    creatorName = "사용자1",
                    createdDate = "2025년 5월 16일",
                    likeCount = 23,
                    viewCount = 156,
                    categories = listOf("일반", "기타"),
                    isLiked = false
                ),
                PromptCardItem(
                    id = 7,
                    title = "검색 결과 프롬프트 2",
                    content = "'$query' 관련 두 번째 프롬프트입니다. 실용적인 활용이 가능합니다.",
                    creatorName = "사용자2",
                    createdDate = "2025년 5월 15일",
                    likeCount = 34,
                    viewCount = 201,
                    categories = listOf("일반", "생산성"),
                    isLiked = false
                )
            )
        }
    }

    fun removeFilter(filter: SearchFilter) {
        val currentFilters = _activeFilters.value?.toMutableList() ?: return
        currentFilters.remove(filter)
        _activeFilters.value = currentFilters

        // 필터 변경 시 재검색
        if (currentQuery.isNotEmpty()) {
            performSearch(currentQuery)
        }
    }

    fun setSortFilter(sortFilter: String) {
        _currentSortFilter.value = sortFilter

        // 정렬 변경 시 재검색
        if (currentQuery.isNotEmpty()) {
            performSearch(currentQuery)
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