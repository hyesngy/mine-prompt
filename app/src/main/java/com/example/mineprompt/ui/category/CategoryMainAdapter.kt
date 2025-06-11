package com.example.mineprompt.ui.category

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mineprompt.R
import com.example.mineprompt.databinding.ItemCategoryMainBinding

class CategoryMainAdapter(
    private val onCategoryClick: (CategoryMainItem) -> Unit
) : ListAdapter<CategoryMainItem, CategoryMainAdapter.ViewHolder>(CategoryMainDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryMainBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemCategoryMainBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CategoryMainItem) {
            binding.apply {
                tvCategoryName.text = item.name
                ivCategoryIcon.setImageResource(item.iconRes)

                // 즐겨찾기 상태 표시 (별표)
                if (item.isFavorite) {
                    ivFavoriteIcon.setImageResource(R.drawable.ic_star_filled_20dp)
                    ivFavoriteIcon.setColorFilter(ContextCompat.getColor(root.context, R.color.yellow))
                } else {
                    ivFavoriteIcon.setImageResource(R.drawable.ic_star_20dp)
                    ivFavoriteIcon.setColorFilter(ContextCompat.getColor(root.context, R.color.gray_400))
                }

                root.setOnClickListener {
                    onCategoryClick(item)
                }

                // 즐겨찾기 토글
                ivFavoriteIcon.setOnClickListener {
                    item.isFavorite = !item.isFavorite
                    bind(item) // UI 업데이트
                    // TODO: 즐겨찾기 상태를 데이터베이스에 저장
                }
            }
        }
    }
}

class CategoryMainDiffCallback : DiffUtil.ItemCallback<CategoryMainItem>() {
    override fun areItemsTheSame(oldItem: CategoryMainItem, newItem: CategoryMainItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: CategoryMainItem, newItem: CategoryMainItem): Boolean {
        return oldItem == newItem
    }
}