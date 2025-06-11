package com.example.mineprompt.ui.likes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mineprompt.data.PromptRepository
import com.example.mineprompt.ui.common.adapter.PromptCardItem
import com.example.mineprompt.ui.search.CategoryFilterItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LikesViewModel(application: Application) : AndroidViewModel(application) {

    private val promptRepository = PromptRepository(application)

    private val _likedPrompts = MutableLiveData<List<PromptCardItem>>()
    val likedPrompts: LiveData<List<PromptCardItem>> = _likedPrompts

    private val _selectedCategories = MutableLiveData<List<CategoryFilterItem>>()
    val selectedCategories: LiveData<List<CategoryFilterItem>> = _selectedCategories

    private val _currentSortFilter = MutableLiveData<String>()
    val currentSortFilter: LiveData<String> = _currentSortFilter

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var currentSortOrder: SortOrder = SortOrder.LIKED_DATE

    enum class SortOrder {
        LIKED_DATE,   // 담은순 (좋아요한 날짜순)
        LATEST,       // 최신순 (프롬프트 생성순)
        POPULARITY    // 인기순 (좋아요 개수순)
    }

    init {
        _currentSortFilter.value = "담은순"
        _selectedCategories.value = getCategoryFilterItems()
    }

    fun loadLikedPrompts() {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 선택된 카테고리 이름들 추출
                val selectedCategoryNames = _selectedCategories.value
                    ?.filter { it.isSelected }
                    ?.map { it.name } ?: emptyList()

                // 좋아요한 프롬프트 조회
                val prompts = promptRepository.getLikedPrompts(selectedCategoryNames, currentSortOrder)

                viewModelScope.launch(Dispatchers.Main) {
                    _likedPrompts.value = prompts
                    _isLoading.value = false
                }

            } catch (e: Exception) {
                viewModelScope.launch(Dispatchers.Main) {
                    _error.value = "좋아요 목록을 불러오는 중 오류가 발생했습니다."
                    _likedPrompts.value = emptyList()
                    _isLoading.value = false
                }
            }
        }
    }

    fun refreshLikedPrompts() {
        loadLikedPrompts()
    }

    fun setSortFilter(sortFilter: String) {
        _currentSortFilter.value = sortFilter

        currentSortOrder = when (sortFilter) {
            "최신순" -> SortOrder.LATEST
            "인기순" -> SortOrder.POPULARITY
            else -> SortOrder.LIKED_DATE
        }

        loadLikedPrompts()
    }

    fun applyCategoryFilters(categories: List<CategoryFilterItem>) {
        _selectedCategories.value = categories
        loadLikedPrompts()
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
        loadLikedPrompts()
    }

    fun clearAllFilters() {
        val currentCategories = _selectedCategories.value?.toMutableList() ?: return
        val clearedCategories = currentCategories.map { it.copy(isSelected = false) }
        _selectedCategories.value = clearedCategories
        loadLikedPrompts()
    }

    fun hasActiveFilters(): Boolean {
        return _selectedCategories.value?.any { it.isSelected } ?: false
    }

    fun togglePromptLike(promptId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newLikeState = promptRepository.togglePromptLike(promptId)

                // 좋아요 취소된 경우 목록에서 제거, 좋아요 추가된 경우는 이미 목록에 있으므로 업데이트
                viewModelScope.launch(Dispatchers.Main) {
                    val currentPrompts = _likedPrompts.value?.toMutableList() ?: return@launch

                    if (!newLikeState) {
                        // 좋아요 취소 시 목록에서 제거
                        val updatedList = currentPrompts.filter { it.id != promptId }
                        _likedPrompts.value = updatedList
                    } else {
                        // 좋아요 추가 시 좋아요 개수 업데이트
                        val updatedList = currentPrompts.map { prompt ->
                            if (prompt.id == promptId) {
                                prompt.copy(
                                    isLiked = true,
                                    likeCount = prompt.likeCount + 1
                                )
                            } else {
                                prompt
                            }
                        }
                        _likedPrompts.value = updatedList
                    }
                }

            } catch (e: Exception) {
                viewModelScope.launch(Dispatchers.Main) {
                    _error.value = "좋아요 처리 중 오류가 발생했습니다."
                }
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    private fun getCategoryFilterItems(): List<CategoryFilterItem> {
        return com.example.mineprompt.data.CategoryType.entries.map { categoryType ->
            CategoryFilterItem(
                id = categoryType.id,
                name = categoryType.displayName,
                iconRes = getCategoryIcon(categoryType),
                isSelected = false
            )
        }
    }

    private fun getCategoryIcon(categoryType: com.example.mineprompt.data.CategoryType): Int {
        return when (categoryType) {
            com.example.mineprompt.data.CategoryType.CONTENT_CREATION -> com.example.mineprompt.R.drawable.ic_category_content_creation_32dp
            com.example.mineprompt.data.CategoryType.BUSINESS -> com.example.mineprompt.R.drawable.ic_category_business_32dp
            com.example.mineprompt.data.CategoryType.MARKETING -> com.example.mineprompt.R.drawable.ic_category_marketing_32dp
            com.example.mineprompt.data.CategoryType.WRITING -> com.example.mineprompt.R.drawable.ic_category_writing_32dp
            com.example.mineprompt.data.CategoryType.DEVELOPMENT -> com.example.mineprompt.R.drawable.ic_category_development_32dp
            com.example.mineprompt.data.CategoryType.LEARNING -> com.example.mineprompt.R.drawable.ic_category_learning_32dp
            com.example.mineprompt.data.CategoryType.PRODUCTIVITY -> com.example.mineprompt.R.drawable.ic_category_productivity_32dp
            com.example.mineprompt.data.CategoryType.SELF_DEVELOPMENT -> com.example.mineprompt.R.drawable.ic_category_self_development_32dp
            com.example.mineprompt.data.CategoryType.LANGUAGE -> com.example.mineprompt.R.drawable.ic_category_language_32dp
            com.example.mineprompt.data.CategoryType.FUN -> com.example.mineprompt.R.drawable.ic_category_fun_32dp
            com.example.mineprompt.data.CategoryType.DAILY -> com.example.mineprompt.R.drawable.ic_category_daily_32dp
            com.example.mineprompt.data.CategoryType.LEGAL -> com.example.mineprompt.R.drawable.ic_category_legal_32dp
        }
    }
}