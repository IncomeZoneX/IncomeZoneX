package com.example.data.ai

import com.example.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiAiService {

    private val apiKey: String = BuildConfig.GEMINI_API_KEY

    private val generativeModel: GenerativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = apiKey
        )
    }

    suspend fun generateContent(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (apiKey.isBlank() || apiKey == "YOUR_GEMINI_API_KEY" || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext Result.failure(IllegalStateException("Gemini API key is not configured."))
            }
            val response = generativeModel.generateContent(prompt)
            val text = response.text ?: ""
            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
