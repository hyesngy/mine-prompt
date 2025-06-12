package com.example.mineprompt.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mineprompt.R
import com.example.mineprompt.data.CategoryType
import com.example.mineprompt.databinding.ItemTrendingCurationBinding

class TrendingCurationAdapter(
    private val onItemClick: (TrendingCurationItem) -> Unit
) : ListAdapter<TrendingCurationItem, TrendingCurationAdapter.ViewHolder>(TrendingCurationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTrendingCurationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemTrendingCurationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TrendingCurationItem) {
            binding.apply {
                tvCurationTitle.text = item.title

                val categoryIcon = getCategoryIcon(item.categoryId)
                ivCategoryIcon.setImageResource(categoryIcon)

                item.imageRes?.let { resId ->
                    ivCurationImage.setImageResource(resId)
                }

                root.setOnClickListener {
                    onItemClick(item)
                }
            }
        }

        private fun getCategoryIcon(categoryId: Long?): Int {
            val categoryType = CategoryType.entries.find { it.id == categoryId }
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
                else -> R.drawable.ic_category_daily_32dp // 기본 아이콘
            }
        }
    }
}

class TrendingCurationDiffCallback : DiffUtil.ItemCallback<TrendingCurationItem>() {
    override fun areItemsTheSame(oldItem: TrendingCurationItem, newItem: TrendingCurationItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: TrendingCurationItem, newItem: TrendingCurationItem): Boolean {
        return oldItem == newItem
    }
}