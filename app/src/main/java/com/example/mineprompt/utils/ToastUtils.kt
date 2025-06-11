package com.example.mineprompt.utils

import android.content.Context
import android.widget.Toast

object ToastUtils {

    fun showShort(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun showLong(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    fun showLoginRequired(context: Context) {
        showShort(context, "로그인이 필요합니다.")
    }

    fun showLoginSuccess(context: Context) {
        showShort(context, "로그인 성공!")
    }

    fun showLoginFailed(context: Context) {
        showShort(context, "이메일 또는 비밀번호가 잘못되었습니다.")
    }

    fun showSignupSuccess(context: Context) {
        showShort(context, "회원가입이 완료되었습니다!")
    }


    fun showLikeAdded(context: Context) {
        showShort(context, "좋아요에 추가했습니다 ❤️")
    }

    fun showLikeRemoved(context: Context) {
        showShort(context, "좋아요에서 제거했습니다")
    }

    fun showLikeFailed(context: Context) {
        showShort(context, "좋아요 처리 중 오류가 발생했습니다.")
    }


    fun showSearchEmpty(context: Context) {
        showShort(context, "검색어를 입력해주세요.")
    }

    fun showSearchFailed(context: Context) {
        showShort(context, "검색 중 오류가 발생했습니다.")
    }

    fun showNoSearchResults(context: Context) {
        showShort(context, "검색 결과가 없습니다.")
    }

    fun showRecentSearchCleared(context: Context) {
        showShort(context, "최근 검색어가 삭제되었습니다.")
    }


    fun showPromptCopied(context: Context) {
        showShort(context, "프롬프트가 복사되었습니다.")
    }

    fun showPromptSaved(context: Context) {
        showShort(context, "프롬프트가 저장되었습니다.")
    }

    fun showPromptCreated(context: Context) {
        showShort(context, "프롬프트가 생성되었습니다 ✨")
    }


    fun showNetworkError(context: Context) {
        showShort(context, "네트워크 연결을 확인해주세요.")
    }

    fun showGeneralError(context: Context) {
        showShort(context, "오류가 발생했습니다. 다시 시도해주세요.")
    }

    fun showLoading(context: Context) {
        showShort(context, "로딩 중...")
    }

    fun showComingSoon(context: Context) {
        showShort(context, "준비 중입니다.")
    }


    fun showGuestLimitation(context: Context) {
        showShort(context, "게스트는 이용할 수 없는 기능입니다.")
    }

    fun showGuestLoginPrompt(context: Context) {
        showLong(context, "회원가입하고 더 많은 기능을 이용해보세요!")
    }
}