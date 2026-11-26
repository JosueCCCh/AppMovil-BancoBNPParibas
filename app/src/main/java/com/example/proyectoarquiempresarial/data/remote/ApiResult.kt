package com.example.proyectoarquiempresarial.data.remote

enum class ApiErrorKind {
    VALIDATION,
    UNAUTHORIZED,
    CONFLICT,
    NOT_FOUND,
    NETWORK,
    SERVER,
    UNKNOWN
}

sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class Failure(
        val message: String,
        val code: Int? = null,
        val kind: ApiErrorKind = ApiErrorKind.UNKNOWN
    ) : ApiResult<Nothing>
}
