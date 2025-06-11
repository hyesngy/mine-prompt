package com.example.mineprompt.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mineprompt.data.PromptRepository
import com.example.mineprompt.ui.common.adapter.PromptCardItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val promptRepository = PromptRepository(application)

    private val _trendingCurations = MutableLiveData<List<TrendingCurationItem>>()
    val trendingCurations: LiveData<List<TrendingCurationItem>> = _trendingCurations

    private val _weeklyPopularPrompts = MutableLiveData<List<WeeklyPopularPromptItem>>()
    val weeklyPopularPrompts: LiveData<List<WeeklyPopularPromptItem>> = _weeklyPopularPrompts

    private val _recommendedPrompts = MutableLiveData<List<PromptCardItem>>()
    val recommendedPrompts: LiveData<List<PromptCardItem>> = _recommendedPrompts

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadAllData()
    }

    private fun loadAllData() {
        _isLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 병렬로 데이터 로드
                val trendingJob = launch { loadTrendingCurations() }
                val weeklyJob = launch { loadWeeklyPopularPrompts() }
                val recommendedJob = launch { loadRecommendedPrompts() }

                // 모든 작업 완료 대기
                trendingJob.join()
                weeklyJob.join()
                recommendedJob.join()

            } finally {
                viewModelScope.launch(Dispatchers.Main) {
                    _isLoading.value = false
                }
            }
        }
    }

    private fun loadTrendingCurations() {
        try {
            val curations = promptRepository.getTrendingCurations()
            viewModelScope.launch(Dispatchers.Main) {
                _trendingCurations.value = curations
            }
        } catch (e: Exception) {
            // 에러 시 빈 리스트
            viewModelScope.launch(Dispatchers.Main) {
                _trendingCurations.value = emptyList()
            }
        }
    }

    private fun loadWeeklyPopularPrompts() {
        try {
            val prompts = promptRepository.getWeeklyPopularPrompts()
            viewModelScope.launch(Dispatchers.Main) {
                _weeklyPopularPrompts.value = prompts
            }
        } catch (e: Exception) {
            viewModelScope.launch(Dispatchers.Main) {
                _weeklyPopularPrompts.value = emptyList()
            }
        }
    }

    private fun loadRecommendedPrompts() {
        try {
            val prompts = promptRepository.getRecommendedPrompts()
            viewModelScope.launch(Dispatchers.Main) {
                _recommendedPrompts.value = prompts
            }
        } catch (e: Exception) {
            viewModelScope.launch(Dispatchers.Main) {
                _recommendedPrompts.value = emptyList()
            }
        }
    }

    fun togglePromptLike(promptId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newLikeState = promptRepository.togglePromptLike(promptId)

                // UI 업데이트 (추천 프롬프트)
                viewModelScope.launch(Dispatchers.Main) {
                    val currentRecommended = _recommendedPrompts.value?.toMutableList() ?: return@launch
                    val updatedList = currentRecommended.map { prompt ->
                        if (prompt.id == promptId) {
                            prompt.copy(
                                isLiked = newLikeState,
                                likeCount = if (newLikeState) prompt.likeCount + 1 else prompt.likeCount - 1
                            )
                        } else {
                            prompt
                        }
                    }
                    _recommendedPrompts.value = updatedList
                }

                // UI 업데이트 (주간 인기 프롬프트)
                viewModelScope.launch(Dispatchers.Main) {
                    val currentWeekly = _weeklyPopularPrompts.value?.toMutableList() ?: return@launch
                    val updatedWeekly = currentWeekly.map { prompt ->
                        if (prompt.id == promptId) {
                            prompt.copy(
                                isLiked = newLikeState,
                                likeCount = if (newLikeState) prompt.likeCount + 1 else prompt.likeCount - 1
                            )
                        } else {
                            prompt
                        }
                    }
                    _weeklyPopularPrompts.value = updatedWeekly
                }

            } catch (e: Exception) {

            }
        }
    }

    fun refreshData() {
        loadAllData()
    }
}