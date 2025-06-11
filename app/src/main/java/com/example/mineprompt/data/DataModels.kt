package com.example.mineprompt.data

data class User(
    val id: Long = 0,
    val nickname: String,
    val email: String,
    val password: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class Category(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val isActive: Boolean = true
)

data class Prompt(
    val id: Long = 0,
    val title: String,
    val content: String,
    val description: String? = null,
    val purpose: String? = null,
    val keywords: String? = null,
    val length: PromptLength = PromptLength.MEDIUM,
    val style: PromptStyle? = null,
    val language: String = "한국어",
    val creatorId: Long,
    val likeCount: Long = 0,
    val viewCount: Long = 0,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class PromptCategory(
    val id: Long = 0,
    val promptId: Long,
    val categoryId: Long
)

data class UserLike(
    val id: Long = 0,
    val userId: Long,
    val promptId: Long,
    val createdAt: Long = System.currentTimeMillis()
)

data class UserFavoriteCategory(
    val id: Long = 0,
    val userId: Long,
    val categoryId: Long,
    val createdAt: Long = System.currentTimeMillis()
)

data class SearchHistory(
    val id: Long = 0,
    val userId: Long,
    val keyword: String,
    val searchedAt: Long = System.currentTimeMillis()
)

data class PromptWithCategories(
    val prompt: Prompt,
    val categories: List<Category>,
    val creator: User,
    val isLiked: Boolean = false
)

data class CategoryWithCount(
    val category: Category,
    val promptCount: Long,
    val isFavorite: Boolean = false
)


enum class PromptLength(val displayName: String, val description: String) {
    SHORT("짧게", "1~2문장"),
    MEDIUM("중간", "단락"),
    LONG("길게", "글 형식")
}

enum class PromptStyle(val displayName: String) {
    CREATIVE("창의적"),
    FORMAL("공식적"),
    LOGICAL("논리적"),
    EMOTIONAL("감성적"),
    PROFESSIONAL("전문적")
}

enum class CategoryType(val id: Long, val code: String, val displayName: String) {
    CONTENT_CREATION(1, "content_creation", "콘텐츠 제작"),
    BUSINESS(2, "business", "비즈니스"),
    MARKETING(3, "marketing", "마케팅"),
    WRITING(4, "writing", "글쓰기/창작"),
    DEVELOPMENT(5, "development", "개발"),
    LEARNING(6, "learning", "학습"),
    PRODUCTIVITY(7, "productivity", "생산성"),
    SELF_DEVELOPMENT(8, "self_development", "자기계발"),
    LANGUAGE(9, "language", "언어"),
    FUN(10, "fun", "재미"),
    DAILY(11, "daily", "일상"),
    LEGAL(12, "legal", "법률/재무");

    companion object {
        fun fromId(id: Long): CategoryType? = values().find { it.id == id }
        fun fromCode(code: String): CategoryType? = values().find { it.code == code }
        fun fromDisplayName(displayName: String): CategoryType? = values().find { it.displayName == displayName }
    }
}