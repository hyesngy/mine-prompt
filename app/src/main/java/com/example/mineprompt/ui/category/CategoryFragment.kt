package com.example.mineprompt.ui.category

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mineprompt.R
import com.example.mineprompt.data.CategoryType
import com.example.mineprompt.databinding.FragmentCategoryBinding
import com.example.mineprompt.ui.search.SearchActivity
import com.google.android.material.button.MaterialButton

class CategoryFragment : Fragment() {

    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var categoryMainAdapter: CategoryMainAdapter

    private var selectedTab = TabType.ALL

    enum class TabType {
        ALL, POPULAR, LATEST, RECOMMENDED
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()
        setupClickListeners()
        return root
    }

    private fun setupRecyclerView() {
        categoryMainAdapter = CategoryMainAdapter { categoryItem ->
            navigateToCategoryDetail(categoryItem)
        }

        binding.recyclerViewCategories.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = categoryMainAdapter
        }

        // 카테고리 아이템 생성
        val categoryItems = createCategoryItems()
        categoryMainAdapter.submitList(categoryItems)
    }

    private fun setupClickListeners() {
        // 검색바 클릭
        binding.layoutSearchBar.setOnClickListener {
            val intent = SearchActivity.newIntent(requireContext())
            startActivity(intent)
        }

        // 전체 카드 클릭
        binding.cardAll.setOnClickListener {
            updateTabUI(TabType.ALL)
            navigateToPromptList(
                title = "전체",
                filterType = CategoryFilterType.ALL,
                sortType = CategorySortType.RECOMMENDED
            )
        }

        // 인기 카드 클릭
        binding.cardPopular.setOnClickListener {
            updateTabUI(TabType.POPULAR)
            navigateToPromptList(
                title = "인기",
                filterType = CategoryFilterType.ALL,
                sortType = CategorySortType.POPULAR
            )
        }

        // 최신 카드 클릭
        binding.cardLatest.setOnClickListener {
            updateTabUI(TabType.LATEST)
            navigateToPromptList(
                title = "최신",
                filterType = CategoryFilterType.ALL,
                sortType = CategorySortType.LATEST
            )
        }

        // 추천 카드 클릭 (사용자 관심 카테고리 기반)
        binding.cardRecommended.setOnClickListener {
            updateTabUI(TabType.RECOMMENDED)
            navigateToPromptList(
                title = "추천",
                filterType = CategoryFilterType.RECOMMENDED,
                sortType = CategorySortType.RECOMMENDED
            )
        }
    }

    private fun updateTabUI(tabType: TabType) {
        selectedTab = tabType

        // 모든 카드 초기화
        resetTabCard(binding.cardAll, binding.ivAll, binding.tvAll)
        resetTabCard(binding.cardPopular, binding.ivPopular, binding.tvPopular)
        resetTabCard(binding.cardLatest, binding.ivLatest, binding.tvLatest)
        resetTabCard(binding.cardRecommended, binding.ivRecommended, binding.tvRecommended)

        // 선택된 카드 활성화
        when (tabType) {
            TabType.ALL -> setTabCardSelected(binding.cardAll, binding.ivAll, binding.tvAll)
            TabType.POPULAR -> setTabCardSelected(binding.cardPopular, binding.ivPopular, binding.tvPopular)
            TabType.LATEST -> setTabCardSelected(binding.cardLatest, binding.ivLatest, binding.tvLatest)
            TabType.RECOMMENDED -> setTabCardSelected(binding.cardRecommended, binding.ivRecommended, binding.tvRecommended)
        }
    }

    private fun resetTabCard(card: androidx.cardview.widget.CardView, icon: android.widget.ImageView, text: android.widget.TextView) {
        card.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.gray_600))
        card.cardElevation = 2f
        icon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.gray_300))
        text.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_300))
    }

    private fun setTabCardSelected(card: androidx.cardview.widget.CardView, icon: android.widget.ImageView, text: android.widget.TextView) {
        card.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary_100))
        card.cardElevation = 8f
        icon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.primary_500))
        text.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_500))
    }

    private fun createCategoryItems(): List<CategoryMainItem> {
        return CategoryType.entries.map { categoryType ->
            CategoryMainItem(
                id = categoryType.id,
                name = categoryType.displayName,
                iconRes = getCategoryIcon(categoryType),
                isFavorite = false // TODO: 사용자 즐겨찾기 상태 확인
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

    private fun navigateToCategoryDetail(categoryItem: CategoryMainItem) {
        navigateToPromptList(
            title = categoryItem.name,
            filterType = CategoryFilterType.SINGLE_CATEGORY,
            sortType = CategorySortType.RECOMMENDED,
            categoryId = categoryItem.id,
            categoryName = categoryItem.name
        )
    }

    private fun navigateToPromptList(
        title: String,
        filterType: CategoryFilterType,
        sortType: CategorySortType,
        categoryId: Long? = null,
        categoryName: String? = null
    ) {
        val intent = CategoryPromptListActivity.newIntent(
            context = requireContext(),
            title = title,
            filterType = filterType,
            sortType = sortType,
            categoryId = categoryId,
            categoryName = categoryName
        )
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// 카테고리 메인 아이템
data class CategoryMainItem(
    val id: Long,
    val name: String,
    val iconRes: Int,
    var isFavorite: Boolean = false
)

// 필터 타입
enum class CategoryFilterType {
    ALL,
    RECOMMENDED,
    SINGLE_CATEGORY
}

// 정렬 타입
enum class CategorySortType {
    RECOMMENDED,
    POPULAR,
    LATEST
}