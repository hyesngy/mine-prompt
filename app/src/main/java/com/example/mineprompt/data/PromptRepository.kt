package com.example.mineprompt.data

import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.example.mineprompt.ui.common.adapter.PromptCardItem
import com.example.mineprompt.ui.home.TrendingCurationItem
import com.example.mineprompt.ui.home.WeeklyPopularPromptItem
import com.example.mineprompt.ui.prompt.CategoryTagItem
import com.example.mineprompt.ui.prompt.PromptDetailItem

class PromptRepository(private val context: Context) {

    private val databaseHelper = DatabaseHelper(context)
    private val userPreferences = UserPreferences(context)

    companion object {
        private const val TAG = "PromptRepository"
    }

    // 홈 화면용 데이터 가져오기
    fun getTrendingCurations(): List<TrendingCurationItem> {
        return listOf(
            TrendingCurationItem(1, "AI 프롬프트 작성법"),
            TrendingCurationItem(2, "창작 영감 모음집"),
            TrendingCurationItem(3, "비즈니스 필수 템플릿")
        )
    }

    fun getWeeklyPopularPrompts(): List<WeeklyPopularPromptItem> {
        val db = databaseHelper.readableDatabase
        val prompts = mutableListOf<WeeklyPopularPromptItem>()

        try {
            // 좋아요 순으로 상위 4개 가져오기
            val query = """
                SELECT p.id, p.title, p.like_count, u.nickname,
                       GROUP_CONCAT(c.name) as categories
                FROM prompts p
                LEFT JOIN users u ON p.creator_id = u.id  
                LEFT JOIN prompt_categories pc ON p.id = pc.prompt_id
                LEFT JOIN categories c ON pc.category_id = c.id
                WHERE p.is_active = 1
                GROUP BY p.id
                ORDER BY p.like_count DESC, p.created_at DESC
                LIMIT 4
            """

            val cursor = db.rawQuery(query, null)
            var rank = 1

            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                val likeCount = cursor.getInt(cursor.getColumnIndexOrThrow("like_count"))
                val creatorName = cursor.getString(cursor.getColumnIndexOrThrow("nickname")) ?: "익명"
                val categories = cursor.getString(cursor.getColumnIndexOrThrow("categories")) ?: ""

                // 첫 번째 카테고리만 표시
                val primaryCategory = categories.split(",").firstOrNull() ?: "일반"

                // 현재 사용자가 좋아요 했는지 확인
                val isLiked = isPromptLiked(id)

                prompts.add(
                    WeeklyPopularPromptItem(
                        id = id,
                        rank = rank++,
                        title = title,
                        creatorName = creatorName,
                        likeCount = likeCount,
                        category = primaryCategory,
                        isLiked = isLiked
                    )
                )
            }
            cursor.close()

        } catch (e: Exception) {
            Log.e(TAG, "인기 프롬프트 조회 실패", e)
        }

        return prompts
    }

    fun getRecommendedPrompts(): List<PromptCardItem> {
        val db = databaseHelper.readableDatabase
        val prompts = mutableListOf<PromptCardItem>()

        try {
            val currentUserId = userPreferences.getUserId()

            // 사용자가 좋아요한 카테고리 기반으로 추천 (게스트면 전체)
            val query = if (currentUserId > 0 && !userPreferences.isGuest()) {
                """
                SELECT DISTINCT p.id, p.title, p.content, p.like_count, p.view_count, 
                       p.created_at, u.nickname,
                       GROUP_CONCAT(c.name) as categories
                FROM prompts p
                LEFT JOIN users u ON p.creator_id = u.id
                LEFT JOIN prompt_categories pc ON p.id = pc.prompt_id
                LEFT JOIN categories c ON pc.category_id = c.id
                LEFT JOIN user_favorite_categories ufc ON c.id = ufc.category_id AND ufc.user_id = ?
                WHERE p.is_active = 1
                GROUP BY p.id
                ORDER BY (CASE WHEN ufc.category_id IS NOT NULL THEN 1 ELSE 0 END) DESC,
                         p.like_count DESC, p.created_at DESC
                LIMIT 10
                """
            } else {
                """
                SELECT p.id, p.title, p.content, p.like_count, p.view_count, 
                       p.created_at, u.nickname,
                       GROUP_CONCAT(c.name) as categories
                FROM prompts p
                LEFT JOIN users u ON p.creator_id = u.id
                LEFT JOIN prompt_categories pc ON p.id = pc.prompt_id  
                LEFT JOIN categories c ON pc.category_id = c.id
                WHERE p.is_active = 1
                GROUP BY p.id
                ORDER BY p.created_at DESC
                LIMIT 10
                """
            }

            val cursor = if (currentUserId > 0 && !userPreferences.isGuest()) {
                db.rawQuery(query, arrayOf(currentUserId.toString()))
            } else {
                db.rawQuery(query, null)
            }

            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                val content = cursor.getString(cursor.getColumnIndexOrThrow("content"))
                val likeCount = cursor.getInt(cursor.getColumnIndexOrThrow("like_count"))
                val viewCount = cursor.getInt(cursor.getColumnIndexOrThrow("view_count"))
                val createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at"))
                val creatorName = cursor.getString(cursor.getColumnIndexOrThrow("nickname")) ?: "익명"
                val categories = cursor.getString(cursor.getColumnIndexOrThrow("categories")) ?: ""

                // 날짜 포맷팅
                val createdDate = formatCreatedDate(createdAt)

                // 카테고리 리스트로 변환
                val categoryList = categories.split(",").filter { it.isNotEmpty() }

                // 좋아요 여부 확인
                val isLiked = isPromptLiked(id)

                prompts.add(
                    PromptCardItem(
                        id = id,
                        title = title,
                        content = content,
                        creatorName = creatorName,
                        createdDate = createdDate,
                        likeCount = likeCount,
                        viewCount = viewCount,
                        categories = categoryList,
                        isLiked = isLiked
                    )
                )
            }
            cursor.close()

        } catch (e: Exception) {
            Log.e(TAG, "추천 프롬프트 조회 실패", e)
        }

        return prompts
    }

    // 검색 기능
    fun searchPrompts(query: String, categories: List<String> = emptyList()): List<PromptCardItem> {
        val db = databaseHelper.readableDatabase
        val prompts = mutableListOf<PromptCardItem>()

        try {
            var sql = """
                SELECT DISTINCT p.id, p.title, p.content, p.like_count, p.view_count,
                       p.created_at, u.nickname,
                       GROUP_CONCAT(DISTINCT c.name) as categories
                FROM prompts p
                LEFT JOIN users u ON p.creator_id = u.id
                LEFT JOIN prompt_categories pc ON p.id = pc.prompt_id
                LEFT JOIN categories c ON pc.category_id = c.id
                WHERE p.is_active = 1
            """

            val args = mutableListOf<String>()

            // 검색어 조건 추가
            if (query.isNotEmpty()) {
                sql += " AND (p.title LIKE ? OR p.content LIKE ? OR p.keywords LIKE ?)"
                val searchTerm = "%$query%"
                args.addAll(listOf(searchTerm, searchTerm, searchTerm))
            }

            // 카테고리 필터 추가
            if (categories.isNotEmpty()) {
                val categoryPlaceholders = categories.joinToString(",") { "?" }
                sql += " AND c.name IN ($categoryPlaceholders)"
                args.addAll(categories)
            }

            sql += """
                GROUP BY p.id
                ORDER BY p.like_count DESC, p.created_at DESC
                LIMIT 50
            """

            val cursor = db.rawQuery(sql, args.toTypedArray())

            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                val content = cursor.getString(cursor.getColumnIndexOrThrow("content"))
                val likeCount = cursor.getInt(cursor.getColumnIndexOrThrow("like_count"))
                val viewCount = cursor.getInt(cursor.getColumnIndexOrThrow("view_count"))
                val createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at"))
                val creatorName = cursor.getString(cursor.getColumnIndexOrThrow("nickname")) ?: "익명"
                val categoriesStr = cursor.getString(cursor.getColumnIndexOrThrow("categories")) ?: ""

                val createdDate = formatCreatedDate(createdAt)
                val categoryList = categoriesStr.split(",").filter { it.isNotEmpty() }
                val isLiked = isPromptLiked(id)

                prompts.add(
                    PromptCardItem(
                        id = id,
                        title = title,
                        content = content,
                        creatorName = creatorName,
                        createdDate = createdDate,
                        likeCount = likeCount,
                        viewCount = viewCount,
                        categories = categoryList,
                        isLiked = isLiked
                    )
                )
            }
            cursor.close()

        } catch (e: Exception) {
            Log.e(TAG, "프롬프트 검색 실패: $query", e)
        }

        return prompts
    }

    // 좋아요 토글
    fun togglePromptLike(promptId: Long): Boolean {
        val db = databaseHelper.writableDatabase
        val currentUserId = userPreferences.getUserId()

        if (currentUserId <= 0 || userPreferences.isGuest()) {
            Log.w(TAG, "게스트는 좋아요 기능을 사용할 수 없습니다")
            return false
        }

        try {
            // 현재 좋아요 상태 확인
            val isCurrentlyLiked = isPromptLiked(promptId)

            if (isCurrentlyLiked) {
                // 좋아요 취소
                val deleteResult = db.delete(
                    "user_likes",
                    "user_id = ? AND prompt_id = ?",
                    arrayOf(currentUserId.toString(), promptId.toString())
                )

                if (deleteResult > 0) {
                    // 프롬프트의 좋아요 카운트 감소
                    db.execSQL(
                        "UPDATE prompts SET like_count = like_count - 1 WHERE id = ? AND like_count > 0",
                        arrayOf(promptId.toString())
                    )
                }

            } else {
                // 좋아요 추가
                val values = ContentValues().apply {
                    put("user_id", currentUserId)
                    put("prompt_id", promptId)
                    put("created_at", System.currentTimeMillis())
                }

                val insertResult = db.insert("user_likes", null, values)

                if (insertResult != -1L) {
                    // 프롬프트의 좋아요 카운트 증가
                    db.execSQL(
                        "UPDATE prompts SET like_count = like_count + 1 WHERE id = ?",
                        arrayOf(promptId.toString())
                    )
                }
            }

            return !isCurrentlyLiked

        } catch (e: Exception) {
            Log.e(TAG, "좋아요 토글 실패: promptId=$promptId", e)
            return false
        }
    }

    // 좋아요 상태 확인
    private fun isPromptLiked(promptId: Long): Boolean {
        val currentUserId = userPreferences.getUserId()

        if (currentUserId <= 0 || userPreferences.isGuest()) {
            return false
        }

        val db = databaseHelper.readableDatabase

        try {
            val cursor = db.query(
                "user_likes",
                arrayOf("id"),
                "user_id = ? AND prompt_id = ?",
                arrayOf(currentUserId.toString(), promptId.toString()),
                null, null, null
            )

            val isLiked = cursor.count > 0
            cursor.close()
            return isLiked

        } catch (e: Exception) {
            Log.e(TAG, "좋아요 상태 확인 실패: promptId=$promptId", e)
            return false
        }
    }

    // 날짜 포맷팅
    private fun formatCreatedDate(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60 * 1000 -> "방금 전"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}분 전"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}시간 전"
            diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)}일 전"
            else -> "${diff / (7 * 24 * 60 * 60 * 1000)}주 전"
        }
    }

    // 조회수 증가
    fun increaseViewCount(promptId: Long) {
        val db = databaseHelper.writableDatabase

        try {
            db.execSQL(
                "UPDATE prompts SET view_count = view_count + 1 WHERE id = ?",
                arrayOf(promptId.toString())
            )
        } catch (e: Exception) {
            Log.e(TAG, "조회수 증가 실패: promptId=$promptId", e)
        }
    }

    // 인기 검색어
    fun getPopularSearches(): List<String> {
        return listOf(
            "콘텐츠 제작", "개발", "글쓰기", "마케팅", "비즈니스",
            "학습", "생산성", "자기계발", "영어", "일본어"
        )
    }

    // 프롬프트 상세 정보 가져오기
    fun getPromptDetail(promptId: Long): PromptDetailItem? {
        val db = databaseHelper.readableDatabase

        try {
            val query = """
            SELECT p.id, p.title, p.content, p.description, p.purpose, p.keywords, 
                   p.like_count, p.view_count, p.created_at,
                   u.nickname,
                   GROUP_CONCAT(c.id || ':' || c.name) as categories
            FROM prompts p
            LEFT JOIN users u ON p.creator_id = u.id
            LEFT JOIN prompt_categories pc ON p.id = pc.prompt_id
            LEFT JOIN categories c ON pc.category_id = c.id
            WHERE p.id = ? AND p.is_active = 1
            GROUP BY p.id
        """

            val cursor = db.rawQuery(query, arrayOf(promptId.toString()))

            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                val content = cursor.getString(cursor.getColumnIndexOrThrow("content"))
                val description = cursor.getString(cursor.getColumnIndexOrThrow("description")) ?: title
                val purpose = cursor.getString(cursor.getColumnIndexOrThrow("purpose")) ?: ""
                val keywords = cursor.getString(cursor.getColumnIndexOrThrow("keywords")) ?: ""
                val likeCount = cursor.getInt(cursor.getColumnIndexOrThrow("like_count"))
                val viewCount = cursor.getInt(cursor.getColumnIndexOrThrow("view_count"))
                val createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at"))
                val creatorName = cursor.getString(cursor.getColumnIndexOrThrow("nickname")) ?: "익명"
                val categoriesString = cursor.getString(cursor.getColumnIndexOrThrow("categories")) ?: ""

                // 카테고리 파싱
                val categories = if (categoriesString.isNotEmpty()) {
                    categoriesString.split(",").mapNotNull { categoryData ->
                        val parts = categoryData.split(":")
                        if (parts.size == 2) {
                            CategoryTagItem(
                                id = parts[0].toLongOrNull() ?: 0,
                                name = parts[1]
                            )
                        } else null
                    }
                } else {
                    emptyList()
                }

                // 날짜 포맷팅
                val createdDate = formatCreatedDateDetail(createdAt)

                // 좋아요 여부 확인
                val isLiked = isPromptLiked(id)

                cursor.close()

                return PromptDetailItem(
                    id = id,
                    title = title,
                    content = content,
                    description = description,
                    purpose = purpose,
                    keywords = keywords,
                    creatorName = creatorName,
                    createdDate = createdDate,
                    likeCount = likeCount,
                    viewCount = viewCount,
                    categories = categories,
                    isLiked = isLiked
                )
            }

            cursor.close()

        } catch (e: Exception) {
            Log.e(TAG, "프롬프트 상세 조회 실패: promptId=$promptId", e)
        }

        return null
    }

    // 상세 화면용 날짜 포맷팅 (년월일 표시)
    private fun formatCreatedDateDetail(timestamp: Long): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timestamp

        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH) + 1
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

        return "${year}년 ${month}월 ${day}일"
    }

    fun getLikedPrompts(categories: List<String> = emptyList(), sortOrder: com.example.mineprompt.ui.likes.LikesViewModel.SortOrder): List<PromptCardItem> {
        val db = databaseHelper.readableDatabase
        val prompts = mutableListOf<PromptCardItem>()
        val currentUserId = userPreferences.getUserId()

        if (currentUserId <= 0 || userPreferences.isGuest()) {
            return emptyList()
        }

        try {
            var sql = """
            SELECT DISTINCT p.id, p.title, p.content, p.like_count, p.view_count,
                   p.created_at, u.nickname, ul.created_at as liked_at,
                   GROUP_CONCAT(DISTINCT c.name) as categories
            FROM prompts p
            INNER JOIN user_likes ul ON p.id = ul.prompt_id AND ul.user_id = ?
            LEFT JOIN users u ON p.creator_id = u.id
            LEFT JOIN prompt_categories pc ON p.id = pc.prompt_id
            LEFT JOIN categories c ON pc.category_id = c.id
            WHERE p.is_active = 1
        """

            val args = mutableListOf<String>()
            args.add(currentUserId.toString())

            // 카테고리 필터 추가
            if (categories.isNotEmpty()) {
                val categoryPlaceholders = categories.joinToString(",") { "?" }
                sql += " AND c.name IN ($categoryPlaceholders)"
                args.addAll(categories)
            }

            sql += " GROUP BY p.id"

            // 정렬 추가
            sql += when (sortOrder) {
                com.example.mineprompt.ui.likes.LikesViewModel.SortOrder.LIKED_DATE -> " ORDER BY ul.created_at DESC"
                com.example.mineprompt.ui.likes.LikesViewModel.SortOrder.LATEST -> " ORDER BY p.created_at DESC"
                com.example.mineprompt.ui.likes.LikesViewModel.SortOrder.POPULARITY -> " ORDER BY p.like_count DESC, p.created_at DESC"
            }

            sql += " LIMIT 100"

            val cursor = db.rawQuery(sql, args.toTypedArray())

            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                val content = cursor.getString(cursor.getColumnIndexOrThrow("content"))
                val likeCount = cursor.getInt(cursor.getColumnIndexOrThrow("like_count"))
                val viewCount = cursor.getInt(cursor.getColumnIndexOrThrow("view_count"))
                val createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at"))
                val creatorName = cursor.getString(cursor.getColumnIndexOrThrow("nickname")) ?: "익명"
                val categoriesStr = cursor.getString(cursor.getColumnIndexOrThrow("categories")) ?: ""

                val createdDate = formatCreatedDate(createdAt)
                val categoryList = categoriesStr.split(",").filter { it.isNotEmpty() }

                prompts.add(
                    PromptCardItem(
                        id = id,
                        title = title,
                        content = content,
                        creatorName = creatorName,
                        createdDate = createdDate,
                        likeCount = likeCount,
                        viewCount = viewCount,
                        categories = categoryList,
                        isLiked = true // 좋아요한 프롬프트이므로 항상 true
                    )
                )
            }
            cursor.close()

        } catch (e: Exception) {
            Log.e(TAG, "좋아요한 프롬프트 조회 실패", e)
        }

        return prompts
    }

    // 카테고리별 프롬프트 가져오기
    fun getPromptsByCategory(categories: List<String>, sortType: com.example.mineprompt.ui.category.CategorySortType): List<PromptCardItem> {
        val db = databaseHelper.readableDatabase
        val prompts = mutableListOf<PromptCardItem>()

        try {
            var sql = """
            SELECT DISTINCT p.id, p.title, p.content, p.like_count, p.view_count,
                   p.created_at, u.nickname,
                   GROUP_CONCAT(DISTINCT c.name) as categories
            FROM prompts p
            LEFT JOIN users u ON p.creator_id = u.id
            LEFT JOIN prompt_categories pc ON p.id = pc.prompt_id
            LEFT JOIN categories c ON pc.category_id = c.id
            WHERE p.is_active = 1
        """

            val args = mutableListOf<String>()

            // 카테고리 필터 추가 (비어있으면 전체)
            if (categories.isNotEmpty()) {
                val categoryPlaceholders = categories.joinToString(",") { "?" }
                sql += " AND c.name IN ($categoryPlaceholders)"
                args.addAll(categories)
            }

            sql += " GROUP BY p.id"

            // 정렬 추가
            sql += when (sortType) {
                com.example.mineprompt.ui.category.CategorySortType.RECOMMENDED -> " ORDER BY p.like_count DESC, p.view_count DESC, p.created_at DESC"
                com.example.mineprompt.ui.category.CategorySortType.POPULAR -> " ORDER BY p.like_count DESC, p.created_at DESC"
                com.example.mineprompt.ui.category.CategorySortType.LATEST -> " ORDER BY p.created_at DESC"
            }

            sql += " LIMIT 100"

            val cursor = db.rawQuery(sql, args.toTypedArray())

            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                val content = cursor.getString(cursor.getColumnIndexOrThrow("content"))
                val likeCount = cursor.getInt(cursor.getColumnIndexOrThrow("like_count"))
                val viewCount = cursor.getInt(cursor.getColumnIndexOrThrow("view_count"))
                val createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at"))
                val creatorName = cursor.getString(cursor.getColumnIndexOrThrow("nickname")) ?: "익명"
                val categoriesStr = cursor.getString(cursor.getColumnIndexOrThrow("categories")) ?: ""

                val createdDate = formatCreatedDate(createdAt)
                val categoryList = categoriesStr.split(",").filter { it.isNotEmpty() }
                val isLiked = isPromptLiked(id)

                prompts.add(
                    PromptCardItem(
                        id = id,
                        title = title,
                        content = content,
                        creatorName = creatorName,
                        createdDate = createdDate,
                        likeCount = likeCount,
                        viewCount = viewCount,
                        categories = categoryList,
                        isLiked = isLiked
                    )
                )
            }
            cursor.close()

        } catch (e: Exception) {
            Log.e(TAG, "카테고리별 프롬프트 조회 실패", e)
        }

        return prompts
    }

    // 사용자 관심 카테고리 기반 추천 프롬프트
    fun getRecommendedPromptsByUserInterest(sortType: com.example.mineprompt.ui.category.CategorySortType): List<PromptCardItem> {
        val db = databaseHelper.readableDatabase
        val prompts = mutableListOf<PromptCardItem>()
        val currentUserId = userPreferences.getUserId()

        try {
            val sql = if (currentUserId > 0 && !userPreferences.isGuest()) {
                // 로그인한 사용자: 즐겨찾기한 카테고리 기반 추천
                """
            SELECT DISTINCT p.id, p.title, p.content, p.like_count, p.view_count,
                   p.created_at, u.nickname,
                   GROUP_CONCAT(DISTINCT c.name) as categories,
                   (CASE WHEN ufc.category_id IS NOT NULL THEN 1 ELSE 0 END) as is_favorite_category
            FROM prompts p
            LEFT JOIN users u ON p.creator_id = u.id
            LEFT JOIN prompt_categories pc ON p.id = pc.prompt_id
            LEFT JOIN categories c ON pc.category_id = c.id
            LEFT JOIN user_favorite_categories ufc ON c.id = ufc.category_id AND ufc.user_id = ?
            WHERE p.is_active = 1
            GROUP BY p.id
            ORDER BY is_favorite_category DESC, 
                     ${when (sortType) {
                    com.example.mineprompt.ui.category.CategorySortType.RECOMMENDED -> "p.like_count DESC, p.view_count DESC, p.created_at DESC"
                    com.example.mineprompt.ui.category.CategorySortType.POPULAR -> "p.like_count DESC, p.created_at DESC"
                    com.example.mineprompt.ui.category.CategorySortType.LATEST -> "p.created_at DESC"
                }}
            LIMIT 50
            """
            } else {
                // 게스트: 전체 프롬프트에서 추천
                """
            SELECT p.id, p.title, p.content, p.like_count, p.view_count,
                   p.created_at, u.nickname,
                   GROUP_CONCAT(DISTINCT c.name) as categories
            FROM prompts p
            LEFT JOIN users u ON p.creator_id = u.id
            LEFT JOIN prompt_categories pc ON p.id = pc.prompt_id
            LEFT JOIN categories c ON pc.category_id = c.id
            WHERE p.is_active = 1
            GROUP BY p.id
            ORDER BY ${when (sortType) {
                    com.example.mineprompt.ui.category.CategorySortType.RECOMMENDED -> "p.like_count DESC, p.view_count DESC, p.created_at DESC"
                    com.example.mineprompt.ui.category.CategorySortType.POPULAR -> "p.like_count DESC, p.created_at DESC"
                    com.example.mineprompt.ui.category.CategorySortType.LATEST -> "p.created_at DESC"
                }}
            LIMIT 50
            """
            }

            val cursor = if (currentUserId > 0 && !userPreferences.isGuest()) {
                db.rawQuery(sql, arrayOf(currentUserId.toString()))
            } else {
                db.rawQuery(sql, null)
            }

            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                val content = cursor.getString(cursor.getColumnIndexOrThrow("content"))
                val likeCount = cursor.getInt(cursor.getColumnIndexOrThrow("like_count"))
                val viewCount = cursor.getInt(cursor.getColumnIndexOrThrow("view_count"))
                val createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at"))
                val creatorName = cursor.getString(cursor.getColumnIndexOrThrow("nickname")) ?: "익명"
                val categoriesStr = cursor.getString(cursor.getColumnIndexOrThrow("categories")) ?: ""

                val createdDate = formatCreatedDate(createdAt)
                val categoryList = categoriesStr.split(",").filter { it.isNotEmpty() }
                val isLiked = isPromptLiked(id)

                prompts.add(
                    PromptCardItem(
                        id = id,
                        title = title,
                        content = content,
                        creatorName = creatorName,
                        createdDate = createdDate,
                        likeCount = likeCount,
                        viewCount = viewCount,
                        categories = categoryList,
                        isLiked = isLiked
                    )
                )
            }
            cursor.close()

        } catch (e: Exception) {
            Log.e(TAG, "추천 프롬프트 조회 실패", e)
        }

        return prompts
    }
}

