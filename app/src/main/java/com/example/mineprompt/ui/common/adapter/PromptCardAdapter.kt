package com.example.mineprompt.ui.common.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mineprompt.R
import com.example.mineprompt.databinding.ItemRecommendedPromptBinding

// 프롬프트 카드 어댑터
class PromptCardAdapter(
    private val onPromptClick: (PromptCardItem) -> Unit,
    private val onFavoriteClick: (PromptCardItem) -> Unit
) : ListAdapter<PromptCardItem, PromptCardAdapter.ViewHolder>(PromptCardDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecommendedPromptBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemRecommendedPromptBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PromptCardItem) {
            binding.apply {
                tvPromptTitle.text = item.title
                tvPromptPreview.text = item.content
                tvCreatorName.text = item.creatorName
                tvCreatedDate.text = item.createdDate
                tvLikeCount.text = item.likeCount.toString()
                tvViewCount.text = item.viewCount.toString()

                // 좋아요 상태
                if (item.isLiked) {
                    ivFavorite.setImageResource(R.drawable.ic_favorite_filled_20dp)
                    ivFavorite.setColorFilter(ContextCompat.getColor(root.context, R.color.red))
                } else {
                    ivFavorite.setImageResource(R.drawable.ic_favorite_20dp)
                    ivFavorite.setColorFilter(ContextCompat.getColor(root.context, R.color.gray_300))
                }

                // 동적 카테고리 태그 표시
                setupCategoryTags(item.categories)

                root.setOnClickListener {
                    onPromptClick(item)
                }

                ivFavorite.setOnClickListener {
                    onFavoriteClick(item)
                }
            }
        }

        private fun setupCategoryTags(categories: List<String>) {
            binding.layoutCategoryTags.removeAllViews()

            // 최대 3개까지만 표시
            categories.take(3).forEach { categoryName ->
                val tagView = createCategoryTag(categoryName)
                binding.layoutCategoryTags.addView(tagView)
            }
        }

        private fun createCategoryTag(categoryName: String): TextView {
            val context = binding.root.context

            return TextView(context).apply {
                text = categoryName
                textSize = 10f
                setTextColor(ContextCompat.getColor(context, R.color.primary_500))
                background = ContextCompat.getDrawable(context, R.drawable.category_tag_background)
                typeface = android.graphics.Typeface.DEFAULT_BOLD

                // 패딩 설정 (paddingHorizontal="8dp", paddingVertical="4dp")
                val paddingHorizontal = (8 * context.resources.displayMetrics.density).toInt()
                val paddingVertical = (4 * context.resources.displayMetrics.density).toInt()
                setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical)

                // 마진 설정 (marginEnd="6dp")
                val layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = (6 * context.resources.displayMetrics.density).toInt()
                }
                this.layoutParams = layoutParams
            }
        }
    }
}

data class PromptCardItem(
    val id: Long,
    val title: String,
    val content: String,
    val creatorName: String,
    val createdDate: String,
    val likeCount: Int,
    val viewCount: Int,
    val categories: List<String>,
    val isLiked: Boolean = false
)

class PromptCardDiffCallback : DiffUtil.ItemCallback<PromptCardItem>() {
    override fun areItemsTheSame(oldItem: PromptCardItem, newItem: PromptCardItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: PromptCardItem, newItem: PromptCardItem): Boolean {
        return oldItem == newItem
    }
}