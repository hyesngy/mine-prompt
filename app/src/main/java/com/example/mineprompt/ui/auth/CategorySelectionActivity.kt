package com.example.mineprompt.ui.auth

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mineprompt.MainActivity
import com.example.mineprompt.R
import com.example.mineprompt.data.CategoryType
import com.example.mineprompt.data.DatabaseHelper
import com.example.mineprompt.data.UserPreferences
import com.example.mineprompt.databinding.ActivityCategorySelectionBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategorySelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategorySelectionBinding
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var userPreferences: UserPreferences
    private lateinit var categoryAdapter: CategorySelectionAdapter
    private val selectedCategories = mutableSetOf<Long>()

    private var nickname: String = ""
    private var email: String = ""
    private var password: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategorySelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = DatabaseHelper(this)
        userPreferences = UserPreferences(this)

        // 이전 단계에서 전달받은 데이터
        nickname = intent.getStringExtra("nickname") ?: ""
        email = intent.getStringExtra("email") ?: ""
        password = intent.getStringExtra("password") ?: ""

        if (nickname.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "잘못된 접근입니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupRecyclerView()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        val categories = CategoryType.entries.map { categoryType ->
            CategoryItem(
                id = categoryType.id,
                name = categoryType.displayName,
                iconRes = getCategoryIcon(categoryType),
                isSelected = false
            )
        }

        categoryAdapter = CategorySelectionAdapter(categories) { categoryId, isSelected ->
            if (isSelected) {
                selectedCategories.add(categoryId)
            } else {
                selectedCategories.remove(categoryId)
            }
            updateContinueButton()
        }

        binding.recyclerViewCategories.apply {
            layoutManager = GridLayoutManager(this@CategorySelectionActivity, 2)
            adapter = categoryAdapter
        }
    }

    private fun getCategoryIcon(categoryType: CategoryType): Int {
        return when (categoryType) {
            CategoryType.CONTENT_CREATION -> R.drawable.ic_category_content_creation_32dp
            CategoryType.BUSINESS -> R.drawable.ic_category_business_32dp
            CategoryType.MARKETING -> R.drawable.ic_category_marketing_32dp
            CategoryType.WRITING -> R.drawable.ic_category_writing_32dp
            CategoryType.DEVELOPMENT -> R.drawable.ic_category_development_32dp
            CategoryType.LEARNING -> R.drawable.ic_category_learning_32dp
            CategoryType.PRODUCTIVITY -> R.drawable.ic_category_productivity_32dp
            CategoryType.SELF_DEVELOPMENT -> R.drawable.ic_category_self_development_32dp
            CategoryType.LANGUAGE -> R.drawable.ic_category_language_32dp
            CategoryType.FUN -> R.drawable.ic_category_fun_32dp
            CategoryType.DAILY -> R.drawable.ic_category_daily_32dp
            CategoryType.LEGAL -> R.drawable.ic_category_legal_32dp
        }
    }

    private fun setupClickListeners() {
        binding.btnComplete.setOnClickListener {
            completeSignup()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnSkip.setOnClickListener {
            // 카테고리 선택 없이 완료
            completeSignup()
        }
    }

    private fun updateContinueButton() {
        binding.btnComplete.isEnabled = true
        binding.btnComplete.text = if (selectedCategories.isEmpty()) {
            "완료"
        } else {
            "완료 (${selectedCategories.size}개 선택됨)"
        }
    }

    private fun completeSignup() {
        binding.btnComplete.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = createUser()
                if (userId != -1L) {
                    // 선택된 카테고리들을 즐겨찾기에 추가
                    saveUserFavoriteCategories(userId)

                    withContext(Dispatchers.Main) {
                        // 로그인 상태 저장
                        userPreferences.saveUserLogin(userId, nickname, email)

                        Toast.makeText(this@CategorySelectionActivity, "회원가입이 완료되었습니다!", Toast.LENGTH_SHORT).show()

                        // 메인 화면으로 이동
                        val intent = Intent(this@CategorySelectionActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@CategorySelectionActivity, "회원가입 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                        binding.btnComplete.isEnabled = true
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CategorySelectionActivity, "회원가입 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    binding.btnComplete.isEnabled = true
                }
            }
        }
    }

    private fun createUser(): Long {
        val db = databaseHelper.writableDatabase
        val values = ContentValues().apply {
            put("nickname", nickname)
            put("email", email)
            put("password", password)
            put("created_at", System.currentTimeMillis())
            put("updated_at", System.currentTimeMillis())
        }

        return db.insert("users", null, values)
    }

    private fun saveUserFavoriteCategories(userId: Long) {
        if (selectedCategories.isEmpty()) return

        val db = databaseHelper.writableDatabase
        selectedCategories.forEach { categoryId ->
            val values = ContentValues().apply {
                put("user_id", userId)
                put("category_id", categoryId)
                put("created_at", System.currentTimeMillis())
            }
            db.insert("user_favorite_categories", null, values)
        }
    }
}

data class CategoryItem(
    val id: Long,
    val name: String,
    val iconRes: Int,
    var isSelected: Boolean
)