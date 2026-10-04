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
    private val keyNodesList = stringPreferencesKey("saved_nodes_list_v3")

    private val DELIMITER = "\n:::\n"
    private val MAX_NODES = 10

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
        val rawListString = preferences[keyNodesList]
        val rawSet = preferences[keyNodesSet] ?: emptySet()

        val rawUrls: List<String> = if (!rawListString.isNullOrBlank()) {
            rawListString.split(DELIMITER).filter { it.isNotBlank() }
        } else {
            rawSet.toList()
        }

        val parsedList = rawUrls.mapNotNull { raw ->
            try {
                VlessParser.parse(raw)
            } catch (_: Exception) {
                null
            }
        }.distinctBy { it.rawUrl }.take(MAX_NODES)

        val activeRaw = preferences[keyVlessUrl]
        val resultList = parsedList.toMutableList()

        if (!activeRaw.isNullOrBlank() && resultList.none { it.rawUrl == activeRaw.trim() }) {
            try {
                val activeParsed = VlessParser.parse(activeRaw)
                resultList.add(0, activeParsed)
            } catch (_: Exception) {}
        }

        resultList.take(MAX_NODES)
    }

    suspend fun saveVlessUrl(url: String) {
        val trimmed = url.trim()
        context.vlessDataStore.edit { preferences ->
            preferences[keyVlessUrl] = trimmed

            val rawListString = preferences[keyNodesList]
            val currentList = if (!rawListString.isNullOrBlank()) {
                rawListString.split(DELIMITER).filter { it.isNotBlank() }
            } else {
                (preferences[keyNodesSet] ?: emptySet()).toList()
            }

            val updatedList = (listOf(trimmed) + currentList.filter { it != trimmed }).take(MAX_NODES)
            preferences[keyNodesList] = updatedList.joinToString(DELIMITER)
            preferences[keyNodesSet] = updatedList.toSet()
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
            val rawListString = preferences[keyNodesList]
            val currentList = if (!rawListString.isNullOrBlank()) {
                rawListString.split(DELIMITER).filter { it.isNotBlank() }
            } else {
                (preferences[keyNodesSet] ?: emptySet()).toList()
            }

            val updatedList = currentList.filter { it != trimmed }
            preferences[keyNodesList] = updatedList.joinToString(DELIMITER)
            preferences[keyNodesSet] = updatedList.toSet()

            if (preferences[keyVlessUrl] == trimmed) {
                preferences[keyVlessUrl] = updatedList.firstOrNull() ?: ""
            }
        }
    }

    suspend fun clear() {
        context.vlessDataStore.edit { preferences ->
            preferences.remove(keyVlessUrl)
            preferences.remove(keyNodesSet)
            preferences.remove(keyNodesList)
        }
    }
}
