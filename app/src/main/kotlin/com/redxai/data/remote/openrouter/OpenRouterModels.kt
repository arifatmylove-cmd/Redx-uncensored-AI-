package com.redxai.data.remote.openrouter

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatRequest(
    @Json(name = "model") val model: String,
    @Json(name = "messages") val messages: List<ChatMessage>,
    @Json(name = "temperature") val temperature: Double = 0.7,
    @Json(name = "max_tokens") val maxTokens: Int = 8192,
    @Json(name = "stream") val stream: Boolean = false
)

@JsonClass(generateAdapter = true)
data class ChatMessage(
    @Json(name = "role") val role: String,
    @Json(name = "content") val content: String
)

@JsonClass(generateAdapter = true)
data class ChatResponse(
    @Json(name = "id") val id: String,
    @Json(name = "choices") val choices: List<Choice>,
    @Json(name = "model") val model: String? = null
)

@JsonClass(generateAdapter = true)
data class Choice(
    @Json(name = "message") val message: ChatMessage,
    @Json(name = "finish_reason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class ModelsResponse(
    @Json(name = "data") val data: List<ModelInfo>
)

@JsonClass(generateAdapter = true)
data class ModelInfo(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String? = null,
    @Json(name = "context_length") val contextLength: Int? = null,
    @Json(name = "pricing") val pricing: ModelPricing? = null
)

@JsonClass(generateAdapter = true)
data class ModelPricing(
    @Json(name = "prompt") val prompt: String? = null,
    @Json(name = "completion") val completion: String? = null
)

// OpenRouter free models (no credits needed)
object RedxModels {
    val models = listOf(
        ModelEntry("google/gemini-2.0-flash-exp:free",       "Gemini 2.0 Flash (FREE ✓)",    "Google's fastest model. FREE. Smart, fast, great for chat & code."),
        ModelEntry("deepseek/deepseek-r1:free",               "DeepSeek R1 (FREE ✓)",         "Elite reasoning model, FREE tier. Best for complex logic & APK builds."),
        ModelEntry("meta-llama/llama-3.3-70b-instruct:free",  "Llama 3.3 70B (FREE ✓)",       "Meta's newest large model. FREE. Very capable for code and reasoning."),
        ModelEntry("microsoft/phi-4-reasoning-plus:free",     "Phi-4 Reasoning Plus (FREE ✓)","Microsoft reasoning model. FREE. Excellent at step-by-step problems."),
        ModelEntry("qwen/qwen3-235b-a22b:free",               "Qwen3 235B (FREE ✓)",          "Alibaba 235B model. FREE. Top-tier intelligence."),
        ModelEntry("mistralai/mistral-7b-instruct:free",      "Mistral 7B (FREE ✓)",          "Lightweight & fast. FREE. Good for everyday questions."),
        ModelEntry("anthropic/claude-sonnet-4-5",             "Claude Sonnet 4.5 ★",          "Anthropic's smartest model. Paid — needs credits."),
        ModelEntry("openai/gpt-4o",                           "GPT-4o",                       "OpenAI flagship. Excellent coding & reasoning. Paid."),
        ModelEntry("deepseek/deepseek-r1",                    "DeepSeek R1 (Paid)",           "DeepSeek R1 without rate limits. Paid."),
        ModelEntry("cognitivecomputations/dolphin-llama-3-70b","Dolphin Llama 3 70B",         "Uncensored. No restrictions. Paid."),
    )
}

data class ModelEntry(val id: String, val name: String, val description: String)
