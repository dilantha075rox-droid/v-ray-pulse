package com.example.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.VlessConfig
import com.example.parser.VlessParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.vlessDataStore: DataStore<Preferences> by preferencesDataStore(name = "vray_pulse_prefs")

class VlessRepository(private val context: Context) {

    private val keyVlessUrl = stringPreferencesKey("last_vless_url")
    private val keyNodesSet = stringSetPreferencesKey("saved_nodes_set")

    val savedVlessConfig: Flow<VlessConfig?> = context.vlessDataStore.data.map { preferences ->
        val rawUrl = preferences[keyVlessUrl]
        if (!rawUrl.isNullOrBlank()) {
            try {
                VlessParser.parse(rawUrl)
            } catch (_: Exception) {
                null
            }
        } else {
            null
        }
    }

    val allNodes: Flow<List<VlessConfig>> = context.vlessDataStore.data.map { preferences ->
        val set = preferences[keyNodesSet] ?: emptySet()
        val list = set.mapNotNull { raw ->
            try {
                VlessParser.parse(raw)
            } catch (_: Exception) {
                null
            }
        }.toMutableList()

        val activeRaw = preferences[keyVlessUrl]
        if (!activeRaw.isNullOrBlank()) {
            try {
                val activeParsed = VlessParser.parse(activeRaw)
                if (list.none { it.rawUrl == activeParsed.rawUrl }) {
                    list.add(0, activeParsed)
                }
            } catch (_: Exception) {}
        }
        list
    }

    suspend fun saveVlessUrl(url: String) {
        val trimmed = url.trim()
        context.vlessDataStore.edit { preferences ->
            preferences[keyVlessUrl] = trimmed
            val currentSet = preferences[keyNodesSet] ?: emptySet()
            preferences[keyNodesSet] = currentSet + trimmed
        }
    }

    suspend fun setActiveNode(url: String) {
        val trimmed = url.trim()
        context.vlessDataStore.edit { preferences ->
            preferences[keyVlessUrl] = trimmed
        }
    }

    suspend fun deleteNode(url: String) {
        val trimmed = url.trim()
        context.vlessDataStore.edit { preferences ->
            val currentSet = preferences[keyNodesSet] ?: emptySet()
            preferences[keyNodesSet] = currentSet - trimmed
            if (preferences[keyVlessUrl] == trimmed) {
                val remaining = currentSet - trimmed
                preferences[keyVlessUrl] = remaining.firstOrNull() ?: ""
            }
        }
    }

    suspend fun clear() {
        context.vlessDataStore.edit { preferences ->
            preferences.remove(keyVlessUrl)
            preferences.remove(keyNodesSet)
        }
    }
}
