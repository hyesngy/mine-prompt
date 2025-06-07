package com.example.mineprompt.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mineprompt.MainActivity
import com.example.mineprompt.data.DatabaseHelper
import com.example.mineprompt.data.UserPreferences
import com.example.mineprompt.databinding.ActivityLoginBinding
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
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = DatabaseHelper(this)
        userPreferences = UserPreferences(this)

        // 이미 로그인된 사용자인지 확인
        if (userPreferences.isLoggedIn()) {
            navigateToMain()
            return
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            performLogin()
        }

        binding.btnSignup.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        binding.tvSkipLogin.setOnClickListener {
            // 게스트로 로그인 (임시 사용자)
            userPreferences.setGuestLogin()
            navigateToMain()
        }
    }

    private fun performLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnLogin.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val user = authenticateUser(email, password)

                withContext(Dispatchers.Main) {
                    if (user != null) {
                        userPreferences.saveUserLogin(user.id, user.nickname, user.email)
                        Toast.makeText(this@LoginActivity, "로그인 성공!", Toast.LENGTH_SHORT).show()
                        navigateToMain()
                    } else {
                        Toast.makeText(this@LoginActivity, "이메일 또는 비밀번호가 잘못되었습니다.", Toast.LENGTH_SHORT).show()
                        binding.btnLogin.isEnabled = true
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "로그인 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    binding.btnLogin.isEnabled = true
                }
            }
        }
    }

    private fun authenticateUser(email: String, password: String): com.example.mineprompt.data.User? {
        val db = databaseHelper.readableDatabase
        val cursor = db.query(
            "users",
            null,
            "email = ? AND password = ?",
            arrayOf(email, password),
            null, null, null
        )

        return if (cursor.moveToFirst()) {
            val user = com.example.mineprompt.data.User(
                id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                nickname = cursor.getString(cursor.getColumnIndexOrThrow("nickname")),
                email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
                password = cursor.getString(cursor.getColumnIndexOrThrow("password")),
                createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at")),
                updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow("updated_at"))
            )
            cursor.close()
            user
        } else {
            cursor.close()
            null
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}