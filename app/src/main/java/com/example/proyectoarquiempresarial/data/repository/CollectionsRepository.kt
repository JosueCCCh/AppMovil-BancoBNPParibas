package com.example.proyectoarquiempresarial.data.repository

import com.example.proyectoarquiempresarial.data.auth.AuthRepository
import com.example.proyectoarquiempresarial.data.local.RecentCasesStore
import com.example.proyectoarquiempresarial.data.model.CollectionCase
import com.example.proyectoarquiempresarial.data.model.InitiateRequest
import com.example.proyectoarquiempresarial.data.model.RegisterStrategyRequest
import com.example.proyectoarquiempresarial.data.model.Strategy
import com.example.proyectoarquiempresarial.data.remote.ApiErrorKind
import com.example.proyectoarquiempresarial.data.remote.ApiHttpException
import com.example.proyectoarquiempresarial.data.remote.ApiResult
import com.example.proyectoarquiempresarial.data.remote.CollectionsApi
import java.io.IOException

class CollectionsRepository(
    private val api: CollectionsApi,
    private val auth: AuthRepository,
    private val recentCases: RecentCasesStore
) {

    suspend fun getStrategies(): ApiResult<List<Strategy>> = authorized { token ->
        api.getStrategies(token)
    }

    suspend fun initiate(request: InitiateRequest): ApiResult<CollectionCase> =
        authorized { token -> api.initiate(token, request) }
            .also { result ->
                if (result is ApiResult.Success) recentCases.upsert(result.data)
            }

    suspend fun retrieve(caseId: String): ApiResult<CollectionCase> =
        authorized { token -> api.retrieve(token, caseId) }
            .also { result ->
                if (result is ApiResult.Success) recentCases.upsert(result.data)
            }

    suspend fun updateStatus(caseId: String, status: String): ApiResult<CollectionCase> =
        authorized { token -> api.updateStatus(token, caseId, status) }
            .also { result ->
                if (result is ApiResult.Success) recentCases.upsert(result.data)
            }

    suspend fun registerStrategy(request: RegisterStrategyRequest): ApiResult<Strategy> =
        authorized { token -> api.registerStrategy(token, request) }

    private suspend fun <T> authorized(block: suspend (String) -> T): ApiResult<T> {
        val token = auth.validIdToken() ?: return unauthorized()
        return try {
            ApiResult.Success(block(token))
        } catch (error: ApiHttpException) {
            if (error.statusCode == 401) {
                if (auth.refreshIfNeeded(force = true)) {
                    val retryToken = auth.validIdToken()
                    if (retryToken != null) {
                        try {
                            return ApiResult.Success(block(retryToken))
                        } catch (retryError: ApiHttpException) {
                            return failure(retryError)
                        } catch (_: IOException) {
                            return networkError()
                        }
                    }
                }
                auth.logout()
                unauthorized()
            } else {
                failure(error)
            }
        } catch (_: IOException) {
            networkError()
        }
    }

    private fun failure(error: ApiHttpException): ApiResult.Failure {
        val kind = when (error.statusCode) {
            400 -> ApiErrorKind.VALIDATION
            401 -> ApiErrorKind.UNAUTHORIZED
            404 -> ApiErrorKind.NOT_FOUND
            409 -> ApiErrorKind.CONFLICT
            in 500..599 -> ApiErrorKind.SERVER
            else -> ApiErrorKind.UNKNOWN
        }
        return ApiResult.Failure(error.message, error.statusCode, kind)
    }

    private fun unauthorized(): ApiResult.Failure = ApiResult.Failure(
        message = "Tu sesión expiró. Inicia sesión nuevamente.",
        code = 401,
        kind = ApiErrorKind.UNAUTHORIZED
    )

    private fun networkError(): ApiResult.Failure = ApiResult.Failure(
        message = "No se pudo conectar con el servicio de cobranzas. Revisa tu conexión.",
        code = null,
        kind = ApiErrorKind.NETWORK
    )
}
