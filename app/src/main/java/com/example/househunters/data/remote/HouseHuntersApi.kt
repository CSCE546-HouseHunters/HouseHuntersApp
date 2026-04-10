package com.example.househunters.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class HouseHuntersApi(
    @PublishedApi internal val client: OkHttpClient = OkHttpClient(),
    @PublishedApi internal val json: Json = Json {
        ignoreUnknownKeys = true
    }
) {
    suspend inline fun <reified T> get(
        path: String,
        token: String? = null
    ): T = request<T, Unit>("GET", path, body = null, token = token)

    suspend inline fun <reified T, reified B> post(
        path: String,
        body: B,
        token: String? = null
    ): T = request("POST", path, body, token)

    suspend inline fun <reified T, reified B> patch(
        path: String,
        body: B,
        token: String? = null
    ): T = request("PATCH", path, body, token)

    suspend inline fun <reified T, reified B> request(
        method: String,
        path: String,
        body: B? = null,
        token: String? = null
    ): T = withContext(Dispatchers.IO) {
        val builder = Request.Builder()
            .url("$BASE_URL$path")
            .header("Accept", "application/json")

        if (!token.isNullOrBlank()) {
            builder.header("Authorization", "Bearer $token")
        }

        val requestBody = when {
            body == null -> null
            body is String -> body.toRequestBody(JSON_MEDIA_TYPE)
            else -> json.encodeToString(body).toRequestBody(JSON_MEDIA_TYPE)
        }

        when (method) {
            "GET" -> builder.get()
            "DELETE" -> builder.delete()
            else -> builder.method(method, requestBody ?: "{}".toRequestBody(JSON_MEDIA_TYPE))
        }

        client.newCall(builder.build()).execute().use { response ->
            val rawBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("HTTP ${response.code}: ${rawBody.ifBlank { "Unknown error" }}")
            }

            if (T::class == Unit::class) {
                Unit as T
            } else {
                json.decodeFromString<T>(rawBody)
            }
        }
    }

    companion object {
        @PublishedApi
        internal const val BASE_URL = "https://househunterapi-hhhybqa2b4chc7c9.eastus-01.azurewebsites.net"

        @PublishedApi
        internal val JSON_MEDIA_TYPE = "application/json".toMediaType()
    }
}
