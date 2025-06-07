package com.example.mineprompt.ui.common.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
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

                // 좋아요
                if (item.isLiked) {
                    ivFavorite.setImageResource(R.drawable.ic_favorite_filled_20dp)
                    ivFavorite.setColorFilter(ContextCompat.getColor(root.context, R.color.red))
                } else {
                    ivFavorite.setImageResource(R.drawable.ic_favorite_20dp)
                    ivFavorite.setColorFilter(ContextCompat.getColor(root.context, R.color.gray_300))
                }

                // TODO: 동적 카테고리 태그 레이아웃 구현

                root.setOnClickListener {
                    onPromptClick(item)
                }

                ivFavorite.setOnClickListener {
                    onFavoriteClick(item)
                }
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