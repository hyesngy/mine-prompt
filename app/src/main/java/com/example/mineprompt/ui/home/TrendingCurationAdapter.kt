package com.example.mineprompt.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mineprompt.databinding.ItemTrendingCurationBinding

// 트렌딩 큐레이션 어댑터
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

                item.imageRes?.let { resId ->
                    ivCurationImage.setImageResource(resId)
                }

                root.setOnClickListener {
                    onItemClick(item)
                }
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