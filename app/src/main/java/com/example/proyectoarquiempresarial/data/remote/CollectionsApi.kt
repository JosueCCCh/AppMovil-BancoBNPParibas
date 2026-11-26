package com.example.proyectoarquiempresarial.data.remote

import com.example.proyectoarquiempresarial.core.ConfigStore
import com.example.proyectoarquiempresarial.data.model.CollectionCase
import com.example.proyectoarquiempresarial.data.model.InitiateRequest
import com.example.proyectoarquiempresarial.data.model.RegisterStrategyRequest
import com.example.proyectoarquiempresarial.data.model.Strategy
import com.example.proyectoarquiempresarial.data.model.toCollectionCase
import com.example.proyectoarquiempresarial.data.model.toStrategy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder

class ApiHttpException(
    val statusCode: Int,
    override val message: String
) : IOException(message)

class CollectionsApi(
    private val client: OkHttpClient,
    private val configStore: ConfigStore
) {

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun getStrategies(token: String): List<Strategy> = withContext(Dispatchers.IO) {
        val body = get(token, "/v1/Collections/Strategy/Retrieve")
        val array = JSONObject(body).optJSONArray("strategies") ?: JSONArray()
        buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                add(item.toStrategy())
            }
        }
    }

    suspend fun initiate(token: String, request: InitiateRequest): CollectionCase =
        withContext(Dispatchers.IO) {
            val payload = JSONObject().apply {
                put("accountReference", request.accountReference)
                put("customerReference", request.customerReference)
                put("currency", request.currency)
                put("overdueAmount", request.overdueAmount)
                put("overdueDays", request.overdueDays)
            }
            JSONObject(post(token, "/v1/Collections/Initiate", payload)).toCollectionCase()
        }

    suspend fun retrieve(token: String, caseId: String): CollectionCase =
        withContext(Dispatchers.IO) {
            val encoded = URLEncoder.encode(caseId, "UTF-8")
            JSONObject(get(token, "/v1/Collections/$encoded/Retrieve")).toCollectionCase()
        }

    suspend fun updateStatus(token: String, caseId: String, status: String): CollectionCase =
        withContext(Dispatchers.IO) {
            val encoded = URLEncoder.encode(caseId, "UTF-8")
            val payload = JSONObject().put("status", status)
            JSONObject(put(token, "/v1/Collections/$encoded/Update", payload)).toCollectionCase()
        }

    suspend fun registerStrategy(
        token: String,
        request: RegisterStrategyRequest
    ): Strategy = withContext(Dispatchers.IO) {
        val payload = JSONObject().apply {
            put("strategyId", request.strategyId)
            put("name", request.name)
            put("minDays", request.minDays)
            put("maxDays", request.maxDays)
            put("action", request.action)
            put("lateFeeRate", request.lateFeeRate)
            put("channel", request.channel)
            put("currency", request.currency)
            put("validFrom", request.validFrom)
            put("validTo", request.validTo)
        }
        JSONObject(post(token, "/v1/Collections/Strategy/Register", payload)).toStrategy()
    }

    private fun get(token: String, path: String): String {
        val request = Request.Builder()
            .url(baseUrl() + path)
            .header("Authorization", token)
            .get()
            .build()
        return execute(request)
    }

    private fun post(token: String, path: String, payload: JSONObject): String {
        val request = Request.Builder()
            .url(baseUrl() + path)
            .header("Authorization", token)
            .post(payload.toString().toRequestBody(jsonMediaType))
            .build()
        return execute(request)
    }

    private fun put(token: String, path: String, payload: JSONObject): String {
        val request = Request.Builder()
            .url(baseUrl() + path)
            .header("Authorization", token)
            .put(payload.toString().toRequestBody(jsonMediaType))
            .build()
        return execute(request)
    }

    private fun execute(request: Request): String {
        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw ApiHttpException(response.code, extractMessage(body, response.code))
            }
            return body
        }
    }

    private fun extractMessage(body: String, statusCode: Int): String {
        if (statusCode == 401) {
            return "No autorizado. Inicia sesión nuevamente."
        }
        if (statusCode == 403) {
            return "No tienes permisos para realizar esta operación."
        }
        val json = runCatching { JSONObject(body) }.getOrNull()
        val serverMessage = json?.optString("error")?.takeIf { it.isNotBlank() }
            ?: json?.optString("message")?.takeIf { it.isNotBlank() }
        if (serverMessage != null) return serverMessage

        return when (statusCode) {
            400 -> "La solicitud no es válida. Revisa los datos ingresados."
            404 -> "No se encontró el caso solicitado."
            409 -> "El caso ya no está en estado OPEN."
            in 500..599 -> "Error interno del servicio (HTTP $statusCode)."
            else -> "Error HTTP $statusCode."
        }
    }

    private fun baseUrl(): String = configStore.apiBaseUrl().trimEnd('/')
}
