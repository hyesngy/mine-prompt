package com.example.mineprompt.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mineprompt.R
import com.example.mineprompt.databinding.ItemCategoryFilterBinding

class CategoryFilterAdapter(
    private val onCategorySelected: (CategoryFilterItem) -> Unit
) : ListAdapter<CategoryFilterItem, CategoryFilterAdapter.ViewHolder>(CategoryFilterDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryFilterBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemCategoryFilterBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CategoryFilterItem) {
            binding.apply {
                tvCategoryName.text = item.name
                ivCategoryIcon.setImageResource(item.iconRes)

                // 선택 상태에 따른 UI 업데이트
                updateSelectionState(item.isSelected)

                root.setOnClickListener {
                    item.isSelected = !item.isSelected
                    updateSelectionState(item.isSelected)
                    onCategorySelected(item)
                }
            }
        }

        private fun updateSelectionState(isSelected: Boolean) {
            binding.apply {
                if (isSelected) {
                    cardCategory.strokeColor = ContextCompat.getColor(root.context, R.color.primary_500)
                    cardCategory.strokeWidth = 2
                    ivCheck.visibility = View.VISIBLE
                    cardCategory.setCardBackgroundColor(ContextCompat.getColor(root.context, R.color.primary_500))
                    tvCategoryName.setTextColor(ContextCompat.getColor(root.context, R.color.white))
                } else {
                    cardCategory.strokeColor = ContextCompat.getColor(root.context, R.color.gray_400)
                    cardCategory.strokeWidth = 1
                    ivCheck.visibility = View.GONE
                    cardCategory.setCardBackgroundColor(ContextCompat.getColor(root.context, R.color.gray_700))
                    tvCategoryName.setTextColor(ContextCompat.getColor(root.context, R.color.white))
                    ivCategoryIcon.clearColorFilter()
                }
            }
        }
    }
}

class CategoryFilterDiffCallback : DiffUtil.ItemCallback<CategoryFilterItem>() {
    override fun areItemsTheSame(oldItem: CategoryFilterItem, newItem: CategoryFilterItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: CategoryFilterItem, newItem: CategoryFilterItem): Boolean {
        return oldItem == newItem
    }
}