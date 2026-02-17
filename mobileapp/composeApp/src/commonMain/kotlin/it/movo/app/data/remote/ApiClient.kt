package it.movo.app.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
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
    tokenProvider: suspend () -> BearerTokens?,
    tokenRefresher: suspend () -> BearerTokens?
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

        install(Auth) {
            bearer {
                loadTokens {
                    tokenProvider()
                }
                refreshTokens {
                    tokenRefresher()
                }
            }
        }

        HttpResponseValidator {
            validateResponse { response: HttpResponse ->
                if (!response.status.isSuccess()) {
                    try {
                        val details = response.body<ProblemDetails>()
                        throw ProblemDetailsException(details)
                    } catch (e: Exception) {
                        if (e is ProblemDetailsException) throw e
                        // Fallback for non-ProblemDetails errors
                    }
                }
            }
        }

        defaultRequest {
            url(BuildConfig.API_BASE_URL)
            contentType(ContentType.Application.Json)
        }
    }
}
