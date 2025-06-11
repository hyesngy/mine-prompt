package com.example.mineprompt.utils

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mineprompt.data.PromptRepository
import com.example.mineprompt.data.UserPreferences
import com.example.mineprompt.ui.mypage.MyPageViewModel

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    private val userPreferences by lazy { UserPreferences(context) }
    private val promptRepository by lazy { PromptRepository(context) }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MyPageViewModel::class.java) -> {
                MyPageViewModel(promptRepository, userPreferences) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}