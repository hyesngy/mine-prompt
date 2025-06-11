package com.example.mineprompt.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mineprompt.data.PromptRepository
import com.example.mineprompt.ui.common.adapter.PromptCardItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchResultViewModel(application: Application) : AndroidViewModel(application) {

    private val promptRepository = PromptRepository(application)

    private val _searchResults = MutableLiveData<List<PromptCardItem>>()
    val searchResults: LiveData<List<PromptCardItem>> = _searchResults

    private val _activeFilters = MutableLiveData<List<SearchFilter>>()
    val activeFilters: LiveData<List<SearchFilter>> = _activeFilters

    private val _selectedCategories = MutableLiveData<List<CategoryFilterItem>>()
    val selectedCategories: LiveData<List<CategoryFilterItem>> = _selectedCategories

    private val _currentSortFilter = MutableLiveData<String>()
    val currentSortFilter: LiveData<String> = _currentSortFilter

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var currentQuery: String = ""
    private var currentSortOrder: SortOrder = SortOrder.POPULARITY

    enum class SortOrder {
        POPULARITY,  // 인기순
        LATEST,      // 최신순
        LIKES,       // 좋아요순
        VIEWS        // 조회순
    }

    init {
        _currentSortFilter.value = "인기순"
        _selectedCategories.value = emptyList()
        _activeFilters.value = emptyList()
    }

    fun performSearch(query: String) {
        currentQuery = query
        _isLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 선택된 카테고리 이름들 추출
                val selectedCategoryNames = _selectedCategories.value
                    ?.filter { it.isSelected }
                    ?.map { it.name } ?: emptyList()

                // 정렬 방식에 따라 검색
                val results = searchWithSort(query, selectedCategoryNames, currentSortOrder)

                viewModelScope.launch(Dispatchers.Main) {
                    _searchResults.value = results
                    _isLoading.value = false
                    updateActiveFilters(query, selectedCategoryNames)
                }

            } catch (e: Exception) {
                viewModelScope.launch(Dispatchers.Main) {
                    _searchResults.value = emptyList()
                    _isLoading.value = false
                }
            }
        }
    }

    private fun searchWithSort(
        query: String,
        categories: List<String>,
        sortOrder: SortOrder
    ): List<PromptCardItem> {
        // 기본 검색은 PromptRepository에서 수행하고, 여기서 정렬만 추가 적용
        val baseResults = promptRepository.searchPrompts(query, categories)

        return when (sortOrder) {
            SortOrder.POPULARITY -> baseResults.sortedByDescending { it.likeCount }
            SortOrder.LATEST -> baseResults.sortedByDescending {
                when {
                    it.createdDate.contains("분 전") -> 100000
                    it.createdDate.contains("시간 전") -> 10000
                    it.createdDate.contains("일 전") -> {
                        val days = it.createdDate.replace("일 전", "").toIntOrNull() ?: 0
                        1000 - days
                    }
                    it.createdDate.contains("주 전") -> {
                        val weeks = it.createdDate.replace("주 전", "").toIntOrNull() ?: 0
                        100 - (weeks * 7)
                    }
                    else -> 0
                }
            }
            SortOrder.LIKES -> baseResults.sortedByDescending { it.likeCount }
            SortOrder.VIEWS -> baseResults.sortedByDescending { it.viewCount }
        }
    }

    private fun updateActiveFilters(query: String, categories: List<String>) {
        val filters = mutableListOf<SearchFilter>()

        // 검색어 필터
        if (query.isNotEmpty()) {
            filters.add(SearchFilter("검색: $query", FilterType.SEARCH, true))
        }

        // 카테고리 필터들
        categories.forEach { category ->
            filters.add(SearchFilter(category, FilterType.CATEGORY, true))
        }

        // 정렬 필터
        filters.add(SearchFilter(_currentSortFilter.value ?: "인기순", FilterType.SORT, true))

        _activeFilters.value = filters
    }

    fun removeFilter(filter: SearchFilter) {
        when (filter.type) {
            FilterType.CATEGORY -> {
                // 카테고리 필터 제거
                val currentCategories = _selectedCategories.value?.toMutableList() ?: return
                val updatedCategories = currentCategories.map { category ->
                    if (category.name == filter.name) {
                        category.copy(isSelected = false)
                    } else {
                        category
                    }
                }
                _selectedCategories.value = updatedCategories
            }
            FilterType.SEARCH -> {
                // 검색어 필터는 제거하지 않음 (뒤로가기로 처리)
                return
            }
            FilterType.SORT -> {
                // 정렬 필터는 기본값으로 리셋
                setSortFilter("인기순")
                return
            }
            else -> return
        }

        // 필터 변경 후 재검색
        if (currentQuery.isNotEmpty()) {
            performSearch(currentQuery)
        }
    }

    fun setSortFilter(sortFilter: String) {
        _currentSortFilter.value = sortFilter

        currentSortOrder = when (sortFilter) {
            "최신순" -> SortOrder.LATEST
            "좋아요순" -> SortOrder.LIKES
            "조회순" -> SortOrder.VIEWS
            else -> SortOrder.POPULARITY
        }

        // 정렬 변경 시 재검색
        if (currentQuery.isNotEmpty()) {
            performSearch(currentQuery)
        }
    }

    fun applyCategoryFilters(categories: List<CategoryFilterItem>) {
        _selectedCategories.value = categories

        // 카테고리 필터 변경 시 재검색
        if (currentQuery.isNotEmpty()) {
            performSearch(currentQuery)
        }
    }

    fun removeCategoryFilter(categoryId: Long) {
        val currentCategories = _selectedCategories.value?.toMutableList() ?: return
        val updatedCategories = currentCategories.map { category ->
            if (category.id == categoryId) {
                category.copy(isSelected = false)
            } else {
                category
            }
        }
        _selectedCategories.value = updatedCategories

        // 필터 변경 시 재검색
        if (currentQuery.isNotEmpty()) {
            performSearch(currentQuery)
        }
    }

    fun updateSelectedCategories(categories: List<CategoryFilterItem>) {
        _selectedCategories.value = categories

        // 카테고리 필터 변경 시 재검색
        if (currentQuery.isNotEmpty()) {
            performSearch(currentQuery)
        }
    }

    fun togglePromptLike(promptId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newLikeState = promptRepository.togglePromptLike(promptId)

                // UI 업데이트
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
}