package com.example.mineprompt.ui.prompt

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.mineprompt.R
import com.example.mineprompt.databinding.ActivityPromptDetailBinding
import com.example.mineprompt.utils.ToastUtils
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

class PromptDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPromptDetailBinding
    private lateinit var promptDetailViewModel: PromptDetailViewModel
    private lateinit var categoryTagAdapter: CategoryTagAdapter

    private var promptId: Long = -1L

    companion object {
        private const val EXTRA_PROMPT_ID = "prompt_id"

        fun newIntent(context: Context, promptId: Long): Intent {
            return Intent(context, PromptDetailActivity::class.java).apply {
                putExtra(EXTRA_PROMPT_ID, promptId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPromptDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        promptId = intent.getLongExtra(EXTRA_PROMPT_ID, -1L)

        if (promptId == -1L) {
            ToastUtils.showGeneralError(this)
            finish()
            return
        }

        promptDetailViewModel = ViewModelProvider(this)[PromptDetailViewModel::class.java]

        setupRecyclerView()
        setupClickListeners()
        observeData()

        // 프롬프트 상세 정보 로드
        promptDetailViewModel.loadPromptDetail(promptId)
    }

    private fun setupRecyclerView() {
        categoryTagAdapter = CategoryTagAdapter()

        val flexboxLayoutManager = FlexboxLayoutManager(this).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.FLEX_START
        }

        binding.recyclerViewCategories.apply {
            layoutManager = flexboxLayoutManager
            adapter = categoryTagAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnViewPrompt.setOnClickListener {
            showPromptBottomSheet()
        }

        binding.btnCopyPrompt.setOnClickListener {
            copyPromptToClipboard()
        }

        binding.ivFavorite.setOnClickListener {
            promptDetailViewModel.toggleLike()
        }
    }

    private fun showPromptBottomSheet() {
        val promptDetail = promptDetailViewModel.promptDetail.value ?: return

        val bottomSheet = PromptBottomSheetFragment.newInstance(
            title = promptDetail.title,
            content = promptDetail.content
        )
        bottomSheet.show(supportFragmentManager, "PromptBottomSheet")
    }

    private fun observeData() {
        promptDetailViewModel.promptDetail.observe(this) { promptDetail ->
            promptDetail?.let {
                updateUI(it)
            }
        }

        promptDetailViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        promptDetailViewModel.error.observe(this) { error ->
            error?.let {
                ToastUtils.showShort(this, it)
            }
        }
    }

    private fun updateUI(promptDetail: PromptDetailItem) {
        binding.apply {
            tvHeaderTitle.text = promptDetail.title
            categoryTagAdapter.submitList(promptDetail.categories)
            tvPromptDescription.text = promptDetail.description
            tvCreatorName.text = promptDetail.creatorName
            tvCreatedDate.text = promptDetail.createdDate
            tvLikeCount.text = promptDetail.likeCount.toString()
            tvViewCount.text = promptDetail.viewCount.toString()
            updateLikeButton(promptDetail.isLiked)

            // 프롬프트 설명
            if (promptDetail.purpose.isNotEmpty()) {
                tvPromptPurpose.text = promptDetail.purpose
                tvPromptPurpose.visibility = View.VISIBLE
                tvPurposeLabel.visibility = View.VISIBLE
            } else {
                tvPromptPurpose.visibility = View.GONE
                tvPurposeLabel.visibility = View.GONE
            }

            // 키워드
            if (promptDetail.keywords.isNotEmpty()) {
                tvPromptKeywords.text = promptDetail.keywords
                tvPromptKeywords.visibility = View.VISIBLE
                tvKeywordsLabel.visibility = View.VISIBLE
            } else {
                tvPromptKeywords.visibility = View.GONE
                tvKeywordsLabel.visibility = View.GONE
            }

            // 프롬프트 미리보기
            val previewContent = if (promptDetail.content.length > 100) {
                promptDetail.content.take(100) + "..."
            } else {
                promptDetail.content
            }
            tvPromptPreview.text = previewContent
        }
    }

    private fun updateLikeButton(isLiked: Boolean) {
        if (isLiked) {
            binding.ivFavorite.setImageResource(R.drawable.ic_favorite_filled_20dp)
            binding.ivFavorite.setColorFilter(ContextCompat.getColor(this, R.color.red))
        } else {
            binding.ivFavorite.setImageResource(R.drawable.ic_favorite_20dp)
            binding.ivFavorite.setColorFilter(ContextCompat.getColor(this, R.color.gray_300))
        }
    }

    private fun copyPromptToClipboard() {
        val promptDetail = promptDetailViewModel.promptDetail.value ?: return

        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("프롬프트", promptDetail.content)
        clipboard.setPrimaryClip(clip)

        ToastUtils.showPromptCopied(this)
    }
}