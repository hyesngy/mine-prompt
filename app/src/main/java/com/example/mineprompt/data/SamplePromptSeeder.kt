package com.example.mineprompt.data

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase

object SamplePromptSeeder {

    private const val TABLE_PROMPTS = "prompts"
    private const val COLUMN_ID = "id"
    private const val COLUMN_PROMPT_TITLE = "title"
    private const val COLUMN_PROMPT_CONTENT = "content"
    private const val COLUMN_PROMPT_PURPOSE = "purpose"
    private const val COLUMN_PROMPT_DESCRIPTION = "description"
    private const val COLUMN_PROMPT_KEYWORDS = "keywords"
    private const val COLUMN_PROMPT_LENGTH = "length"
    private const val COLUMN_PROMPT_STYLE = "style"
    private const val COLUMN_PROMPT_LANGUAGE = "language"
    private const val COLUMN_PROMPT_CREATOR_ID = "creator_id"
    private const val COLUMN_PROMPT_LIKE_COUNT = "like_count"
    private const val COLUMN_PROMPT_VIEW_COUNT = "view_count"
    private const val COLUMN_IS_ACTIVE = "is_active"
    private const val COLUMN_CREATED_AT = "created_at"
    private const val COLUMN_UPDATED_AT = "updated_at"

    fun loadSamplePrompts(db: SQLiteDatabase) {
        val samplePrompts = listOf(
            mapOf(
                COLUMN_ID to 1,
                COLUMN_PROMPT_TITLE to "효과적인 회의 진행 가이드",
                COLUMN_PROMPT_CONTENT to "[회의 목적]에 맞는 효율적인 회의를 진행하기 위해 [참석자 수]명의 [직급/역할]을 고려하여 회의 안건과 진행 방식을 제안해주세요.",
                COLUMN_PROMPT_DESCRIPTION to "팀 회의를 효과적으로 진행하고 생산성을 높이기 위한 맞춤형 회의 가이드를 제공합니다. 참석자 특성과 회의 목적에 따라 최적화된 진행 방식을 제안합니다.",
                COLUMN_PROMPT_PURPOSE to "회의 효율성 향상",
                COLUMN_PROMPT_KEYWORDS to "회의, 리더십, 커뮤니케이션, 생산성",
                COLUMN_PROMPT_LENGTH to "MEDIUM",
                COLUMN_PROMPT_STYLE to "PROFESSIONAL",
                COLUMN_PROMPT_LANGUAGE to "한국어",
                COLUMN_PROMPT_CREATOR_ID to 1,
                COLUMN_PROMPT_LIKE_COUNT to 34,
                COLUMN_PROMPT_VIEW_COUNT to 156,
                COLUMN_IS_ACTIVE to 1,
                COLUMN_CREATED_AT to System.currentTimeMillis() - 86400000,
                COLUMN_UPDATED_AT to System.currentTimeMillis() - 86400000
            ),

            mapOf(
                COLUMN_ID to 2,
                COLUMN_PROMPT_TITLE to "매력적인 유튜브 썸네일 기획",
                COLUMN_PROMPT_CONTENT to "[동영상 주제]와 [타겟 연령층]을 고려하여 클릭률을 높일 수 있는 유튜브 썸네일 디자인 컨셉을 5가지 제안해주세요. 각각의 색상, 텍스트, 레이아웃을 구체적으로 설명해주세요.",
                COLUMN_PROMPT_DESCRIPTION to "유튜브 동영상의 클릭률을 극대화하는 썸네일 디자인을 제안합니다. 타겟 연령층과 콘텐츠 특성을 분석하여 5가지 차별화된 썸네일 컨셉을 구체적으로 제공합니다.",
                COLUMN_PROMPT_PURPOSE to "유튜브 콘텐츠 최적화",
                COLUMN_PROMPT_KEYWORDS to "유튜브, 썸네일, 디자인, 마케팅",
                COLUMN_PROMPT_LENGTH to "LONG",
                COLUMN_PROMPT_STYLE to "CREATIVE",
                COLUMN_PROMPT_LANGUAGE to "한국어",
                COLUMN_PROMPT_CREATOR_ID to 2,
                COLUMN_PROMPT_LIKE_COUNT to 67,
                COLUMN_PROMPT_VIEW_COUNT to 289,
                COLUMN_IS_ACTIVE to 1,
                COLUMN_CREATED_AT to System.currentTimeMillis() - 172800000,
                COLUMN_UPDATED_AT to System.currentTimeMillis() - 172800000
            ),

            mapOf(
                COLUMN_ID to 3,
                COLUMN_PROMPT_TITLE to "코드 리뷰 체크리스트 생성",
                COLUMN_PROMPT_CONTENT to "[프로그래밍 언어]와 [프로젝트 유형]에 맞는 코드 리뷰 체크리스트를 작성해주세요. 성능, 보안, 가독성, 유지보수성 관점에서 확인해야 할 항목들을 우선순위별로 정리해주세요.",
                COLUMN_PROMPT_DESCRIPTION to "개발 프로젝트의 코드 품질을 향상시키기 위한 맞춤형 코드 리뷰 체크리스트를 생성합니다. 프로그래밍 언어와 프로젝트 특성에 따라 우선순위가 정해진 체크리스트를 제공합니다.",
                COLUMN_PROMPT_PURPOSE to "코드 품질 관리",
                COLUMN_PROMPT_KEYWORDS to "코드리뷰, 개발, 품질관리, 협업",
                COLUMN_PROMPT_LENGTH to "LONG",
                COLUMN_PROMPT_STYLE to "LOGICAL",
                COLUMN_PROMPT_LANGUAGE to "한국어",
                COLUMN_PROMPT_CREATOR_ID to 3,
                COLUMN_PROMPT_LIKE_COUNT to 89,
                COLUMN_PROMPT_VIEW_COUNT to 412,
                COLUMN_IS_ACTIVE to 1,
                COLUMN_CREATED_AT to System.currentTimeMillis() - 259200000,
                COLUMN_UPDATED_AT to System.currentTimeMillis() - 259200000
            ),

            mapOf(
                COLUMN_ID to 4,
                COLUMN_PROMPT_TITLE to "효율적인 공부 계획 수립",
                COLUMN_PROMPT_CONTENT to "[공부 주제]를 [목표 기간] 동안 마스터하기 위한 학습 계획을 세워주세요. [현재 수준]과 [하루 가능한 공부 시간]을 고려하여 단계별 학습 로드맵을 제시해주세요.",
                COLUMN_PROMPT_DESCRIPTION to "개인의 학습 목표와 현재 수준에 맞는 체계적인 공부 계획을 수립합니다. 제한된 시간 내에서 최대 효율을 낼 수 있는 단계별 학습 로드맵을 제공합니다.",
                COLUMN_PROMPT_PURPOSE to "체계적 학습 관리",
                COLUMN_PROMPT_KEYWORDS to "학습계획, 공부방법, 목표설정, 자기계발",
                COLUMN_PROMPT_LENGTH to "MEDIUM",
                COLUMN_PROMPT_STYLE to "LOGICAL",
                COLUMN_PROMPT_LANGUAGE to "한국어",
                COLUMN_PROMPT_CREATOR_ID to 1,
                COLUMN_PROMPT_LIKE_COUNT to 45,
                COLUMN_PROMPT_VIEW_COUNT to 203,
                COLUMN_IS_ACTIVE to 1,
                COLUMN_CREATED_AT to System.currentTimeMillis() - 345600000,
                COLUMN_UPDATED_AT to System.currentTimeMillis() - 345600000
            ),

            mapOf(
                COLUMN_ID to 5,
                COLUMN_PROMPT_TITLE to "소설 캐릭터 심화 개발",
                COLUMN_PROMPT_CONTENT to "[장르] 소설의 [주인공 역할] 캐릭터를 개발해주세요. [성격 키워드]와 [배경 설정]을 바탕으로 캐릭터의 과거 이야기, 동기, 갈등 요소, 성장 아크를 상세히 구성해주세요.",
                COLUMN_PROMPT_DESCRIPTION to "소설 창작을 위한 입체적이고 매력적인 캐릭터를 개발합니다. 장르와 배경에 맞는 캐릭터의 심리적 깊이와 성장 과정을 구체적으로 설계하여 독자의 몰입감을 높입니다.",
                COLUMN_PROMPT_PURPOSE to "창작 소설 캐릭터 개발",
                COLUMN_PROMPT_KEYWORDS to "소설, 캐릭터, 창작, 스토리텔링",
                COLUMN_PROMPT_LENGTH to "LONG",
                COLUMN_PROMPT_STYLE to "CREATIVE",
                COLUMN_PROMPT_LANGUAGE to "한국어",
                COLUMN_PROMPT_CREATOR_ID to 2,
                COLUMN_PROMPT_LIKE_COUNT to 73,
                COLUMN_PROMPT_VIEW_COUNT to 324,
                COLUMN_IS_ACTIVE to 1,
                COLUMN_CREATED_AT to System.currentTimeMillis() - 432000000,
                COLUMN_UPDATED_AT to System.currentTimeMillis() - 432000000
            )
        )

        samplePrompts.forEach { prompt ->
            val values = ContentValues().apply {
                prompt.forEach { (key, value) ->
                    when (value) {
                        is String -> put(key, value)
                        is Long -> put(key, value)
                        is Int -> put(key, value.toLong())
                    }
                }
            }
            db.insertWithOnConflict(TABLE_PROMPTS, null, values, SQLiteDatabase.CONFLICT_IGNORE)
        }
    }
}