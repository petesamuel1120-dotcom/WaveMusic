package com.petedev.wavemusic.aiservice

import com.petedev.wavemusic.domain.data.model.metadata.Lyrics

class AiClient {
    private var aiService: AiService? = null
    var host = AIHost.GEMINI
        set(value) {
            field = value
            rebuildAiService()
        }
    var apiKey: String? = null
        set(value) {
            field = value
            rebuildAiService()
        }
    var customModelId: String? = null
        set(value) {
            field = value
            rebuildAiService()
        }
    var openRouterModelId: String? = null
        set(value) {
            field = value
            rebuildAiService()
        }
    var customBaseUrl: String? = null
        set(value) {
            field = value
            rebuildAiService()
        }
    var customHeaders: Map<String, String>? = null
        set(value) {
            field = value
            rebuildAiService()
        }

    private fun rebuildAiService() {
        aiService =
            if (apiKey != null) {
                AiService(
                    aiHost = host,
                    apiKey = apiKey!!,
                    customModelId = customModelId,
                    openRouterModelId = openRouterModelId,
                    customBaseUrl = customBaseUrl,
                    customHeaders = customHeaders,
                )
            } else {
                null
            }
    }

    suspend fun translateLyrics(
        inputLyrics: Lyrics,
        targetLanguage: String,
    ): Result<Lyrics> =
        runCatching {
            val result =
                aiService?.translateLyrics(inputLyrics, targetLanguage)
                    ?: throw IllegalStateException("AI service is not initialized. Please set host and apiKey.")

            validateTranslation(inputLyrics, result)
        }

    /** Translate with an immutable settings snapshot, avoiding races with startup preference collectors. */
    suspend fun translateLyrics(
        inputLyrics: Lyrics,
        targetLanguage: String,
        config: AIConfig,
    ): Result<Lyrics> =
        runCatching {
            require(config.apiKey.isNotBlank()) { "AI API key is not configured." }
            val result =
                AiService(
                    aiHost = config.host,
                    apiKey = config.apiKey,
                    customModelId = config.customModelId,
                    openRouterModelId = config.openRouterModelId,
                    customBaseUrl = config.customBaseUrl,
                    customHeaders = config.customHeaders,
                ).translateLyrics(inputLyrics, targetLanguage)
            validateTranslation(inputLyrics, result)
        }

    private fun validateTranslation(
        inputLyrics: Lyrics,
        result: Lyrics,
    ): Lyrics {
        val originalWords = inputLyrics.lines?.map { it.words } ?: emptyList()
        val translatedWords = result.lines?.map { it.words } ?: emptyList()
        val unchangedCount = originalWords.zip(translatedWords).count { (orig, trans) -> orig == trans }
        val translatableCount = originalWords.count { it.trim().isNotEmpty() && it.trim() != "♫" }
        if (translatableCount > 0 && unchangedCount.toFloat() / translatableCount > 0.8f) {
            throw IllegalStateException("Translation failed or returned empty lyrics or same language.")
        }
        return result
    }
}

data class AIConfig(
    val host: AIHost,
    val apiKey: String,
    val customModelId: String? = null,
    val openRouterModelId: String? = null,
    val customBaseUrl: String? = null,
    val customHeaders: Map<String, String>? = null,
)
