package com.example.househunters.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
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

    suspend inline fun <reified T, reified B> put(
        path: String,
        body: B,
        token: String? = null
    ): T = request("PUT", path, body, token)

    suspend inline fun <reified T, reified B> patch(
        path: String,
        body: B,
        token: String? = null
    ): T = request("PATCH", path, body, token)

    suspend inline fun <reified T> delete(
        path: String,
        token: String? = null
    ): T = request<T, Unit>("DELETE", path, body = null, token = token)

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
                throw IllegalStateException(buildErrorMessage(response.code, rawBody))
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

    @PublishedApi
    internal fun buildErrorMessage(code: Int, rawBody: String): String {
        val parsedMessage = rawBody
            .takeIf { it.isNotBlank() }
            ?.let(::extractJsonErrorMessage)
            ?.ifBlank { null }
            ?: rawBody.trim().ifBlank { defaultStatusMessage(code) }

        return "HTTP $code: $parsedMessage"
    }

    private fun extractJsonErrorMessage(rawBody: String): String? = runCatching {
        val element = json.parseToJsonElement(rawBody)
        collectMessages(element).joinToString(" ")
    }.getOrNull()

    private fun collectMessages(element: JsonElement): List<String> = when (element) {
        is JsonObject -> buildList {
            listOf("title", "detail", "message").forEach { key ->
                element[key]
                    ?.let(::extractLeafText)
                    ?.takeIf { it.isNotBlank() }
                    ?.let(::add)
            }

            element["errors"]?.let { errors ->
                when (errors) {
                    is JsonObject -> errors.values
                        .flatMap(::collectMessages)
                        .forEach(::add)

                    else -> collectMessages(errors).forEach(::add)
                }
            }

            if (isEmpty()) {
                element.values
                    .flatMap(::collectMessages)
                    .forEach(::add)
            }
        }

        is JsonArray -> element.flatMap(::collectMessages)
        is JsonPrimitive -> extractLeafText(element)?.let(::listOf) ?: emptyList()
        else -> emptyList()
    }

    private fun extractLeafText(element: JsonElement): String? = when (element) {
        is JsonPrimitive -> element.contentOrNull
        is JsonArray -> element.mapNotNull(::extractLeafText).joinToString(", ")
        is JsonObject -> element.values.mapNotNull(::extractLeafText).joinToString(" ")
        else -> null
    }

    private fun defaultStatusMessage(code: Int): String = when (code) {
        400 -> "The request was rejected."
        401 -> "You need to log in again."
        403 -> "You do not have access to this action."
        404 -> "The requested resource was not found."
        else -> "Unknown error"
    }
}
