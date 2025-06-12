package com.example.mineprompt.ui.auth

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.mineprompt.MainActivity
import com.example.mineprompt.R
import com.example.mineprompt.data.DatabaseHelper
import com.example.mineprompt.data.UserPreferences
import com.example.mineprompt.databinding.ActivityLoginBinding
import com.example.mineprompt.utils.ToastUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setupStatusBar()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = DatabaseHelper(this)
        userPreferences = UserPreferences(this)

        triggerDatabaseForInspector()

        // 이미 로그인된 사용자인지 확인
        if (userPreferences.isLoggedIn()) {
            navigateToMain()
            return
        }

        setupClickListeners()
        startEntranceAnimation()
    }

    private fun setupStatusBar() {
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = false
    }

    private fun startEntranceAnimation() {
        binding.layoutHeader.alpha = 0f
        binding.cardLoginForm.alpha = 0f
        binding.cardLoginForm.translationY = 100f

        Handler(Looper.getMainLooper()).postDelayed({
            ObjectAnimator.ofFloat(binding.layoutHeader, "alpha", 0f, 1f).apply {
                duration = 800
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }

            Handler(Looper.getMainLooper()).postDelayed({
                ObjectAnimator.ofFloat(binding.cardLoginForm, "alpha", 0f, 1f).apply {
                    duration = 600
                    start()
                }
                ObjectAnimator.ofFloat(binding.cardLoginForm, "translationY", 100f, 0f).apply {
                    duration = 600
                    interpolator = AccelerateDecelerateInterpolator()
                    start()
                }
            }, 300)
        }, 200)
    }

    private fun triggerDatabaseForInspector() {
        try {
            val db = databaseHelper.readableDatabase

            val cursor = db.rawQuery("SELECT COUNT(*) FROM users", null)
            if (cursor.moveToFirst()) {
                val userCount = cursor.getInt(0)
                Log.d("LoginActivity", "사용자 수: $userCount")
            }
            cursor.close()

            val tables = listOf("categories", "prompts", "user_likes")
            tables.forEach { tableName ->
                try {
                    val tableCursor = db.rawQuery("SELECT COUNT(*) FROM $tableName", null)
                    if (tableCursor.moveToFirst()) {
                        val count = tableCursor.getInt(0)
                        Log.d("LoginActivity", "$tableName 테이블 레코드 수: $count")
                    }
                    tableCursor.close()
                } catch (e: Exception) {
                    Log.w("LoginActivity", "$tableName 테이블 접근 실패: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("LoginActivity", "DB 트리거 실패", e)
        }
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            performLogin()
        }

        binding.btnSignup.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }

        binding.tvSkipLogin.setOnClickListener {
            // 게스트 로그인
            userPreferences.setGuestLogin()
            ToastUtils.showCustomToast(this, "게스트로 시작합니다 👋", true)
            navigateToMain()
        }
    }

    private fun performLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            ToastUtils.showLogininputRequired(this)
            return
        }

        setLoginLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = databaseHelper.readableDatabase
                val cursor = db.rawQuery(
                    "SELECT id, nickname, email FROM users WHERE email = ? AND password = ?",
                    arrayOf(email, password)
                )

                withContext(Dispatchers.Main) {
                    if (cursor.moveToFirst()) {
                        val userId = cursor.getInt(0)
                        val nickname = cursor.getString(1)
                        val userEmail = cursor.getString(2)

                        cursor.close()

                        // 사용자 정보 저장
                        userPreferences.setUserLoggedIn(userId, nickname, userEmail)

                        ToastUtils.showLoginSuccess(this@LoginActivity)

                        Handler(Looper.getMainLooper()).postDelayed({
                            navigateToMain()
                        }, 1000)

                    } else {
                        cursor.close()
                        setLoginLoading(false)
                        ToastUtils.showLoginFailed(this@LoginActivity)
                        shakeInputFields()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    setLoginLoading(false)
                    ToastUtils.showGeneralError(this@LoginActivity)
                    Log.e("LoginActivity", "로그인 처리 중 오류", e)
                }
            }
        }
    }

    private fun setLoginLoading(isLoading: Boolean) {
        binding.btnLogin.isEnabled = !isLoading
        binding.btnSignup.isEnabled = !isLoading
        binding.etEmail.isEnabled = !isLoading
        binding.etPassword.isEnabled = !isLoading

        if (isLoading) {
            binding.btnLogin.text = "로그인 중..."
        } else {
            binding.btnLogin.text = "로그인"
        }
    }

    private fun shakeInputFields() {
        val shakeAnimationEmail = ObjectAnimator.ofFloat(binding.tilEmail, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f)
        val shakeAnimationPassword = ObjectAnimator.ofFloat(binding.tilPassword, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f)

        shakeAnimationEmail.duration = 600
        shakeAnimationPassword.duration = 600

        shakeAnimationEmail.start()
        shakeAnimationPassword.start()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}