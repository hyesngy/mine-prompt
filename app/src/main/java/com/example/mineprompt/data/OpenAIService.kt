package com.example.mineprompt.data

import android.util.Log
import com.example.mineprompt.BuildConfig
import com.example.mineprompt.data.api.ChatMessage
import com.example.mineprompt.data.api.OpenAIApi
import com.example.mineprompt.data.api.OpenAIRequest
import com.example.mineprompt.ui.create.PromptGenerationRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class OpenAIService {

    companion object {
        private const val TAG = "OpenAIService"
        private const val BASE_URL = "https://api.openai.com/v1/"
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            })
            .build()
    }

    private val api: OpenAIApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenAIApi::class.java)
    }

    suspend fun generatePrompt(request: PromptGenerationRequest): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = BuildConfig.OPENAI_API_KEY

                if (apiKey.isEmpty() || apiKey == "\"\"") {
                    return@withContext Result.failure(Exception("OpenAI API 키가 설정되지 않았습니다."))
                }

                val prompt = buildPromptMessage(request)

                val openAIRequest = OpenAIRequest(
                    model = "gpt-4.1-nano",
                    messages = listOf(
                        ChatMessage(
                            role = "system",
                            content = """당신은 프롬프트 생성 전문가입니다. 
                            사용자의 요구사항에 맞는 효과적이고 구체적인 프롬프트를 생성해주세요.
                            생성된 프롬프트는 바로 AI에게 질문할 수 있는 완성된 형태여야 합니다."""
                        ),
                        ChatMessage(
                            role = "user",
                            content = prompt
                        )
                    ),
                    maxTokens = 500,
                    temperature = 0.7
                )

                val response = api.createChatCompletion(
                    authorization = "Bearer $apiKey",
                    request = openAIRequest
                )

                if (response.isSuccessful) {
                    val generatedText = response.body()?.choices?.firstOrNull()?.message?.content
                        ?: throw Exception("응답에서 생성된 텍스트를 찾을 수 없습니다")

                    Log.d(TAG, "프롬프트 생성 성공")
                    Result.success(generatedText.trim())
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "API 호출 실패: ${response.code()} - $errorBody")

                    val errorMessage = when (response.code()) {
                        401 -> "API 키가 유효하지 않습니다."
                        429 -> "API 사용량 한도를 초과했습니다."
                        500, 502, 503 -> "OpenAI 서버에 일시적인 문제가 발생했습니다."
                        else -> "프롬프트 생성 중 오류가 발생했습니다."
                    }

                    Result.failure(Exception(errorMessage))
                }

            } catch (e: Exception) {
                Log.e(TAG, "프롬프트 생성 중 오류 발생", e)
                Result.failure(e)
            }
        }
    }

    suspend fun generateTitle(request: PromptGenerationRequest): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = BuildConfig.OPENAI_API_KEY

                if (apiKey.isEmpty() || apiKey == "\"\"") {
                    return@withContext Result.failure(Exception("OpenAI API 키가 설정되지 않았습니다."))
                }

                val titlePrompt = buildTitlePrompt(request)

                val openAIRequest = OpenAIRequest(
                    model = "gpt-4.1-nano",
                    messages = listOf(
                        ChatMessage(
                            role = "system",
                            content = """당신은 프롬프트 제목 생성 전문가입니다. 
                            주어진 목적과 키워드를 바탕으로 매력적이고 명확한 제목을 생성해주세요.
                            제목은 15자 이내로 간결하게 작성하세요."""
                        ),
                        ChatMessage(
                            role = "user",
                            content = titlePrompt
                        )
                    ),
                    maxTokens = 50,
                    temperature = 0.8
                )

                val response = api.createChatCompletion(
                    authorization = "Bearer $apiKey",
                    request = openAIRequest
                )

                if (response.isSuccessful) {
                    val titleText = response.body()?.choices?.firstOrNull()?.message?.content
                        ?: throw Exception("제목 생성 응답을 찾을 수 없습니다")

                    val cleanTitle = titleText.trim()
                        .replace("\"", "")
                        .replace("제목:", "")
                        .trim()

                    Log.d(TAG, "제목 생성 성공: $cleanTitle")
                    Result.success(cleanTitle)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "제목 생성 API 호출 실패: ${response.code()} - $errorBody")

                    val errorMessage = when (response.code()) {
                        401 -> "API 키가 유효하지 않습니다."
                        429 -> "API 사용량 한도를 초과했습니다."
                        500, 502, 503 -> "OpenAI 서버에 일시적인 문제가 발생했습니다."
                        else -> "제목 생성 중 오류가 발생했습니다."
                    }

                    Result.failure(Exception(errorMessage))
                }

            } catch (e: Exception) {
                Log.e(TAG, "제목 생성 중 오류 발생", e)
                Result.failure(e)
            }
        }
    }

    suspend fun classifyCategory(request: PromptGenerationRequest): Result<List<String>> {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = BuildConfig.OPENAI_API_KEY

                if (apiKey.isEmpty() || apiKey == "\"\"") {
                    return@withContext Result.failure(Exception("OpenAI API 키가 설정되지 않았습니다."))
                }

                val categoryPrompt = buildCategoryPrompt(request)

                val openAIRequest = OpenAIRequest(
                    model = "gpt-4.1-nano",
                    messages = listOf(
                        ChatMessage(
                            role = "system",
                            content = """당신은 프롬프트 카테고리 분류 전문가입니다. 
                            주어진 목적과 키워드를 분석하여 가장 적합한 카테고리 1-2개를 정확하게 선택해주세요.
                            반드시 제공된 카테고리 목록에서만 선택하세요."""
                        ),
                        ChatMessage(
                            role = "user",
                            content = categoryPrompt
                        )
                    ),
                    maxTokens = 100,
                    temperature = 0.3
                )

                val response = api.createChatCompletion(
                    authorization = "Bearer $apiKey",
                    request = openAIRequest
                )

                if (response.isSuccessful) {
                    val categoriesText = response.body()?.choices?.firstOrNull()?.message?.content
                        ?: throw Exception("카테고리 분류 응답을 찾을 수 없습니다")

                    val categories = parseCategoriesFromResponse(categoriesText)
                    Log.d(TAG, "카테고리 분류 성공: $categories")
                    Result.success(categories)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "카테고리 분류 API 호출 실패: ${response.code()} - $errorBody")

                    val errorMessage = when (response.code()) {
                        401 -> "API 키가 유효하지 않습니다."
                        429 -> "API 사용량 한도를 초과했습니다."
                        500, 502, 503 -> "OpenAI 서버에 일시적인 문제가 발생했습니다."
                        else -> "카테고리 분류 중 오류가 발생했습니다."
                    }

                    Result.failure(Exception(errorMessage))
                }

            } catch (e: Exception) {
                Log.e(TAG, "카테고리 분류 중 오류 발생", e)
                Result.failure(e)
            }
        }
    }

    private fun buildTitlePrompt(request: PromptGenerationRequest): String {
        return buildString {
            append("다음 프롬프트의 목적과 키워드를 바탕으로 매력적이고 명확한 제목을 생성해주세요:\n\n")

            append("**목적/의도**: ${request.purpose}\n")
            append("**키워드**: ${request.keywords}\n")
            append("**스타일**: ${request.style}\n\n")

            append("조건:\n")
            append("- 15자 이내의 간결한 제목\n")
            append("- 내용을 명확히 표현\n")
            append("- 매력적이고 클릭하고 싶은 제목\n")
            append("- 따옴표나 '제목:' 등의 접두사 없이 제목만 응답\n\n")

            append("예시: '효과적인 회의 진행 가이드', 'SNS 마케팅 전략', '코딩 실력 향상법'")
        }
    }

    private fun buildCategoryPrompt(request: PromptGenerationRequest): String {
        return buildString {
            append("다음 프롬프트의 목적과 키워드를 분석하여 가장 적합한 카테고리를 1-2개 선택해주세요:\n\n")

            append("**목적/의도**: ${request.purpose}\n")
            append("**키워드**: ${request.keywords}\n")
            append("**스타일**: ${request.style}\n\n")

            append("선택 가능한 카테고리 (정확한 이름으로 응답):\n")
            append("1. 콘텐츠 제작 - 유튜브, 블로그, SNS 등 콘텐츠 관련\n")
            append("2. 비즈니스 - 사업, 경영, 기업 활동 관련\n")
            append("3. 마케팅 - 광고, 홍보, 브랜딩 관련\n")
            append("4. 글쓰기/창작 - 소설, 시나리오, 창작 관련\n")
            append("5. 개발 - 프로그래밍, 코딩, 앱/웹 개발 관련\n")
            append("6. 학습 - 공부, 교육, 연구 관련\n")
            append("7. 생산성 - 업무 효율, 시간 관리 관련\n")
            append("8. 자기계발 - 개인 성장, 스킬 향상 관련\n")
            append("9. 언어 - 외국어, 번역, 언어 학습 관련\n")
            append("10. 재미 - 엔터테인먼트, 게임, 유머 관련\n")
            append("11. 일상 - 생활, 취미, 건강, 요리 등 일반적인 주제\n")
            append("12. 법률/재무 - 법률, 금융, 투자, 세금 관련\n\n")

            append("응답 형식: 정확한 카테고리명을 쉼표로 구분 (예: \"개발, 생산성\" 또는 \"학습\")")
        }
    }

    private fun parseCategoriesFromResponse(response: String): List<String> {
        val validCategories = listOf(
            "콘텐츠 제작", "비즈니스", "마케팅", "글쓰기/창작", "개발",
            "학습", "생산성", "자기계발", "언어", "재미", "일상", "법률/재무"
        )

        val foundCategories = mutableListOf<String>()

        // 응답에서 유효한 카테고리명 찾기
        for (category in validCategories) {
            if (response.contains(category, ignoreCase = true)) {
                foundCategories.add(category)
            }
        }

        // 최대 2개까지만 반환, 없으면 일상으로 기본값
        return foundCategories.take(2).ifEmpty { listOf("일상") }
    }

    private fun buildPromptMessage(request: PromptGenerationRequest): String {
        return buildString {
            append("다음 조건에 맞는 프롬프트를 생성해주세요:\n\n")

            append("**목적/의도**: ${request.purpose}\n")
            append("**포함할 키워드**: ${request.keywords}\n")

            append("**길이**: ")
            when (request.length) {
                "SHORT" -> append("1-2문장의 짧은 프롬프트")
                "MEDIUM" -> append("단락 형태의 중간 길이 프롬프트")
                "LONG" -> append("글 형식의 상세한 긴 프롬프트")
                else -> append("적절한 길이의 프롬프트")
            }
            append("\n")

            append("**말투 스타일**: ")
            when (request.style) {
                "창의적" -> append("창의적이고 혁신적인 접근")
                "공식적" -> append("공식적이고 전문적인 어조")
                "논리적" -> append("논리적이고 체계적인 방식")
                "감성적" -> append("감성적이고 공감적인 톤")
                "전문적" -> append("전문적이고 기술적인 관점")
                else -> append("자연스럽고 일반적인 톤")
            }
            append("\n")

            append("**결과 언어**: ${request.language}\n\n")

            append("위 조건들을 모두 반영하여 실제로 AI에게 질문할 수 있는 완성된 프롬프트를 생성해주세요. ")
            append("프롬프트에는 사용자가 직접 입력할 수 있는 [변수] 형태의 빈칸을 포함해주세요. ")
            append("생성된 프롬프트는 바로 사용할 수 있도록 완전한 형태여야 합니다.")
        }
    }
}