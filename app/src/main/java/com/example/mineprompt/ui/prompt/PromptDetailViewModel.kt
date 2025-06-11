package com.example.mineprompt.ui.prompt

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mineprompt.data.PromptRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PromptDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val promptRepository = PromptRepository(application)

    private val _promptDetail = MutableLiveData<PromptDetailItem?>()
    val promptDetail: LiveData<PromptDetailItem?> = _promptDetail

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadPromptDetail(promptId: Long) {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val detail = promptRepository.getPromptDetail(promptId)

                viewModelScope.launch(Dispatchers.Main) {
                    if (detail != null) {
                        _promptDetail.value = detail
                        // 조회수 증가
                        promptRepository.increaseViewCount(promptId)
                    } else {
                        _error.value = "프롬프트를 찾을 수 없습니다."
                    }
                    _isLoading.value = false
                }

            } catch (e: Exception) {
                viewModelScope.launch(Dispatchers.Main) {
                    _error.value = "프롬프트를 불러오는 중 오류가 발생했습니다."
                    _isLoading.value = false
                }
            }
        }
    }

    fun toggleLike() {
        val currentDetail = _promptDetail.value ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newLikeState = promptRepository.togglePromptLike(currentDetail.id)

                viewModelScope.launch(Dispatchers.Main) {
                    val updatedDetail = currentDetail.copy(
                        isLiked = newLikeState,
                        likeCount = if (newLikeState) currentDetail.likeCount + 1 else currentDetail.likeCount - 1
                    )
                    _promptDetail.value = updatedDetail
                }

            } catch (e: Exception) {
                viewModelScope.launch(Dispatchers.Main) {
                    _error.value = "좋아요 처리 중 오류가 발생했습니다."
                }
            }
        }
    }
}

data class PromptDetailItem(
    val id: Long,
    val title: String,
    val content: String,
    val description: String,
    val purpose: String,
    val keywords: String,
    val creatorName: String,
    val createdDate: String,
    val likeCount: Int,
    val viewCount: Int,
    val categories: List<CategoryTagItem>,
    val isLiked: Boolean = false
)

data class CategoryTagItem(
    val id: Long,
    val name: String
)