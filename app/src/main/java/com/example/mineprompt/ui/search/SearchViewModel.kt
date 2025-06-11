package com.example.mineprompt.ui.search

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mineprompt.data.PromptRepository
import com.example.mineprompt.ui.common.adapter.PromptCardItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.content.edit

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val promptRepository = PromptRepository(application)
    private val sharedPrefs = application.getSharedPreferences("search_prefs", Context.MODE_PRIVATE)

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

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        // 최근 검색어 로드
        loadRecentSearches()

        // 인기 검색어 로드
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val popularKeywords = promptRepository.getPopularSearches()
                val popularItems = popularKeywords.mapIndexed { index, keyword ->
                    PopularSearchItem(
                        rank = index + 1,
                        keyword = keyword,
                        trend = when (index % 3) {
                            0 -> TrendType.UP
                            1 -> TrendType.DOWN
                            else -> TrendType.SAME
                        }
                    )
                }

                viewModelScope.launch(Dispatchers.Main) {
                    _popularSearches.value = popularItems
                }
            } catch (e: Exception) {
                viewModelScope.launch(Dispatchers.Main) {
                    _popularSearches.value = emptyList()
                }
            }
        }

        _isSearching.value = false
        _activeFilters.value = emptyList()
    }

    private fun loadRecentSearches() {
        val recentSet = sharedPrefs.getStringSet("recent_searches", emptySet()) ?: emptySet()
        _recentSearches.value = recentSet.toList().take(10) // 최대 10개
    }

    fun performSearch(query: String, selectedCategories: List<String> = emptyList()) {
        if (query.trim().isEmpty()) return

        _currentSearchQuery.value = query
        _isSearching.value = true
        _isLoading.value = true

        // 최근 검색어에 추가
        addToRecentSearches(query)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val results = promptRepository.searchPrompts(query, selectedCategories)

                viewModelScope.launch(Dispatchers.Main) {
                    _searchResults.value = results
                    _isLoading.value = false

                    // 검색 결과 기반으로 활성 필터 설정
                    updateActiveFilters(query, selectedCategories, results)
                }

            } catch (e: Exception) {
                viewModelScope.launch(Dispatchers.Main) {
                    _searchResults.value = emptyList()
                    _isLoading.value = false
                }
            }
        }
    }

    private fun updateActiveFilters(query: String, categories: List<String>, results: List<PromptCardItem>) {
        val filters = mutableListOf<SearchFilter>()

        // 검색어 필터
        if (query.isNotEmpty()) {
            filters.add(SearchFilter("검색: $query", FilterType.SEARCH, true))
        }

        // 카테고리 필터들
        categories.forEach { category ->
            filters.add(SearchFilter(category, FilterType.CATEGORY, true))
        }

        // 결과 개수가 있으면 기본 필터 추가
        if (results.isNotEmpty()) {
            filters.add(SearchFilter("인기순", FilterType.SORT, true))
        }

        _activeFilters.value = filters
    }

    fun addToRecentSearches(query: String) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) return

        val currentSearches = _recentSearches.value?.toMutableList() ?: mutableListOf()

        // 중복 제거
        currentSearches.remove(trimmedQuery)
        // 맨 앞에 추가
        currentSearches.add(0, trimmedQuery)

        // 최대 10개까지만 유지
        if (currentSearches.size > 10) {
            currentSearches.removeAt(currentSearches.size - 1)
        }

        _recentSearches.value = currentSearches

        // SharedPreferences에 저장
        sharedPrefs.edit() {
            putStringSet("recent_searches", currentSearches.toSet())
        }
    }

    fun removeRecentSearch(query: String) {
        val currentSearches = _recentSearches.value?.toMutableList() ?: return
        currentSearches.remove(query)
        _recentSearches.value = currentSearches

        // SharedPreferences에 저장
        sharedPrefs.edit() {
            putStringSet("recent_searches", currentSearches.toSet())
        }
    }

    fun clearAllRecentSearches() {
        _recentSearches.value = emptyList()
        sharedPrefs.edit() { remove("recent_searches") }
    }

    fun removeFilter(filter: SearchFilter) {
        val currentFilters = _activeFilters.value?.toMutableList() ?: return
        currentFilters.remove(filter)
        _activeFilters.value = currentFilters

        // 필터 제거 시 재검색
        when (filter.type) {
            FilterType.CATEGORY -> {
                // 카테고리 필터 제거 시 해당 카테고리 없이 재검색
                val remainingCategories = currentFilters
                    .filter { it.type == FilterType.CATEGORY }
                    .map { it.name }

                _currentSearchQuery.value?.let { query ->
                    if (query.isNotEmpty()) {
                        performSearch(query, remainingCategories)
                    }
                }
            }
            FilterType.SEARCH -> {
                // 검색어 필터 제거 시 검색 화면으로 돌아가기
                _isSearching.value = false
                _searchResults.value = emptyList()
                _activeFilters.value = emptyList()
            }
            else -> {
                // 기타 필터는 현재 상태 유지
            }
        }
    }

    fun togglePromptLike(promptId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newLikeState = promptRepository.togglePromptLike(promptId)

                // 검색 결과 UI 업데이트
                viewModelScope.launch(Dispatchers.Main) {
                    val currentResults = _searchResults.value?.toMutableList() ?: return@launch
                    val updatedList = currentResults.map { prompt ->
                        if (prompt.id == promptId) {
                            prompt.copy(
                                isLiked = newLikeState,
                                likeCount = if (newLikeState) prompt.likeCount + 1 else prompt.likeCount - 1
                            )
                        } else {
                            prompt
                        }
                    }
                    _searchResults.value = updatedList
                }

            } catch (e: Exception) {
                // 좋아요 실패 시 에러 처리
            }
        }
    }

    fun backToSearch() {
        _isSearching.value = false
        _searchResults.value = emptyList()
        _activeFilters.value = emptyList()
        _currentSearchQuery.value = ""
    }
}