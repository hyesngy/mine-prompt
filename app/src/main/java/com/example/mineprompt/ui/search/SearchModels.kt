package com.example.mineprompt.ui.search

// 인기 검색어 아이템
data class PopularSearchItem(
    val rank: Int,
    val keyword: String,
    val trend: TrendType
)

// 검색 필터 아이템
data class SearchFilter(
    val name: String,
    val type: FilterType,
    val isActive: Boolean = false
)

// 카테고리 필터 아이템
data class CategoryFilterItem(
    val id: Long,
    val name: String,
    val iconRes: Int,
    var isSelected: Boolean = false
)

// 트렌드 타입
enum class TrendType {
    UP,
    DOWN,
    SAME
}

// 필터 타입
enum class FilterType {
    SEARCH,     // 검색어 필터
    CATEGORY,   // 카테고리 필터
    SORT,       // 정렬 필터
    LENGTH,     // 길이 필터
    STYLE       // 스타일 필터
}