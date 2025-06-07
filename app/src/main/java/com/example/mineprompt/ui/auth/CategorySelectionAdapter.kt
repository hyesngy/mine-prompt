package com.example.mineprompt.ui.auth

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mineprompt.R
import com.example.mineprompt.databinding.ItemCategorySelectionBinding
import com.example.mineprompt.ui.auth.CategoryItem

class CategorySelectionAdapter(
    private val categories: List<CategoryItem>,
    private val onCategorySelected: (Long, Boolean) -> Unit
) : RecyclerView.Adapter<CategorySelectionAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategorySelectionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    inner class CategoryViewHolder(
        private val binding: ItemCategorySelectionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: CategoryItem) {
            binding.apply {
                tvCategoryName.text = category.name
                ivCategoryIcon.setImageResource(category.iconRes)

                updateSelection(category.isSelected)

                root.setOnClickListener {
                    category.isSelected = !category.isSelected
                    updateSelection(category.isSelected)
                    onCategorySelected(category.id, category.isSelected)
                }
            }
        }

        private fun updateSelection(isSelected: Boolean) {
            binding.apply {
                if (isSelected) {
                    cardCategory.strokeColor = ContextCompat.getColor(root.context, R.color.primary_500)
                    cardCategory.strokeWidth = 4
                    root.alpha = 1.0f
                } else {
                    cardCategory.strokeColor = ContextCompat.getColor(root.context, R.color.gray_300)
                    cardCategory.strokeWidth = 2
                    root.alpha = 0.7f
                }
            }
        }
    }
}