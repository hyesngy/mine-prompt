package com.example.mineprompt.ui.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mineprompt.R
import com.example.mineprompt.databinding.ActivitySearchResultBinding
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

        // 액션바 제목 설정
        supportActionBar?.title = "검색 결과"

        // 상태바 설정
        window.statusBarColor = ContextCompat.getColor(this, R.color.gray_500)

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
                // 프롬프트 상세 화면으로 이동
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
            // 정렬 필터 다이얼로그 표시
            showSortFilterDialog()
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

        // 활성 필터들
        searchResultViewModel.activeFilters.observe(this) { filters ->
            updateFilterTags(filters)
        }

        // 정렬 필터
        searchResultViewModel.currentSortFilter.observe(this) { sortFilter ->
            binding.tvSortFilter.text = sortFilter
        }
    }

    private fun updateFilterTags(filters: List<SearchFilter>) {
        binding.flexboxFilterTags.removeAllViews()

        filters.forEach { filter ->
            val tagView = createFilterTag(filter) {
                searchResultViewModel.removeFilter(filter)
            }
            binding.flexboxFilterTags.addView(tagView)
        }
    }

    private fun createFilterTag(filter: SearchFilter, onRemove: () -> Unit): View {
        // 동적으로 필터 태그 생성
        // TODO: 실제 구현 필요
        return View(this) // 임시
    }

    private fun showSortFilterDialog() {
        // 정렬/필터 다이얼로그 표시
        val sortOptions = arrayOf("인기순", "최신순", "좋아요순", "조회순")

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("정렬 방식")
            .setItems(sortOptions) { _, which ->
                searchResultViewModel.setSortFilter(sortOptions[which])
            }
            .show()
    }
}