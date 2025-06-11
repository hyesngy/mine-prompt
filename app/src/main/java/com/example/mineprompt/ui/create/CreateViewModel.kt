package com.example.mineprompt.ui.create

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mineprompt.data.CategoryType
import com.example.mineprompt.data.DatabaseHelper
import com.example.mineprompt.data.PromptLength
import com.example.mineprompt.data.PromptStyle
import com.example.mineprompt.data.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.content.ContentValues
import android.util.Log
import com.example.mineprompt.utils.ToastUtils

class CreateViewModel(application: Application) : AndroidViewModel(application) {

    private val databaseHelper = DatabaseHelper(application)
    private val userPreferences = UserPreferences(application)

    private val _text = MutableLiveData<String>().apply {
        value = "생성 화면입니다"
    }
    val text: LiveData<String> = _text

    private val _isGenerating = MutableLiveData<Boolean>()
    val isGenerating: LiveData<Boolean> = _isGenerating

    private val _generationResult = MutableLiveData<Result<Long>>()
    val generationResult: LiveData<Result<Long>> = _generationResult

    companion object {
        private const val TAG = "CreateViewModel"
    }

    fun generatePrompt(request: PromptGenerationRequest) {
        if (_isGenerating.value == true) return

        _isGenerating.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // AI 생성 시뮬레이션 (실제로는 OpenAI API 호출)
                val generatedPrompt = simulateAIGeneration(request)

                // 생성된 프롬프트를 DB에 저장
                val promptId = saveGeneratedPrompt(generatedPrompt, request)

                if (promptId != -1L) {
                    _generationResult.postValue(Result.success(promptId))
                } else {
                    _generationResult.postValue(Result.failure(Exception("프롬프트 저장 실패")))
                }

            } catch (e: Exception) {
                Log.e(TAG, "프롬프트 생성 실패", e)
                ToastUtils.showGeneralError(getApplication())
                _generationResult.postValue(Result.failure(e))
            } finally {
                _isGenerating.postValue(false)
            }
        }
    }

    private suspend fun simulateAIGeneration(request: PromptGenerationRequest): String {
        // AI 생성 시뮬레이션 (2-3초 딜레이)
        delay(2500)

        // 실제로는 OpenAI API 호출하여 프롬프트 생성
        // 지금은 더미 데이터로 시뮬레이션
        return generateDummyPrompt(request)
    }

    private fun generateDummyPrompt(request: PromptGenerationRequest): String {
        val lengthText = when (request.length) {
            "SHORT" -> "간단하게"
            "MEDIUM" -> "상세하게"
            "LONG" -> "구체적이고 포괄적으로"
            else -> "적절하게"
        }

        val styleText = when (request.style) {
            "창의적" -> "창의적이고 혁신적인 접근으로"
            "공식적" -> "공식적이고 전문적인 어조로"
            "논리적" -> "논리적이고 체계적인 방식으로"
            "감성적" -> "감성적이고 공감적인 톤으로"
            "전문적" -> "전문적이고 기술적인 관점에서"
            else -> "자연스러운 방식으로"
        }

        return buildString {
            append("[${request.purpose}]에 대해 ${lengthText} ${styleText} ")
            append("답변해주세요. ")

            if (request.keywords.isNotEmpty()) {
                append("다음 키워드들을 포함해주세요: ${request.keywords}. ")
            }

            if (request.language != "한국어") {
                append("답변은 ${request.language}로 작성해주세요.")
            }
        }
    }

    private fun saveGeneratedPrompt(generatedPrompt: String, request: PromptGenerationRequest): Long {
        val db = databaseHelper.writableDatabase
        val currentUserId = userPreferences.getUserId()

        try {
            val values = ContentValues().apply {
                put("title", generateTitle(request.purpose))
                put("content", generatedPrompt)
                put("purpose", request.purpose)
                put("keywords", request.keywords)
                put("length", request.length)
                put("style", request.style)
                put("language", request.language)
                put("creator_id", currentUserId)
                put("like_count", 0)
                put("view_count", 0)
                put("is_active", 1)
                put("created_at", System.currentTimeMillis())
                put("updated_at", System.currentTimeMillis())
            }

            val promptId = db.insert("prompts", null, values)

            if (promptId != -1L) {
                // 자동으로 카테고리 매핑
                assignCategoriesToPrompt(promptId, request)
            }

            return promptId

        } catch (e: Exception) {
            Log.e(TAG, "프롬프트 저장 실패", e)
            return -1L
        }
    }

    private fun generateTitle(purpose: String): String {
        // 목적에서 제목 자동 생성
        val cleanPurpose = purpose.take(20).trim()
        return if (cleanPurpose.length < purpose.length) {
            "$cleanPurpose..."
        } else {
            cleanPurpose
        }
    }

    private fun assignCategoriesToPrompt(promptId: Long, request: PromptGenerationRequest) {
        // 키워드와 목적을 기반으로 카테고리 자동 매핑
        val keywords = request.keywords.lowercase() + " " + request.purpose.lowercase()
        val assignedCategories = mutableSetOf<Long>()

        // 간단한 키워드 매칭으로 카테고리 결정
        when {
            keywords.contains("콘텐츠") || keywords.contains("유튜브") || keywords.contains("영상") -> {
                assignedCategories.add(CategoryType.CONTENT_CREATION.id)
            }
            keywords.contains("비즈니스") || keywords.contains("사업") || keywords.contains("회사") -> {
                assignedCategories.add(CategoryType.BUSINESS.id)
            }
            keywords.contains("마케팅") || keywords.contains("광고") || keywords.contains("홍보") -> {
                assignedCategories.add(CategoryType.MARKETING.id)
            }
            keywords.contains("글쓰기") || keywords.contains("소설") || keywords.contains("창작") -> {
                assignedCategories.add(CategoryType.WRITING.id)
            }
            keywords.contains("개발") || keywords.contains("코딩") || keywords.contains("프로그래밍") -> {
                assignedCategories.add(CategoryType.DEVELOPMENT.id)
            }
            keywords.contains("학습") || keywords.contains("공부") || keywords.contains("교육") -> {
                assignedCategories.add(CategoryType.LEARNING.id)
            }
            keywords.contains("생산성") || keywords.contains("효율") || keywords.contains("관리") -> {
                assignedCategories.add(CategoryType.PRODUCTIVITY.id)
            }
            keywords.contains("자기계발") || keywords.contains("성장") || keywords.contains("발전") -> {
                assignedCategories.add(CategoryType.SELF_DEVELOPMENT.id)
            }
            keywords.contains("언어") || keywords.contains("영어") || keywords.contains("번역") -> {
                assignedCategories.add(CategoryType.LANGUAGE.id)
            }
            keywords.contains("재미") || keywords.contains("유머") || keywords.contains("엔터") -> {
                assignedCategories.add(CategoryType.FUN.id)
            }
            keywords.contains("일상") || keywords.contains("생활") || keywords.contains("취미") -> {
                assignedCategories.add(CategoryType.DAILY.id)
            }
            keywords.contains("법률") || keywords.contains("재무") || keywords.contains("금융") -> {
                assignedCategories.add(CategoryType.LEGAL.id)
            }
            else -> {
                // 기본 카테고리 (일반적인 경우)
                assignedCategories.add(CategoryType.DAILY.id)
            }
        }

        // DB에 카테고리 매핑 저장
        val db = databaseHelper.writableDatabase
        assignedCategories.forEach { categoryId ->
            try {
                val values = ContentValues().apply {
                    put("prompt_id", promptId)
                    put("category_id", categoryId)
                }
                db.insert("prompt_categories", null, values)
            } catch (e: Exception) {
                Log.e(TAG, "카테고리 매핑 저장 실패: categoryId=$categoryId", e)
            }
        }
    }
}