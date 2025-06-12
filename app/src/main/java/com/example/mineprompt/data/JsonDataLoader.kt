// app/src/main/java/com/example/mineprompt/data/JsonDataLoader.kt

package com.example.mineprompt.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

object JsonDataLoader {
    private const val TAG = "JsonDataLoader"
    private const val TABLE_PROMPTS = "prompts"
    private const val TABLE_PROMPT_CATEGORIES = "prompt_categories"

    fun loadPromptsFromJson(context: Context, db: SQLiteDatabase) {
        Log.d(TAG, "JSON에서 프롬프트 데이터 로드 시작")

        try {
            val inputStream = context.assets.open("sample_prompts.json")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.readText()
            reader.close()

            Log.d(TAG, "JSON 파일 읽기 완료")

            val jsonArray = JSONArray(jsonString)
            var successCount = 0

            for (i in 0 until jsonArray.length()) {
                val jsonPrompt = jsonArray.getJSONObject(i)
                if (insertPromptWithCategoriesFromJson(db, jsonPrompt)) {
                    successCount++
                }
            }

            Log.d(TAG, "JSON에서 ${successCount}/${jsonArray.length()}개의 프롬프트 로드 완료")

        } catch (e: Exception) {
            Log.e(TAG, "JSON 파일 로드 실패", e)
        }
    }

    private fun insertPromptWithCategoriesFromJson(db: SQLiteDatabase, jsonPrompt: JSONObject): Boolean {
        return try {
            // 프롬프트 데이터 삽입
            val promptValues = ContentValues().apply {
                put("id", jsonPrompt.getLong("id"))
                put("title", jsonPrompt.getString("title"))
                put("content", jsonPrompt.getString("content"))
                put("description", jsonPrompt.getString("description"))
                put("purpose", jsonPrompt.getString("purpose"))
                put("keywords", jsonPrompt.getString("keywords"))
                put("length", jsonPrompt.getString("length"))
                put("style", jsonPrompt.getString("style"))
                put("language", jsonPrompt.getString("language"))
                put("creator_id", jsonPrompt.getInt("creator_id"))
                put("like_count", jsonPrompt.getInt("like_count"))
                put("view_count", jsonPrompt.getInt("view_count"))
                put("is_active", jsonPrompt.getInt("is_active"))

                // 시간 데이터 랜덤 생성
                val randomDaysAgo = (1..30).random()
                val createdTime = System.currentTimeMillis() - (randomDaysAgo * 24 * 60 * 60 * 1000L)
                put("created_at", createdTime)
                put("updated_at", createdTime)
            }

            val promptResult = db.insertWithOnConflict(TABLE_PROMPTS, null, promptValues, SQLiteDatabase.CONFLICT_IGNORE)

            if (promptResult != -1L) {
                // 카테고리 연결 처리
                val promptId = jsonPrompt.getLong("id")
                val categoriesArray = jsonPrompt.optJSONArray("categories")

                if (categoriesArray != null) {
                    for (j in 0 until categoriesArray.length()) {
                        val categoryId = categoriesArray.getInt(j)
                        insertPromptCategory(db, promptId, categoryId.toLong())
                    }
                    Log.d(TAG, "프롬프트 ID $promptId: ${categoriesArray.length()}개 카테고리 연결 완료")
                } else {
                    Log.w(TAG, "프롬프트 ID $promptId: 카테고리 정보 없음")
                }

                return true
            } else {
                Log.w(TAG, "프롬프트 삽입 실패 (이미 존재하거나 오류): ${jsonPrompt.optString("title", "Unknown")}")
                return false
            }

        } catch (e: Exception) {
            Log.e(TAG, "프롬프트 삽입 실패: ${jsonPrompt.optString("title", "Unknown")}", e)
            return false
        }
    }

    private fun insertPromptCategory(db: SQLiteDatabase, promptId: Long, categoryId: Long) {
        try {
            val values = ContentValues().apply {
                put("prompt_id", promptId)
                put("category_id", categoryId)
            }

            val result = db.insertWithOnConflict(TABLE_PROMPT_CATEGORIES, null, values, SQLiteDatabase.CONFLICT_IGNORE)

            if (result == -1L) {
                Log.w(TAG, "카테고리 연결 실패 또는 이미 존재: promptId=$promptId, categoryId=$categoryId")
            }

        } catch (e: Exception) {
            Log.e(TAG, "카테고리 연결 중 오류: promptId=$promptId, categoryId=$categoryId", e)
        }
    }

    // 카테고리별 프롬프트 수를 확인하는 유틸리티 메서드
    fun getPromptCountByCategory(db: SQLiteDatabase): Map<String, Int> {
        val categoryCounts = mutableMapOf<String, Int>()

        try {
            val cursor = db.rawQuery("""
                SELECT c.name, COUNT(DISTINCT p.id) as count 
                FROM categories c
                LEFT JOIN prompt_categories pc ON c.id = pc.category_id
                LEFT JOIN prompts p ON pc.prompt_id = p.id AND p.is_active = 1
                GROUP BY c.id, c.name
                ORDER BY count DESC
            """, null)

            while (cursor.moveToNext()) {
                val categoryName = cursor.getString(0)
                val count = cursor.getInt(1)
                categoryCounts[categoryName] = count
            }
            cursor.close()

        } catch (e: Exception) {
            Log.e(TAG, "카테고리별 프롬프트 수 조회 실패", e)
        }

        return categoryCounts
    }

    // 카테고리 ID별 매핑 확인
    fun getCategoryMappingStats(db: SQLiteDatabase): Map<Long, Int> {
        val categoryStats = mutableMapOf<Long, Int>()

        try {
            val cursor = db.rawQuery("""
                SELECT category_id, COUNT(*) as count 
                FROM prompt_categories 
                GROUP BY category_id 
                ORDER BY category_id
            """, null)

            while (cursor.moveToNext()) {
                val categoryId = cursor.getLong(0)
                val count = cursor.getInt(1)
                categoryStats[categoryId] = count
            }
            cursor.close()

        } catch (e: Exception) {
            Log.e(TAG, "카테고리 매핑 통계 조회 실패", e)
        }

        return categoryStats
    }
}