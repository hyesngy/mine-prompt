package com.example.mineprompt.ui.category

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mineprompt.R
import com.example.mineprompt.data.CategoryType
import com.example.mineprompt.databinding.ActivityCategoryPromptListBinding
import com.example.mineprompt.databinding.DialogCategoryFilterBinding
import com.example.mineprompt.ui.common.adapter.PromptCardAdapter
import com.example.mineprompt.ui.search.CategoryFilterAdapter
import com.example.mineprompt.ui.search.CategoryFilterItem

class CategoryPromptListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryPromptListBinding
    private lateinit var categoryPromptViewModel: CategoryPromptViewModel
    private lateinit var promptCardAdapter: PromptCardAdapter

    companion object {
        private const val EXTRA_TITLE = "title"
        private const val EXTRA_FILTER_TYPE = "filter_type"
        private const val EXTRA_SORT_TYPE = "sort_type"
        private const val EXTRA_CATEGORY_ID = "category_id"
        private const val EXTRA_CATEGORY_NAME = "category_name"

        fun newIntent(
            context: Context,
            title: String,
            filterType: CategoryFilterType,
            sortType: CategorySortType,
            categoryId: Long? = null,
            categoryName: String? = null
        ): Intent {
            return Intent(context, CategoryPromptListActivity::class.java).apply {
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_FILTER_TYPE, filterType.name)
                putExtra(EXTRA_SORT_TYPE, sortType.name)
                categoryId?.let { putExtra(EXTRA_CATEGORY_ID, it) }
                categoryName?.let { putExtra(EXTRA_CATEGORY_NAME, it) }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCategoryPromptListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val title = intent.getStringExtra(EXTRA_TITLE) ?: "카테고리"
        val filterType = CategoryFilterType.valueOf(intent.getStringExtra(EXTRA_FILTER_TYPE) ?: CategoryFilterType.ALL.name)
        val sortType = CategorySortType.valueOf(intent.getStringExtra(EXTRA_SORT_TYPE) ?: CategorySortType.RECOMMENDED.name)
        val categoryId = if (intent.hasExtra(EXTRA_CATEGORY_ID)) intent.getLongExtra(EXTRA_CATEGORY_ID, -1) else null
        val categoryName = intent.getStringExtra(EXTRA_CATEGORY_NAME)

        categoryPromptViewModel = ViewModelProvider(this)[CategoryPromptViewModel::class.java]

        setupViews(title)
        setupRecyclerView()
        setupClickListeners()
        observeData()

        // 초기 데이터 로드
        categoryPromptViewModel.loadPrompts(filterType, sortType, categoryId, categoryName)
    }

    private fun setupViews(title: String) {
        binding.tvTitle.text = title
    }

    private fun setupRecyclerView() {
        promptCardAdapter = PromptCardAdapter(
            onPromptClick = { promptItem ->
                val intent = com.example.mineprompt.ui.prompt.PromptDetailActivity.newIntent(this, promptItem.id)
                startActivity(intent)
            },
            onFavoriteClick = { promptItem ->
                categoryPromptViewModel.togglePromptLike(promptItem.id)
            }
        )

        binding.recyclerViewPrompts.apply {
            layoutManager = LinearLayoutManager(this@CategoryPromptListActivity)
            adapter = promptCardAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        // 정렬 필터 클릭
        binding.tvSortFilter.setOnClickListener {
            showSortFilterDialog()
        }

        // 카테고리 필터 클릭
        binding.btnCategoryFilter.setOnClickListener {
            showCategoryFilterDialog()
        }

        // 전체 카테고리 필터 클리어
        binding.tvClearFilters.setOnClickListener {
            categoryPromptViewModel.clearAllFilters()
        }
    }

    private fun observeData() {
        // 프롬프트 목록
        categoryPromptViewModel.prompts.observe(this) { prompts ->
            promptCardAdapter.submitList(prompts)

            if (prompts.isEmpty()) {
                binding.recyclerViewPrompts.visibility = View.GONE
                binding.layoutEmptyPrompts.visibility = View.VISIBLE
            } else {
                binding.recyclerViewPrompts.visibility = View.VISIBLE
                binding.layoutEmptyPrompts.visibility = View.GONE
            }

            // 결과 개수 표시
            binding.tvResultCount.text = "총 ${prompts.size}개의 프롬프트"
        }

        // 선택된 카테고리
        categoryPromptViewModel.selectedCategories.observe(this) { categories ->
            val selectedCount = categories.count { it.isSelected }
            binding.btnCategoryFilter.text = if (selectedCount > 0) {
                "카테고리 ($selectedCount)"
            } else {
                "카테고리 전체"
            }

            // 필터 태그 업데이트
            updateFilterTags(categories)
        }

        // 현재 정렬 방식
        categoryPromptViewModel.currentSortFilter.observe(this) { sortFilter ->
            binding.tvSortFilter.text = sortFilter
        }

        // 로딩 상태
        categoryPromptViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun updateFilterTags(categories: List<CategoryFilterItem>) {
        binding.flexboxCategoryFilters.removeAllViews()

        categories.filter { it.isSelected }.forEach { category ->
            val tagView = createCategoryFilterTag(category) {
                categoryPromptViewModel.removeCategoryFilter(category.id)
            }
            binding.flexboxCategoryFilters.addView(tagView)
        }

        // 필터 클리어 버튼 표시/숨김
        val hasActiveFilters = categories.any { it.isSelected }
        binding.tvClearFilters.visibility = if (hasActiveFilters) View.VISIBLE else View.GONE
    }

    private fun createCategoryFilterTag(category: CategoryFilterItem, onRemove: () -> Unit): View {
        val tagView = layoutInflater.inflate(R.layout.item_category_tag, binding.flexboxCategoryFilters, false)

        val tvCategoryName = tagView.findViewById<android.widget.TextView>(R.id.tv_category_name)
        val ivClose = tagView.findViewById<android.widget.ImageView>(R.id.iv_close)

        tvCategoryName.text = category.name
        ivClose.setOnClickListener { onRemove() }

        return tagView
    }

    private fun showSortFilterDialog() {
        val sortOptions = arrayOf("추천순", "인기순", "최신순")
        val currentIndex = when (categoryPromptViewModel.currentSortFilter.value) {
            "인기순" -> 1
            "최신순" -> 2
            else -> 0
        }

        AlertDialog.Builder(this)
            .setTitle("정렬 방식")
            .setSingleChoiceItems(sortOptions, currentIndex) { dialog, which ->
                categoryPromptViewModel.setSortFilter(sortOptions[which])
                dialog.dismiss()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showCategoryFilterDialog() {
        val dialogBinding = DialogCategoryFilterBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        // 카테고리 어댑터 설정
        val categoryAdapter = CategoryFilterAdapter { category ->
            // 선택/해제는 어댑터에서 처리됨
        }

        dialogBinding.recyclerViewCategories.apply {
            layoutManager = GridLayoutManager(this@CategoryPromptListActivity, 2)
            adapter = categoryAdapter
        }

        // 현재 선택된 카테고리 상태 반영
        val currentCategories = categoryPromptViewModel.selectedCategories.value ?: getCategoryFilterItems()
        categoryAdapter.submitList(currentCategories)

        // 버튼 리스너
        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnApply.setOnClickListener {
            categoryPromptViewModel.applyCategoryFilters(categoryAdapter.currentList)
            dialog.dismiss()
        }

        dialogBinding.tvClearAll.setOnClickListener {
            val clearedCategories = categoryAdapter.currentList.map { it.copy(isSelected = false) }
            categoryAdapter.submitList(clearedCategories)
        }

        dialog.show()
    }

    private fun getCategoryFilterItems(): List<CategoryFilterItem> {
        return CategoryType.entries.map { categoryType ->
            CategoryFilterItem(
                id = categoryType.id,
                name = categoryType.displayName,
                iconRes = getCategoryIcon(categoryType),
                isSelected = false
            )
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
}