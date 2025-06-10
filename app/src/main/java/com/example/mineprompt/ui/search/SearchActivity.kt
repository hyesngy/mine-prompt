package com.example.mineprompt.ui.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mineprompt.R
import com.example.mineprompt.databinding.ActivitySearchBinding
import com.example.mineprompt.ui.common.adapter.PromptCardAdapter
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var searchViewModel: SearchViewModel
    private lateinit var popularSearchAdapter: PopularSearchAdapter
    private lateinit var searchResultAdapter: PromptCardAdapter

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, SearchActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 액션바 제목 설정
        supportActionBar?.title = "검색"

        // 상태바 설정
        window.statusBarColor = ContextCompat.getColor(this, R.color.gray_500)

        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        searchViewModel = ViewModelProvider(this)[SearchViewModel::class.java]

        setupViews()
        setupRecyclerViews()
        setupClickListeners()
        observeData()
        setupKeyboardListener()

        // 자동으로 키보드 포커스
        binding.etSearch.requestFocus()
        showKeyboard()
    }

    private fun setupKeyboardListener() {
        // 키보드 상태 변화 감지
        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            val heightDiff = binding.root.rootView.height - binding.root.height
            if (heightDiff > 200) {
                // 키보드가 열림
                onKeyboardShown()
            } else {
                // 키보드가 닫힘
                onKeyboardHidden()
            }
        }
    }

    private fun onKeyboardShown() {
        // 키보드가 열렸을 때 처리
        binding.layoutPopularSearches.visibility = View.VISIBLE
        binding.layoutRecentSearches.visibility = View.VISIBLE
    }

    private fun onKeyboardHidden() {
        // 키보드가 닫혔을 때 처리
        binding.layoutPopularSearches.visibility = View.VISIBLE
        binding.layoutRecentSearches.visibility = View.VISIBLE
    }

    private fun setupViews() {
        // 검색 입력 리스너
        binding.etSearch.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                performSearch()
                true
            } else {
                false
            }
        }
    }

    private fun setupRecyclerViews() {
        // 인기 검색어 어댑터
        popularSearchAdapter = PopularSearchAdapter { searchItem ->
            // 인기 검색어 클릭 시
            binding.etSearch.setText(searchItem.keyword)
            performSearch()
        }

        binding.recyclerViewPopularSearches.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = popularSearchAdapter
        }

        // 검색 결과 어댑터
        searchResultAdapter = PromptCardAdapter(
            onPromptClick = { promptItem ->
                // 프롬프트 상세 화면으로 이동
            },
            onFavoriteClick = { promptItem ->
                searchViewModel.togglePromptLike(promptItem.id)
            }
        )

        binding.recyclerViewSearchResults.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = searchResultAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnSearch.setOnClickListener {
            performSearch()
        }

        binding.tvClearAll.setOnClickListener {
            searchViewModel.clearAllRecentSearches()
        }

        binding.tvSortFilter.setOnClickListener {
            // 정렬 필터 다이얼로그 표시
            showSortFilterDialog()
        }
    }

    private fun observeData() {
        // 최근 검색어
        searchViewModel.recentSearches.observe(this) { searches ->
            updateRecentSearchTags(searches)
        }

        // 인기 검색어
        searchViewModel.popularSearches.observe(this) { searches ->
            popularSearchAdapter.submitList(searches)
        }

        // 검색 결과
        searchViewModel.searchResults.observe(this) { results ->
            searchResultAdapter.submitList(results)
        }

        // 검색 상태
        searchViewModel.isSearching.observe(this) { isSearching ->
            if (isSearching) {
                binding.layoutRecentSearches.visibility = View.GONE
                binding.layoutPopularSearches.visibility = View.GONE
                binding.layoutSearchResults.visibility = View.VISIBLE
            } else {
                binding.layoutRecentSearches.visibility = View.VISIBLE
                binding.layoutPopularSearches.visibility = View.VISIBLE
                binding.layoutSearchResults.visibility = View.GONE
            }
        }

        // 검색 쿼리
        searchViewModel.currentSearchQuery.observe(this) { query ->
            if (query.isNotEmpty()) {
                binding.tvSearchResultTitle.text = "'$query'에 대한 검색 결과"
            }
        }

        // 활성 필터들
        searchViewModel.activeFilters.observe(this) { filters ->
            updateFilterTags(filters)
        }
    }

    private fun performSearch() {
        val query = binding.etSearch.text.toString().trim()
        if (query.isNotEmpty()) {
            searchViewModel.performSearch(query)
            hideKeyboard()
        }
    }

    private fun updateRecentSearchTags(searches: List<String>) {
        binding.flexboxRecentSearches.removeAllViews()

        searches.forEach { search ->
            val tagView = createSearchTag(search, isRemovable = true) {
                searchViewModel.removeRecentSearch(search)
            }
            binding.flexboxRecentSearches.addView(tagView)
        }
    }

    private fun updateFilterTags(filters: List<SearchFilter>) {
        binding.flexboxFilterTags.removeAllViews()

        filters.forEach { filter ->
            val tagView = createFilterTag(filter) {
                searchViewModel.removeFilter(filter)
            }
            binding.flexboxFilterTags.addView(tagView)
        }
    }

    private fun createSearchTag(text: String, isRemovable: Boolean = false, onRemove: (() -> Unit)? = null): View {
        // 동적으로 검색 태그 생성
        // TODO: 실제 구현 필요
        return View(this) // 임시
    }

    private fun createFilterTag(filter: SearchFilter, onRemove: () -> Unit): View {
        // 동적으로 필터 태그 생성
        // TODO: 실제 구현 필요
        return View(this) // 임시
    }

    private fun showSortFilterDialog() {
        // 정렬/필터 다이얼로그 표시
        // TODO: 실제 구현 필요
    }

    private fun showKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.etSearch, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
    }
}