package com.example.mineprompt.utils

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.mineprompt.R

object ToastUtils {

    private fun showCustomToast(context: Context, message: String, showIcon: Boolean = false, iconRes: Int? = null) {
        val inflater = LayoutInflater.from(context)
        val layout: View = inflater.inflate(R.layout.layout_custom_toast, null)

        val toastText = layout.findViewById<TextView>(R.id.toast_text)
        val toastIcon = layout.findViewById<ImageView>(R.id.toast_icon)

        toastText.text = message

        if (showIcon && iconRes != null) {
            toastIcon.visibility = View.VISIBLE
            toastIcon.setImageResource(iconRes)
        } else {
            toastIcon.visibility = View.GONE
        }

        val toast = Toast(context)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 150)
        toast.show()
    }

    private fun showCustomToastLong(context: Context, message: String, showIcon: Boolean = false, iconRes: Int? = null) {
        val inflater = LayoutInflater.from(context)
        val layout: View = inflater.inflate(R.layout.layout_custom_toast, null)

        val toastText = layout.findViewById<TextView>(R.id.toast_text)
        val toastIcon = layout.findViewById<ImageView>(R.id.toast_icon)

        toastText.text = message

        if (showIcon && iconRes != null) {
            toastIcon.visibility = View.VISIBLE
            toastIcon.setImageResource(iconRes)
        } else {
            toastIcon.visibility = View.GONE
        }

        val toast = Toast(context)
        toast.duration = Toast.LENGTH_LONG
        toast.view = layout
        toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 150)
        toast.show()
    }

    fun showShort(context: Context, message: String) {
        showCustomToast(context, message)
    }

    fun showLong(context: Context, message: String) {
        showCustomToastLong(context, message)
    }

    fun showLoginRequired(context: Context) {
        showShort(context, "로그인이 필요합니다.")
    }

    fun showLogininputRequired(context: Context) {
        showShort(context, "이메일과 비밀번호를 입력해주세요.")
    }

    fun showLoginSuccess(context: Context) {
        showCustomToast(context, "로그인 성공! ✨", true, R.drawable.ic_check_circle_16dp)
    }

    fun showLoginFailed(context: Context) {
        showShort(context, "이메일 또는 비밀번호가 잘못되었습니다.")
    }

    fun showSignupSuccess(context: Context) {
        showCustomToast(context, "회원가입이 완료되었습니다! 🎉", true, R.drawable.ic_check_circle_16dp)
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
        showCustomToast(context, "최근 검색어가 삭제되었습니다", true, R.drawable.ic_check_circle_16dp)
    }


    fun showPromptCopied(context: Context) {
        showCustomToast(context, "프롬프트가 복사되었습니다", true, R.drawable.ic_check_circle_16dp)
    }

    fun showPromptSaved(context: Context) {
        showCustomToast(context, "프롬프트가 저장되었습니다", true, R.drawable.ic_check_circle_16dp)
    }

    fun showPromptCreated(context: Context) {
        showCustomToast(context, "프롬프트가 생성되었습니다", true, R.drawable.ic_check_circle_16dp)
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