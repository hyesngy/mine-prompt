package com.example.mineprompt.ui.prompt

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mineprompt.databinding.ItemCategoryTagBinding

class CategoryTagAdapter : ListAdapter<CategoryTagItem, CategoryTagAdapter.ViewHolder>(CategoryTagDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryTagBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemCategoryTagBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CategoryTagItem) {
            binding.apply {
                tvCategoryName.text = item.name
                ivClose.visibility = android.view.View.GONE
            }
        }
    }
}

class CategoryTagDiffCallback : DiffUtil.ItemCallback<CategoryTagItem>() {
    override fun areItemsTheSame(oldItem: CategoryTagItem, newItem: CategoryTagItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: CategoryTagItem, newItem: CategoryTagItem): Boolean {
        return oldItem == newItem
    }
}