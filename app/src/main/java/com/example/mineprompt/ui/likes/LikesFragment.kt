package com.example.mineprompt.ui.likes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mineprompt.R
import com.example.mineprompt.data.CategoryType
import com.example.mineprompt.databinding.FragmentLikesBinding
import com.example.mineprompt.databinding.DialogCategoryFilterBinding
import com.example.mineprompt.ui.common.adapter.PromptCardAdapter
import com.example.mineprompt.ui.search.CategoryFilterAdapter
import com.example.mineprompt.ui.search.CategoryFilterItem
import com.example.mineprompt.utils.ToastUtils

class LikesFragment : Fragment() {

    private var _binding: FragmentLikesBinding? = null
    private val binding get() = _binding!!

    private lateinit var likesViewModel: LikesViewModel
    private lateinit var promptCardAdapter: PromptCardAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        likesViewModel = ViewModelProvider(this)[LikesViewModel::class.java]

        _binding = FragmentLikesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()
        setupClickListeners()
        observeData()

        likesViewModel.loadLikedPrompts()

        return root
    }

    private fun setupRecyclerView() {
        promptCardAdapter = PromptCardAdapter(
            onPromptClick = { promptItem ->
                val intent = com.example.mineprompt.ui.prompt.PromptDetailActivity.newIntent(requireContext(), promptItem.id)
                startActivity(intent)
            },
            onFavoriteClick = { promptItem ->
                likesViewModel.togglePromptLike(promptItem.id)
            }
        )

        binding.recyclerViewLikedPrompts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = promptCardAdapter
        }
    }

    private fun setupClickListeners() {
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
            likesViewModel.clearAllFilters()
        }

        // 프롬프트 둘러보기 버튼
        binding.btnExplorePrompts.setOnClickListener {
            // 홈 탭으로 이동
            (activity as? androidx.appcompat.app.AppCompatActivity)?.let { activity ->
                val bottomNav = activity.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(com.example.mineprompt.R.id.nav_view)
                bottomNav?.selectedItemId = com.example.mineprompt.R.id.navigation_home
            }
        }
    }

    private fun observeData() {
        // 좋아요한 프롬프트 목록
        likesViewModel.likedPrompts.observe(viewLifecycleOwner) { prompts ->
            promptCardAdapter.submitList(prompts)

            if (prompts.isEmpty()) {
                binding.recyclerViewLikedPrompts.visibility = View.GONE
                binding.layoutEmptyLikes.visibility = View.VISIBLE

                // 필터가 적용된 상태에서 비어있는지 확인
                val hasActiveFilters = likesViewModel.hasActiveFilters()
                if (hasActiveFilters) {
                    binding.tvEmptyTitle.text = "선택한 조건에 맞는\n좋아요가 없습니다"
                    binding.tvEmptyDescription.text = "다른 조건으로 검색해보세요"
                } else {
                    binding.tvEmptyTitle.text = "아직 좋아요한\n프롬프트가 없습니다"
                    binding.tvEmptyDescription.text = "마음에 드는 프롬프트에 좋아요를 눌러보세요"
                }
            } else {
                binding.recyclerViewLikedPrompts.visibility = View.VISIBLE
                binding.layoutEmptyLikes.visibility = View.GONE
            }
            binding.tvResultCount.text = "총 ${prompts.size}개의 좋아요"
        }

        // 선택된 카테고리
        likesViewModel.selectedCategories.observe(viewLifecycleOwner) { categories ->
            val selectedCount = categories.count { it.isSelected }
            binding.btnCategoryFilter.text = if (selectedCount > 0) {
                "카테고리 ($selectedCount)"
            } else {
                "카테고리 전체"
            }
            updateFilterTags(categories)
        }

        likesViewModel.currentSortFilter.observe(viewLifecycleOwner) { sortFilter ->
            binding.tvSortFilter.text = sortFilter
        }

        likesViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        likesViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                ToastUtils.showShort(requireContext(), it)
                likesViewModel.clearError()
            }
        }
    }

    private fun updateFilterTags(categories: List<CategoryFilterItem>) {
        binding.flexboxCategoryFilters.removeAllViews()

        categories.filter { it.isSelected }.forEach { category ->
            val tagView = createCategoryFilterTag(category) {
                likesViewModel.removeCategoryFilter(category.id)
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
        val sortOptions = arrayOf("담은순", "최신순", "인기순")
        val currentIndex = when (likesViewModel.currentSortFilter.value) {
            "최신순" -> 1
            "인기순" -> 2
            else -> 0
        }

        AlertDialog.Builder(requireContext())
            .setTitle("정렬 방식")
            .setSingleChoiceItems(sortOptions, currentIndex) { dialog, which ->
                likesViewModel.setSortFilter(sortOptions[which])
                dialog.dismiss()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showCategoryFilterDialog() {
        val dialogBinding = DialogCategoryFilterBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        // 카테고리 어댑터 설정
        val categoryAdapter = CategoryFilterAdapter { category ->
        }

        dialogBinding.recyclerViewCategories.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = categoryAdapter
        }

        val currentCategories = likesViewModel.selectedCategories.value ?: getCategoryFilterItems()
        categoryAdapter.submitList(currentCategories)

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnApply.setOnClickListener {
            likesViewModel.applyCategoryFilters(categoryAdapter.currentList)
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

    override fun onResume() {
        super.onResume()
        likesViewModel.refreshLikedPrompts()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}