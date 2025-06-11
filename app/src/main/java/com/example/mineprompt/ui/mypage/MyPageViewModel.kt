package com.example.mineprompt.ui.mypage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mineprompt.data.PromptRepository
import com.example.mineprompt.data.UserPreferences
import kotlinx.coroutines.launch

data class UserInfo(
    val nickname: String,
    val email: String
)

class MyPageViewModel(
    private val promptRepository: PromptRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _userInfo = MutableLiveData<UserInfo>()
    val userInfo: LiveData<UserInfo> = _userInfo

    private val _logoutSuccess = MutableLiveData<Boolean>()
    val logoutSuccess: LiveData<Boolean> = _logoutSuccess

    fun loadUserInfo() {
        viewModelScope.launch {
            try {
                val nickname = userPreferences.getUserNickname()
                val email = userPreferences.getUserEmail()

                _userInfo.value = UserInfo(
                    nickname = nickname.ifEmpty { "사용자" },
                    email = email.ifEmpty { "게스트 계정" }
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _userInfo.value = UserInfo(
                    nickname = "사용자",
                    email = "게스트 계정"
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                userPreferences.clearUserData()
                _logoutSuccess.value = true
            } catch (e: Exception) {
                e.printStackTrace()
                _logoutSuccess.value = false
            }
        }
    }
}