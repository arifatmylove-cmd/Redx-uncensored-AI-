package com.redxai.data.remote.venice

import com.redxai.data.remote.openrouter.ChatMessage
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

// Venice.ai is OpenAI-compatible but fully uncensored — no content filtering whatsoever
interface VeniceApi {
    @POST("chat/completions")
    suspend fun chat(@Body request: VeniceChatRequest): VeniceChatResponse
}

@JsonClass(generateAdapter = true)
data class VeniceChatRequest(
    @Json(name = "model") val model: String,
    @Json(name = "messages") val messages: List<VeniceMessage>,
    @Json(name = "temperature") val temperature: Double = 0.8,
    @Json(name = "max_tokens") val maxTokens: Int = 8192,
    @Json(name = "venice_parameters") val veniceParameters: VeniceParameters = VeniceParameters()
)

@JsonClass(generateAdapter = true)
data class VeniceParameters(
    // Disable ALL Venice safety features
    @Json(name = "include_venice_system_prompt") val includeVeniceSystemPrompt: Boolean = false
)

@JsonClass(generateAdapter = true)
data class VeniceMessage(
    @Json(name = "role") val role: String,
    @Json(name = "content") val content: String
)

@JsonClass(generateAdapter = true)
data class VeniceChatResponse(
    @Json(name = "id") val id: String? = null,
    @Json(name = "choices") val choices: List<VeniceChoice> = emptyList()
)

@JsonClass(generateAdapter = true)
data class VeniceChoice(
    @Json(name = "message") val message: VeniceMessage,
    @Json(name = "finish_reason") val finishReason: String? = null
)

// Venice model catalog
data class VeniceModelEntry(val id: String, val name: String, val description: String, val free: Boolean = false)

object VeniceModels {
    val models = listOf(
        VeniceModelEntry("dolphin-2.9.3-mistral-nemo-12b",  "Dolphin Mistral Nemo 12B ★",  "Fully uncensored Dolphin model. No filters ever. Best for unrestricted chat.", free = true),
        VeniceModelEntry("llama-3.3-70b",                    "Llama 3.3 70B",                "Meta's smartest open model. Excellent reasoning, coding, and conversation."),
        VeniceModelEntry("mistral-31-24b",                   "Mistral Small 3.1 24B",        "Fast and smart. Great for everyday questions and coding."),
        VeniceModelEntry("deepseek-r1-671b",                 "DeepSeek R1 671B",             "Massive reasoning model. Best for complex logic and code generation."),
        VeniceModelEntry("qwen-2.5-vl-72b",                  "Qwen 2.5 VL 72B",             "Alibaba's multimodal model. Strong at code and analysis."),
        VeniceModelEntry("gemma-3-27b",                      "Gemma 3 27B",                 "Google's open model. Good balance of speed and quality."),
    )
}

@Singleton
class VeniceService @Inject constructor() {

    private fun buildApi(apiKey: String): VeniceApi {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val req = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
                    .build()
                chain.proceed(req)
            }
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        return Retrofit.Builder()
            .baseUrl("https://api.venice.ai/api/v1/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(VeniceApi::class.java)
    }

    suspend fun chat(apiKey: String, model: String, messages: List<ChatMessage>): String {
        if (apiKey.isBlank()) throw IllegalStateException("Venice.ai API key not set. Go to Settings → AI Configuration.")
        val api = buildApi(apiKey)
        try {
            val veniceMessages = messages.map { VeniceMessage(role = it.role, content = it.content) }
            val response = api.chat(VeniceChatRequest(model = model, messages = veniceMessages))
            return response.choices.firstOrNull()?.message?.content
                ?: throw IllegalStateException("Empty response from Venice AI. Try again.")
        } catch (e: retrofit2.HttpException) {
            when (e.code()) {
                401 -> throw IllegalStateException("Invalid Venice API key. Get one free at venice.ai")
                402 -> throw IllegalStateException("Venice account needs credits. Top up at venice.ai/settings/billing")
                429 -> throw IllegalStateException("Rate limit hit. Wait a moment and try again.")
                503 -> throw IllegalStateException("Venice model unavailable. Try a different model.")
                else -> throw IllegalStateException("Venice error ${e.code()}: ${e.message()}")
            }
        } catch (e: java.net.UnknownHostException) {
            throw IllegalStateException("No internet connection. Check your network.")
        } catch (e: java.net.SocketTimeoutException) {
            throw IllegalStateException("Venice request timed out. Try a smaller/faster model.")
        }
    }
}
