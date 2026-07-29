package com.omer.akisgesture.backup

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.omer.akisgesture.AkisGestureApp
import com.omer.akisgesture.rule.RuleSerializer.toGestureRuleGraph
import com.omer.akisgesture.rule.Presets
import com.omer.akisgesture.settingsDataStore
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

object SettingsBackupManager {
    private const val FORMAT = "akis-gesture-backup"
    private const val VERSION = 1

    suspend fun export(app: AkisGestureApp): String {
        val entries = JSONArray()
        app.settingsDataStore.data.first().asMap()
            .toSortedMap(compareBy { it.name })
            .forEach { (key, value) ->
                val item = JSONObject().put("key", key.name)
                when (value) {
                    is Boolean -> item.put("type", "boolean").put("value", value)
                    is Int -> item.put("type", "int").put("value", value)
                    is Long -> item.put("type", "long").put("value", value)
                    is Float -> item.put("type", "float").put("value", value.toDouble())
                    is String -> item.put("type", "string").put("value", value)
                    is Set<*> -> item.put("type", "string_set").put(
                        "value",
                        JSONArray(value.filterIsInstance<String>().sorted()),
                    )
                    else -> return@forEach
                }
                entries.put(item)
            }
        return JSONObject()
            .put("format", FORMAT)
            .put("version", VERSION)
            .put("createdAt", System.currentTimeMillis())
            .put("entries", entries)
            .toString(2)
    }

    suspend fun import(app: AkisGestureApp, json: String) {
        val root = JSONObject(json)
        require(root.optString("format") == FORMAT) { "Bu dosya Akış Gesture yedeği değil" }
        require(root.optInt("version") == VERSION) { "Yedek sürümü desteklenmiyor" }
        val entries = root.getJSONArray("entries")

        // Validate rules before replacing any current setting.
        for (index in 0 until entries.length()) {
            val item = entries.getJSONObject(index)
            if (item.getString("key") == "gesture_rules_json") {
                item.getString("value").toGestureRuleGraph()
            }
        }

        app.settingsDataStore.edit { prefs ->
            prefs.clear()
            for (index in 0 until entries.length()) {
                val item = entries.getJSONObject(index)
                val key = item.getString("key")
                when (item.getString("type")) {
                    "boolean" -> prefs[booleanPreferencesKey(key)] = item.getBoolean("value")
                    "int" -> prefs[intPreferencesKey(key)] = item.getInt("value")
                    "long" -> prefs[longPreferencesKey(key)] = item.getLong("value")
                    "float" -> prefs[floatPreferencesKey(key)] =
                        item.getDouble("value").toFloat()
                    "string" -> prefs[stringPreferencesKey(key)] = item.getString("value")
                    "string_set" -> {
                        val values = item.getJSONArray("value")
                        prefs[stringSetPreferencesKey(key)] =
                            (0 until values.length()).map(values::getString).toSet()
                    }
                    else -> error("Bilinmeyen yedek alanı")
                }
            }
        }
        app.applyRules(app.loadSavedRules() ?: Presets.DEFAULT)
    }
}
