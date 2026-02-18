package it.movo.app.data.remote

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import it.movo.app.composeApp.BuildConfig
import it.movo.app.data.model.ProblemDetails
import kotlinx.serialization.json.Json

class ProblemDetailsException(val details: ProblemDetails) :
    Exception(details.detail ?: details.title)

fun parseErrorMessage(exception: Exception): String {
    return when (exception) {
        is ProblemDetailsException -> exception.details.detail ?: exception.details.title
        else -> exception.message ?: "An unknown error occurred"
    }
}

fun createHttpClient(
    tokenProvider: () -> String?
): HttpClient {
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        prettyPrint = false
    }

    return HttpClient {
        install(ContentNegotiation) {
            json(json)
        }

        install(Logging) {
            level = LogLevel.BODY
        }

        HttpResponseValidator {
            validateResponse { response: HttpResponse ->
                if (!response.status.isSuccess()) {
                    val body = response.bodyAsText()
                    try {
                        val details = Json.decodeFromString<ProblemDetails>(body)
                        throw ProblemDetailsException(details)
                    } catch (e: ProblemDetailsException) {
                        throw e
                    } catch (_: Exception) {
                        throw Exception("HTTP ${response.status.value}: $body")
                    }
                }
            }
        }

        defaultRequest {
            url(BuildConfig.API_BASE_URL)
            contentType(ContentType.Application.Json)
            val token = tokenProvider()
            if (token != null) {
                header(HttpHeaders.Authorization, "Bearer $token")
            } else {
                Napier.w("No auth token available for request: ${url.buildString()}")
            }
        }
    }
}
