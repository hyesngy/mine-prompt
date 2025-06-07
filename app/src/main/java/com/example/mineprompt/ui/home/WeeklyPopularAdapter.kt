package com.example.mineprompt.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mineprompt.R
import com.example.mineprompt.databinding.ItemWeeklyPopularPromptBinding

// 주간 인기 프롬프트 어댑터
class WeeklyPopularAdapter(
    private val onItemClick: (WeeklyPopularPromptItem) -> Unit
) : ListAdapter<WeeklyPopularPromptItem, WeeklyPopularAdapter.ViewHolder>(WeeklyPopularDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWeeklyPopularPromptBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemWeeklyPopularPromptBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: WeeklyPopularPromptItem) {
            binding.apply {
                tvRank.text = item.rank.toString()
                tvPromptTitle.text = item.title
                tvCreatorName.text = item.creatorName
                tvLikeCount.text = item.likeCount.toString()
                tvCategory.text = item.category

                when (item.rank) {
                    1 -> tvRank.background = ContextCompat.getDrawable(root.context, R.drawable.rank_background_gold)
                    2 -> tvRank.background = ContextCompat.getDrawable(root.context, R.drawable.rank_background_silver)
                    3 -> tvRank.background = ContextCompat.getDrawable(root.context, R.drawable.rank_background_bronze)
                    else -> tvRank.background = ContextCompat.getDrawable(root.context, R.drawable.rank_background)
                }

                root.setOnClickListener {
                    onItemClick(item)
                }
            }
        }
    }
}

class WeeklyPopularDiffCallback : DiffUtil.ItemCallback<WeeklyPopularPromptItem>() {
    override fun areItemsTheSame(oldItem: WeeklyPopularPromptItem, newItem: WeeklyPopularPromptItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: WeeklyPopularPromptItem, newItem: WeeklyPopularPromptItem): Boolean {
        return oldItem == newItem
    }
}