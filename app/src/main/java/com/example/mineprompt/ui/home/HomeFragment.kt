package com.example.mineprompt.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mineprompt.R
import com.example.mineprompt.databinding.FragmentHomeBinding
import com.example.mineprompt.ui.common.adapter.PromptCardAdapter
import com.example.mineprompt.ui.search.SearchActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var trendingAdapter: TrendingCurationAdapter
    private lateinit var weeklyPopularAdapter: WeeklyPopularAdapter
    private lateinit var recommendedAdapter: PromptCardAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerViews()
        setupClickListeners()
        observeData()

        return root
    }

    private fun setupRecyclerViews() {
        // 트렌딩 큐레이션 (가로 스크롤)
        trendingAdapter = TrendingCurationAdapter { curationItem ->
            // 큐레이션 클릭 처리
        }
        binding.recyclerViewTrending.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = trendingAdapter
        }

        // 주간 인기 프롬프트 (세로 스크롤)
        weeklyPopularAdapter = WeeklyPopularAdapter { promptItem ->
            // 프롬프트 클릭 처리
        }
        binding.recyclerViewWeeklyPopular.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = weeklyPopularAdapter
        }

        // 추천 프롬프트 (세로 스크롤)
        recommendedAdapter = PromptCardAdapter(
            onPromptClick = { promptItem ->
                // 프롬프트 클릭 처리
            },
            onFavoriteClick = { promptItem ->
                // 좋아요 클릭 처리
                homeViewModel.togglePromptLike(promptItem.id)
            }
        )
        binding.recyclerViewRecommended.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recommendedAdapter
        }
    }

    private fun setupClickListeners() {
        // 검색바 클릭 시 검색 화면으로 이동
        binding.root.findViewById<View>(R.id.layout_search_bar)?.setOnClickListener {
            val intent = SearchActivity.newIntent(requireContext())
            startActivity(intent)
        }
    }

    private fun observeData() {
        // 트렌딩 큐레이션 데이터 관찰
        homeViewModel.trendingCurations.observe(viewLifecycleOwner) { curations ->
            trendingAdapter.submitList(curations)
        }

        // 주간 인기 프롬프트 데이터 관찰
        homeViewModel.weeklyPopularPrompts.observe(viewLifecycleOwner) { prompts ->
            weeklyPopularAdapter.submitList(prompts)
        }

        // 추천 프롬프트 데이터 관찰
        homeViewModel.recommendedPrompts.observe(viewLifecycleOwner) { prompts ->
            recommendedAdapter.submitList(prompts)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class TrendingCurationItem(
    val id: Long,
    val title: String,
    val imageUrl: String? = null,
    val imageRes: Int? = null
)

data class WeeklyPopularPromptItem(
    val id: Long,
    val rank: Int,
    val title: String,
    val creatorName: String,
    val likeCount: Int,
    val category: String,
    val isLiked: Boolean = false
)
