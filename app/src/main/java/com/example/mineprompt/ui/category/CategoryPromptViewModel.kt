package com.example.mineprompt.ui.category

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

class CategoryPromptViewModel(application: Application) : AndroidViewModel(application) {

    private val promptRepository = PromptRepository(application)

    private val _prompts = MutableLiveData<List<PromptCardItem>>()
    val prompts: LiveData<List<PromptCardItem>> = _prompts

    private val _selectedCategories = MutableLiveData<List<CategoryFilterItem>>()
    val selectedCategories: LiveData<List<CategoryFilterItem>> = _selectedCategories

    private val _currentSortFilter = MutableLiveData<String>()
    val currentSortFilter: LiveData<String> = _currentSortFilter

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var currentFilterType: CategoryFilterType = CategoryFilterType.ALL
    private var currentSortType: CategorySortType = CategorySortType.RECOMMENDED
    private var currentCategoryId: Long? = null
    private var currentCategoryName: String? = null

    init {
        _currentSortFilter.value = "추천순"
        _selectedCategories.value = getCategoryFilterItems()
    }

    fun loadPrompts(
        filterType: CategoryFilterType,
        sortType: CategorySortType,
        categoryId: Long? = null,
        categoryName: String? = null
    ) {
        currentFilterType = filterType
        currentSortType = sortType
        currentCategoryId = categoryId
        currentCategoryName = categoryName

        // 정렬 필터 텍스트 설정
        _currentSortFilter.value = when (sortType) {
            CategorySortType.RECOMMENDED -> "추천순"
            CategorySortType.POPULAR -> "인기순"
            CategorySortType.LATEST -> "최신순"
        }

        // 단일 카테고리인 경우 해당 카테고리 선택 상태로 설정
        if (filterType == CategoryFilterType.SINGLE_CATEGORY && categoryId != null) {
            val updatedCategories = _selectedCategories.value?.map { category ->
                category.copy(isSelected = category.id == categoryId)
            } ?: getCategoryFilterItems()
            _selectedCategories.value = updatedCategories
        }

        performSearch()
    }

    private fun performSearch() {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val prompts = when (currentFilterType) {
                    CategoryFilterType.ALL -> {
                        // 전체 카테고리에서 선택된 카테고리로 필터링
                        val selectedCategoryNames = _selectedCategories.value
                            ?.filter { it.isSelected }
                            ?.map { it.name } ?: emptyList()

                        promptRepository.getPromptsByCategory(selectedCategoryNames, currentSortType)
                    }
                    CategoryFilterType.RECOMMENDED -> {
                        // 사용자 추천 (관심 카테고리 기반)
                        promptRepository.getRecommendedPromptsByUserInterest(currentSortType)
                    }
                    CategoryFilterType.SINGLE_CATEGORY -> {
                        // 특정 카테고리
                        val categoryNames = if (currentCategoryName != null) {
                            listOf(currentCategoryName!!)
                        } else {
                            emptyList()
                        }
                        promptRepository.getPromptsByCategory(categoryNames, currentSortType)
                    }
                }

                viewModelScope.launch(Dispatchers.Main) {
                    _prompts.value = prompts
                    _isLoading.value = false
                }

            } catch (e: Exception) {
                viewModelScope.launch(Dispatchers.Main) {
                    _error.value = "프롬프트를 불러오는 중 오류가 발생했습니다."
                    _prompts.value = emptyList()
                    _isLoading.value = false
                }
            }
        }
    }

    fun setSortFilter(sortFilter: String) {
        _currentSortFilter.value = sortFilter

        currentSortType = when (sortFilter) {
            "인기순" -> CategorySortType.POPULAR
            "최신순" -> CategorySortType.LATEST
            else -> CategorySortType.RECOMMENDED
        }

        performSearch()
    }

    fun applyCategoryFilters(categories: List<CategoryFilterItem>) {
        _selectedCategories.value = categories
        performSearch()
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
        performSearch()
    }

    fun clearAllFilters() {
        val currentCategories = _selectedCategories.value?.toMutableList() ?: return
        val clearedCategories = currentCategories.map { it.copy(isSelected = false) }
        _selectedCategories.value = clearedCategories
        performSearch()
    }

    fun togglePromptLike(promptId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newLikeState = promptRepository.togglePromptLike(promptId)

                viewModelScope.launch(Dispatchers.Main) {
                    val currentPrompts = _prompts.value?.toMutableList() ?: return@launch
                    val updatedList = currentPrompts.map { prompt ->
                        if (prompt.id == promptId) {
                            prompt.copy(
                                isLiked = newLikeState,
                                likeCount = if (newLikeState) prompt.likeCount + 1 else prompt.likeCount - 1
                            )
                        } else {
                            prompt
                        }
                    }
                    _prompts.value = updatedList
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