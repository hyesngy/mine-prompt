package com.example.mineprompt.ui.create

import android.app.Application
import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mineprompt.data.CategoryType
import com.example.mineprompt.data.DatabaseHelper
import com.example.mineprompt.data.OpenAIService
import com.example.mineprompt.data.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class CreateViewModel(application: Application) : AndroidViewModel(application) {

    private val databaseHelper = DatabaseHelper(application)
    private val userPreferences = UserPreferences(application)
    private val openAIService = OpenAIService()

    private val _text = MutableLiveData<String>().apply {
        value = "생성 화면입니다"
    }
    val text: LiveData<String> = _text

    private val _isGenerating = MutableLiveData<Boolean>()
    val isGenerating: LiveData<Boolean> = _isGenerating

    private val _generationResult = MutableLiveData<Result<Long>>()
    val generationResult: LiveData<Result<Long>> = _generationResult

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    companion object {
        private const val TAG = "CreateViewModel"
    }

    fun generatePrompt(request: PromptGenerationRequest) {
        if (_isGenerating.value == true) return

        _isGenerating.value = true
        _error.value = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "프롬프트, 제목, 카테고리 생성 시작")

                // 프롬프트 생성, 제목 생성, 카테고리 분류 병렬 실행
                val promptJob = async { openAIService.generatePrompt(request) }
                val titleJob = async { openAIService.generateTitle(request) }
                val categoryJob = async { openAIService.classifyCategory(request) }

                val promptResult = promptJob.await()
                val titleResult = titleJob.await()
                val categoryResult = categoryJob.await()

                if (promptResult.isSuccess && titleResult.isSuccess && categoryResult.isSuccess) {
                    val generatedPrompt = promptResult.getOrNull()!!
                    val generatedTitle = titleResult.getOrNull()!!
                    val categories = categoryResult.getOrNull() ?: listOf("일상")

                    Log.d(TAG, "AI 프롬프트 생성 완료")
                    Log.d(TAG, "AI 제목 생성 완료: $generatedTitle")
                    Log.d(TAG, "AI 카테고리 분류 완료: $categories")

                    // 생성된 프롬프트를 DB에 저장 (AI 생성된 제목과 카테고리 포함)
                    val promptId = saveGeneratedPrompt(generatedPrompt, generatedTitle, request, categories)

                    if (promptId != -1L) {
                        Log.d(TAG, "프롬프트 저장 완료: ID=$promptId")
                        _generationResult.postValue(Result.success(promptId))
                    } else {
                        Log.e(TAG, "프롬프트 저장 실패")
                        _error.postValue("프롬프트 저장 중 오류가 발생했습니다.")
                        _generationResult.postValue(Result.failure(Exception("프롬프트 저장 실패")))
                    }
                } else {
                    // 생성 실패 시
                    val errorMsg = promptResult.exceptionOrNull()?.message
                        ?: titleResult.exceptionOrNull()?.message
                        ?: categoryResult.exceptionOrNull()?.message
                        ?: "프롬프트 생성 실패"

                    Log.e(TAG, "AI 프롬프트/제목/카테고리 생성 실패: $errorMsg")
                    _error.postValue(errorMsg)
                    _generationResult.postValue(
                        Result.failure(
                            promptResult.exceptionOrNull()
                                ?: titleResult.exceptionOrNull()
                                ?: categoryResult.exceptionOrNull()
                                ?: Exception("생성 실패")
                        )
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "프롬프트 생성 중 예외 발생", e)
                _error.postValue("프롬프트 생성 중 오류가 발생했습니다.")
                _generationResult.postValue(Result.failure(e))
            } finally {
                _isGenerating.postValue(false)
            }
        }
    }

    private fun saveGeneratedPrompt(
        generatedPrompt: String,
        generatedTitle: String,
        request: PromptGenerationRequest,
        aiCategories: List<String>
    ): Long {
        val db = databaseHelper.writableDatabase
        val currentUserId = userPreferences.getUserId()

        try {
            val values = ContentValues().apply {
                put("title", generatedTitle)
                put("content", generatedPrompt)
                put("description", generatedTitle)
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
                assignAiCategoriesToPrompt(promptId, aiCategories)
            }

            return promptId

        } catch (e: Exception) {
            Log.e(TAG, "프롬프트 저장 실패", e)
            return -1L
        }
    }

    private fun assignAiCategoriesToPrompt(promptId: Long, categoryNames: List<String>) {
        val db = databaseHelper.writableDatabase

        categoryNames.forEach { categoryName ->
            try {
                val categoryType = when (categoryName) {
                    "콘텐츠 제작" -> CategoryType.CONTENT_CREATION
                    "비즈니스" -> CategoryType.BUSINESS
                    "마케팅" -> CategoryType.MARKETING
                    "글쓰기/창작" -> CategoryType.WRITING
                    "개발" -> CategoryType.DEVELOPMENT
                    "학습" -> CategoryType.LEARNING
                    "생산성" -> CategoryType.PRODUCTIVITY
                    "자기계발" -> CategoryType.SELF_DEVELOPMENT
                    "언어" -> CategoryType.LANGUAGE
                    "재미" -> CategoryType.FUN
                    "일상" -> CategoryType.DAILY
                    "법률/재무" -> CategoryType.LEGAL
                    else -> CategoryType.DAILY
                }

                val values = ContentValues().apply {
                    put("prompt_id", promptId)
                    put("category_id", categoryType.id)
                }

                db.insertWithOnConflict("prompt_categories", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE)
                Log.d(TAG, "AI 카테고리 매핑 저장: promptId=$promptId, category=$categoryName (${categoryType.id})")

            } catch (e: Exception) {
                Log.e(TAG, "AI 카테고리 매핑 저장 실패: category=$categoryName", e)
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}