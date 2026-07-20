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
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

interface VeniceApi {
    @POST("chat/completions")
    suspend fun chat(@Body request: VeniceChatRequest): VeniceChatResponse

    @GET("models")
    suspend fun listModels(): VeniceModelsResponse
}

@JsonClass(generateAdapter = true)
data class VeniceChatRequest(
    @Json(name = "model") val model: String,
    @Json(name = "messages") val messages: List<VeniceMessage>,
    @Json(name = "temperature") val temperature: Double = 0.8,
    @Json(name = "max_tokens") val maxTokens: Int = 4096,
    @Json(name = "venice_parameters") val veniceParameters: VeniceParameters = VeniceParameters()
)

@JsonClass(generateAdapter = true)
data class VeniceParameters(
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

@JsonClass(generateAdapter = true)
data class VeniceModelsResponse(
    @Json(name = "data") val data: List<VeniceModelInfo> = emptyList()
)

@JsonClass(generateAdapter = true)
data class VeniceModelInfo(
    @Json(name = "id") val id: String = ""
)

data class VeniceModelEntry(val id: String, val name: String, val description: String)

object VeniceModels {
    // These IDs are verified working on Venice.ai free tier
    val models = listOf(
        VeniceModelEntry("dolphin-2.9.3-mistral-nemo-12b", "Dolphin Mistral Nemo 12B ★", "Default · Fully uncensored · No filters ever"),
        VeniceModelEntry("llama-3.3-70b",                  "Llama 3.3 70B",               "Powerful reasoning · Great for complex tasks"),
        VeniceModelEntry("mistral-31-24b",                  "Mistral 3.1 24B",             "Fast & smart · Good for everyday questions"),
        VeniceModelEntry("deepseek-r1-671b",                "DeepSeek R1 671B",            "Advanced reasoning · Best for logic & code"),
        VeniceModelEntry("qwen-2.5-72b",                   "Qwen 2.5 72B",                "Strong at coding and analysis"),
        VeniceModelEntry("gemma-3-27b",                    "Gemma 3 27B",                 "Google open model · Good balance of speed/quality"),
    )
}

@Singleton
class VeniceService @Inject constructor() {

    private fun buildApi(apiKey: String): VeniceApi {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val req = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer ${apiKey.trim()}")
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
        val key = apiKey.trim()
        if (key.isBlank()) throw IllegalStateException(
            "Venice.ai API key not set.\n\nGo to Settings → Venice.ai → paste your key → Save."
        )
        val api = buildApi(key)
        try {
            val veniceMessages = messages.map { VeniceMessage(role = it.role, content = it.content) }
            val response = api.chat(VeniceChatRequest(model = model, messages = veniceMessages))
            val content = response.choices.firstOrNull()?.message?.content
            if (content.isNullOrBlank()) {
                throw IllegalStateException("Venice returned empty response. Try switching to a different model in the model picker (⋮ icon).")
            }
            return content
        } catch (e: retrofit2.HttpException) {
            val body = runCatching { e.response()?.errorBody()?.string() }.getOrNull() ?: ""
            when (e.code()) {
                401 -> throw IllegalStateException("Invalid Venice API key (401).\n\nCheck your key at venice.ai/settings/api and re-save in Settings.")
                402 -> throw IllegalStateException("Venice account needs credits (402).\n\nVisit venice.ai/settings/billing to top up, or use a free model.")
                404 -> throw IllegalStateException("Venice model not found (404): \"$model\"\n\nOpen the model picker (⋮ icon) and choose a different model.")
                422 -> throw IllegalStateException("Venice rejected the request (422).\n\nTry a different model — some models have restrictions.\n\nDetail: ${body.take(150)}")
                429 -> throw IllegalStateException("Venice rate limit hit (429). Wait a moment and try again.")
                503 -> throw IllegalStateException("Venice model is offline (503). Try a different model from the picker.")
                else -> throw IllegalStateException("Venice error ${e.code()}: ${body.take(200).ifBlank { e.message() }}")
            }
        } catch (e: java.net.UnknownHostException) {
            throw IllegalStateException("No internet connection.\n\nCheck your network and try again.")
        } catch (e: java.net.SocketTimeoutException) {
            throw IllegalStateException("Venice request timed out.\n\nThe model is slow right now — try Dolphin Mistral or Llama 3.3 70B.")
        }
    }

    /** Quick key validation — calls /models to check authentication */
    suspend fun testKey(apiKey: String): Result<String> {
        val key = apiKey.trim()
        if (key.isBlank()) return Result.failure(Exception("API key is empty"))
        return try {
            val api = buildApi(key)
            val models = api.listModels()
            val count = models.data.size
            Result.success("✓ Connected — $count models available on your account")
        } catch (e: retrofit2.HttpException) {
            when (e.code()) {
                401 -> Result.failure(Exception("✗ Invalid API key (401) — check it at venice.ai/settings/api"))
                else -> Result.failure(Exception("✗ Venice error ${e.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("✗ ${e.message}"))
        }
    }
}
