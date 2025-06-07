package com.example.mineprompt.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mineprompt.data.DatabaseHelper
import com.example.mineprompt.databinding.ActivitySignupBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = DatabaseHelper(this)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnNext.setOnClickListener {
            validateAndProceed()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun validateAndProceed() {
        val nickname = binding.etNickname.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        // 유효성 검사
        when {
            nickname.isEmpty() -> {
                Toast.makeText(this, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return
            }
            nickname.length < 2 -> {
                Toast.makeText(this, "닉네임은 2자 이상 입력해주세요.", Toast.LENGTH_SHORT).show()
                return
            }
            email.isEmpty() -> {
                Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                Toast.makeText(this, "올바른 이메일 형식을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return
            }
            password.isEmpty() -> {
                Toast.makeText(this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return
            }
            password.length < 6 -> {
                Toast.makeText(this, "비밀번호는 6자 이상 입력해주세요.", Toast.LENGTH_SHORT).show()
                return
            }
            password != confirmPassword -> {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                return
            }
        }

        binding.btnNext.isEnabled = false

        // 중복 검사 후 다음 단계로 이동
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val duplicateCheck = checkDuplicates(nickname, email)

                withContext(Dispatchers.Main) {
                    when (duplicateCheck) {
                        "nickname" -> {
                            Toast.makeText(this@SignupActivity, "이미 사용 중인 닉네임입니다.", Toast.LENGTH_SHORT).show()
                            binding.btnNext.isEnabled = true
                        }
                        "email" -> {
                            Toast.makeText(this@SignupActivity, "이미 사용 중인 이메일입니다.", Toast.LENGTH_SHORT).show()
                            binding.btnNext.isEnabled = true
                        }
                        "none" -> {
                            // 다음 단계로 이동
                            val intent = Intent(this@SignupActivity, CategorySelectionActivity::class.java)
                            intent.putExtra("nickname", nickname)
                            intent.putExtra("email", email)
                            intent.putExtra("password", password)
                            startActivity(intent)
                            binding.btnNext.isEnabled = true
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SignupActivity, "오류가 발생했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                    binding.btnNext.isEnabled = true
                }
            }
        }
    }

    private fun checkDuplicates(nickname: String, email: String): String {
        val db = databaseHelper.readableDatabase

        // 닉네임 중복 검사
        val nicknameCursor = db.query(
            "users",
            arrayOf("id"),
            "nickname = ?",
            arrayOf(nickname),
            null, null, null
        )

        if (nicknameCursor.count > 0) {
            nicknameCursor.close()
            return "nickname"
        }
        nicknameCursor.close()

        // 이메일 중복 검사
        val emailCursor = db.query(
            "users",
            arrayOf("id"),
            "email = ?",
            arrayOf(email),
            null, null, null
        )

        if (emailCursor.count > 0) {
            emailCursor.close()
            return "email"
        }
        emailCursor.close()

        return "none"
    }
}