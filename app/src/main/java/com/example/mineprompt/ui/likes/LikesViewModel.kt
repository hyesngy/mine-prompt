package com.example.mineprompt.ui.likes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LikesViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "좋아요 화면입니다"
    }
    val text: LiveData<String> = _text
}
