package com.example.mineprompt.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mineprompt.R
import com.example.mineprompt.databinding.ItemPopularSearchBinding

class PopularSearchAdapter(
    private val onItemClick: (PopularSearchItem) -> Unit
) : ListAdapter<PopularSearchItem, PopularSearchAdapter.ViewHolder>(PopularSearchDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPopularSearchBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemPopularSearchBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PopularSearchItem) {
            binding.apply {
                tvRank.text = item.rank.toString()
                tvSearchKeyword.text = item.keyword

                // 순위에 따른 텍스트 색상 변경
                when (item.rank) {
                    in 1..3 -> {
                        tvRank.setTextColor(ContextCompat.getColor(root.context, R.color.primary_500))
                    }
                    else -> {
                        tvRank.setTextColor(ContextCompat.getColor(root.context, R.color.gray_300))
                    }
                }

                // 트렌드에 따른 화살표 설정
                when (item.trend) {
                    TrendType.UP -> {
                        ivTrendArrow.setImageResource(R.drawable.ic_arrow_up_12dp)
                        ivTrendArrow.setColorFilter(ContextCompat.getColor(root.context, R.color.red))
                    }
                    TrendType.DOWN -> {
                        ivTrendArrow.setImageResource(R.drawable.ic_arrow_down_12dp)
                        ivTrendArrow.setColorFilter(ContextCompat.getColor(root.context, R.color.blue))
                    }
                    TrendType.SAME -> {
                        ivTrendArrow.setImageResource(R.drawable.ic_minus_12dp)
                        ivTrendArrow.setColorFilter(ContextCompat.getColor(root.context, R.color.gray_300))
                    }
                }

                root.setOnClickListener {
                    onItemClick(item)
                }
            }
        }
    }
}

class PopularSearchDiffCallback : DiffUtil.ItemCallback<PopularSearchItem>() {
    override fun areItemsTheSame(oldItem: PopularSearchItem, newItem: PopularSearchItem): Boolean {
        return oldItem.rank == newItem.rank
    }

    override fun areContentsTheSame(oldItem: PopularSearchItem, newItem: PopularSearchItem): Boolean {
        return oldItem == newItem
    }
}