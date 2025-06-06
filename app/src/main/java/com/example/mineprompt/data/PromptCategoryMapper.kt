package com.example.mineprompt.data

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase

object PromptCategoryMapper {

    private const val TABLE_PROMPT_CATEGORIES = "prompt_categories"
    private const val COLUMN_PROMPT_ID = "prompt_id"
    private const val COLUMN_CATEGORY_ID = "category_id"

    fun setupPromptCategoryMappings(db: SQLiteDatabase) {
        val promptCategoryMappings = listOf(
            // 프롬프트 1: 비즈니스, 생산성
            mapOf(COLUMN_PROMPT_ID to 1, COLUMN_CATEGORY_ID to 2), // 비즈니스
            mapOf(COLUMN_PROMPT_ID to 1, COLUMN_CATEGORY_ID to 7), // 생산성

            // 프롬프트 2: 콘텐츠 제작, 마케팅
            mapOf(COLUMN_PROMPT_ID to 2, COLUMN_CATEGORY_ID to 1), // 콘텐츠 제작
            mapOf(COLUMN_PROMPT_ID to 2, COLUMN_CATEGORY_ID to 3), // 마케팅

            // 프롬프트 3: 개발, 생산성
            mapOf(COLUMN_PROMPT_ID to 3, COLUMN_CATEGORY_ID to 5), // 개발
            mapOf(COLUMN_PROMPT_ID to 3, COLUMN_CATEGORY_ID to 7), // 생산성

            // 프롬프트 4: 학습, 자기계발
            mapOf(COLUMN_PROMPT_ID to 4, COLUMN_CATEGORY_ID to 6), // 학습
            mapOf(COLUMN_PROMPT_ID to 4, COLUMN_CATEGORY_ID to 8), // 자기계발

            // 프롬프트 5: 글쓰기/창작
            mapOf(COLUMN_PROMPT_ID to 5, COLUMN_CATEGORY_ID to 4)  // 글쓰기/창작
        )

        promptCategoryMappings.forEach { mapping ->
            val values = ContentValues().apply {
                put(COLUMN_PROMPT_ID, mapping[COLUMN_PROMPT_ID] as Int)
                put(COLUMN_CATEGORY_ID, mapping[COLUMN_CATEGORY_ID] as Int)
            }
            db.insertWithOnConflict(TABLE_PROMPT_CATEGORIES, null, values, SQLiteDatabase.CONFLICT_IGNORE)
        }
    }
}