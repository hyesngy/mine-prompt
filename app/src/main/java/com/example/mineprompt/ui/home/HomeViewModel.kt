package com.example.mineprompt.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mineprompt.R
import com.example.mineprompt.ui.common.adapter.PromptCardItem

class HomeViewModel : ViewModel() {

    private val _trendingCurations = MutableLiveData<List<TrendingCurationItem>>()
    val trendingCurations: LiveData<List<TrendingCurationItem>> = _trendingCurations

    private val _weeklyPopularPrompts = MutableLiveData<List<WeeklyPopularPromptItem>>()
    val weeklyPopularPrompts: LiveData<List<WeeklyPopularPromptItem>> = _weeklyPopularPrompts

    private val _recommendedPrompts = MutableLiveData<List<PromptCardItem>>()
    val recommendedPrompts: LiveData<List<PromptCardItem>> = _recommendedPrompts

    init {
        loadTrendingCurations()
        loadWeeklyPopularPrompts()
        loadRecommendedPrompts()
    }

    private fun loadTrendingCurations() {
        // 임시 데이터
        val curations = listOf(
            TrendingCurationItem(
                id = 1,
                title = "AI 프롬프트 작성법",
                imageRes = R.drawable.placeholder_curation
            ),
            TrendingCurationItem(
                id = 2,
                title = "창작 영감 모음집",
                imageRes = R.drawable.placeholder_curation
            ),
            TrendingCurationItem(
                id = 3,
                title = "비즈니스 필수 템플릿",
                imageRes = R.drawable.placeholder_curation
            )
        )
        _trendingCurations.value = curations
    }

    private fun loadWeeklyPopularPrompts() {
        // 임시 데이터
        val prompts = listOf(
            WeeklyPopularPromptItem(
                id = 1,
                rank = 1,
                title = "효과적인 회의 진행 가이드",
                creatorName = "마인프롬프트",
                likeCount = 89,
                category = "비즈니스"
            ),
            WeeklyPopularPromptItem(
                id = 2,
                rank = 2,
                title = "매력적인 유튜브 썸네일 기획",
                creatorName = "나그네",
                likeCount = 67,
                category = "콘텐츠 제작"
            ),
            WeeklyPopularPromptItem(
                id = 3,
                rank = 3,
                title = "코드 리뷰 체크리스트 생성",
                creatorName = "아무개",
                likeCount = 73,
                category = "개발"
            ),
            WeeklyPopularPromptItem(
                id = 4,
                rank = 4,
                title = "효율적인 공부 계획 수립",
                creatorName = "마인프롬프트",
                likeCount = 45,
                category = "학습"
            )
        )
        _weeklyPopularPrompts.value = prompts
    }

    private fun loadRecommendedPrompts() {
        // 임시 데이터
        val prompts = listOf(
            PromptCardItem(
                id = 5,
                title = "소설 캐릭터 심화 개발",
                content = "[장르] 소설의 [주인공 역할] 캐릭터를 개발해주세요. [성격 키워드]와 [배경 설정]을 바탕으로...",
                creatorName = "나그네",
                createdDate = "5일 전",
                likeCount = 73,
                viewCount = 324,
                categories = listOf("글쓰기/창작", "콘텐츠 제작")
            ),
            PromptCardItem(
                id = 1,
                title = "효과적인 회의 진행 가이드",
                content = "[회의 목적]에 맞는 효율적인 회의를 진행하기 위해 [참석자 수]명의 [직급/역할]을 고려하여...",
                creatorName = "마인프롬프트",
                createdDate = "1일 전",
                likeCount = 34,
                viewCount = 156,
                categories = listOf("비즈니스", "생산성")
            ),
            PromptCardItem(
                id = 3,
                title = "코드 리뷰 체크리스트 생성",
                content = "[프로그래밍 언어]와 [프로젝트 유형]에 맞는 코드 리뷰 체크리스트를 작성해주세요...",
                creatorName = "아무개",
                createdDate = "3일 전",
                likeCount = 89,
                viewCount = 412,
                categories = listOf("개발", "생산성")
            )
        )
        _recommendedPrompts.value = prompts
    }

    fun togglePromptLike(promptId: Long) {
        // 좋아요 토글 로직
        val currentRecommended = _recommendedPrompts.value?.toMutableList() ?: return
        val updatedList = currentRecommended.map { prompt ->
            if (prompt.id == promptId) {
                prompt.copy(
                    isLiked = !prompt.isLiked,
                    likeCount = if (prompt.isLiked) prompt.likeCount - 1 else prompt.likeCount + 1
                )
            } else {
                prompt
            }
        }
        _recommendedPrompts.value = updatedList

        val currentWeekly = _weeklyPopularPrompts.value?.toMutableList() ?: return
        val updatedWeekly = currentWeekly.map { prompt ->
            if (prompt.id == promptId) {
                prompt.copy(
                    isLiked = !prompt.isLiked,
                    likeCount = if (prompt.isLiked) prompt.likeCount - 1 else prompt.likeCount + 1
                )
            } else {
                prompt
            }
        }
        _weeklyPopularPrompts.value = updatedWeekly
    }
}