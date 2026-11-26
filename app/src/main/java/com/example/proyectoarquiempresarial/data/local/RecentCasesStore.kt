package com.example.proyectoarquiempresarial.data.local

import android.content.Context
import com.example.proyectoarquiempresarial.data.model.CollectionCase
import com.example.proyectoarquiempresarial.data.model.RecentCase
import com.example.proyectoarquiempresarial.data.model.toJson
import com.example.proyectoarquiempresarial.data.model.toRecentCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class RecentCasesStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _items = MutableStateFlow(read())
    val items: StateFlow<List<RecentCase>> = _items.asStateFlow()

    @Synchronized
    fun upsert(collectionCase: CollectionCase) {
        val updated = (listOf(collectionCase.toRecentCase()) + _items.value)
            .distinctBy { it.caseId }
            .take(MAX_ITEMS)
        _items.value = updated
        write(updated)
    }

    @Synchronized
    fun clear() {
        _items.value = emptyList()
        prefs.edit().remove(KEY_ITEMS).apply()
    }

    private fun read(): List<RecentCase> {
        val raw = prefs.getString(KEY_ITEMS, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val recent = item.toRecentCase()
                    if (recent.caseId.isNotBlank()) add(recent)
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun write(items: List<RecentCase>) {
        val array = JSONArray()
        items.forEach { array.put(it.toJson()) }
        prefs.edit().putString(KEY_ITEMS, array.toString()).apply()
    }

    private companion object {
        const val PREFS_NAME = "collectiq_recent_cases"
        const val KEY_ITEMS = "items"
        const val MAX_ITEMS = 12
    }
}
