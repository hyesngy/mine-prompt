package com.example.mineprompt.ui.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mineprompt.R
import com.example.mineprompt.data.CategoryType
import com.example.mineprompt.databinding.ActivitySearchResultBinding
import com.example.mineprompt.databinding.DialogCategoryFilterBinding
import com.example.mineprompt.ui.common.adapter.PromptCardAdapter

class SearchResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchResultBinding
    private lateinit var searchResultViewModel: SearchResultViewModel
    private lateinit var searchResultAdapter: PromptCardAdapter

    companion object {
        private const val EXTRA_SEARCH_QUERY = "search_query"

        fun newIntent(context: Context, searchQuery: String): Intent {
            return Intent(context, SearchResultActivity::class.java).apply {
                putExtra(EXTRA_SEARCH_QUERY, searchQuery)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySearchResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        searchResultViewModel = ViewModelProvider(this)[SearchResultViewModel::class.java]

        val searchQuery = intent.getStringExtra(EXTRA_SEARCH_QUERY) ?: ""

        setupViews(searchQuery)
        setupRecyclerView()
        setupClickListeners()
        observeData()

        // 검색 실행
        searchResultViewModel.performSearch(searchQuery)
    }

    private fun setupViews(searchQuery: String) {
        binding.tvSearchQuery.text = "'$searchQuery'에 대한 검색 결과"
    }

    private fun setupRecyclerView() {
        searchResultAdapter = PromptCardAdapter(
            onPromptClick = { promptItem ->
                val intent = com.example.mineprompt.ui.prompt.PromptDetailActivity.newIntent(this@SearchResultActivity, promptItem.id)
                startActivity(intent)
            },
            onFavoriteClick = { promptItem ->
                searchResultViewModel.togglePromptLike(promptItem.id)
            }
        )

        binding.recyclerViewSearchResults.apply {
            layoutManager = LinearLayoutManager(this@SearchResultActivity)
            adapter = searchResultAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.tvSortFilter.setOnClickListener {
            showSortFilterDialog()
        }

        binding.btnCategoryFilter.setOnClickListener {
            showCategoryFilterDialog()
        }
    }

    private fun observeData() {
        // 검색 결과
        searchResultViewModel.searchResults.observe(this) { results ->
            searchResultAdapter.submitList(results)

            if (results.isEmpty()) {
                binding.recyclerViewSearchResults.visibility = View.GONE
                binding.layoutNoResults.visibility = View.VISIBLE
                binding.tvResultCount.text = "검색 결과가 없습니다"
            } else {
                binding.recyclerViewSearchResults.visibility = View.VISIBLE
                binding.layoutNoResults.visibility = View.GONE
                binding.tvResultCount.text = "총 ${results.size}개의 프롬프트"
            }
        }

        // 선택된 카테고리 개수만 표시
        searchResultViewModel.selectedCategories.observe(this) { categories ->
            val selectedCount = categories.count { it.isSelected }
            binding.btnCategoryFilter.text = if (selectedCount > 0) {
                "카테고리 ($selectedCount)"
            } else {
                "카테고리 선택"
            }
        }

        // 정렬 필터
        searchResultViewModel.currentSortFilter.observe(this) { sortFilter ->
            binding.tvSortFilter.text = sortFilter
        }
    }

    private fun showCategoryFilterDialog() {
        val dialogBinding = DialogCategoryFilterBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        // 카테고리 어댑터 설정
        val categoryAdapter = CategoryFilterAdapter { category ->
            // 선택/해제는 어댑터에서 처리
        }

        dialogBinding.recyclerViewCategories.apply {
            layoutManager = LinearLayoutManager(this@SearchResultActivity)
            adapter = categoryAdapter
        }

        // 카테고리 목록 생성
        val categories = CategoryType.entries.map { categoryType ->
            CategoryFilterItem(
                id = categoryType.id,
                name = categoryType.displayName,
                iconRes = getCategoryIcon(categoryType),
                isSelected = false // 일단 기본값 false
            )
        }
        categoryAdapter.submitList(categories)

        // 버튼 리스너
        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnApply.setOnClickListener {
            // 선택된 카테고리들 적용
            searchResultViewModel.applyCategoryFilters(categoryAdapter.currentList)
            dialog.dismiss()
        }

        dialogBinding.tvClearAll.setOnClickListener {
            // 모든 선택 해제
            val clearedCategories = categoryAdapter.currentList.map { it.copy(isSelected = false) }
            categoryAdapter.submitList(clearedCategories)
        }

        dialog.show()
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

    private fun showSortFilterDialog() {
        val sortOptions = arrayOf("인기순", "최신순", "좋아요순", "조회순")

        AlertDialog.Builder(this)
            .setTitle("정렬 방식")
            .setItems(sortOptions) { _, which ->
                searchResultViewModel.setSortFilter(sortOptions[which])
            }
            .show()
    }
}