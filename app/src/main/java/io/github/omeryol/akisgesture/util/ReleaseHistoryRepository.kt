package io.github.omeryol.akisgesture.util

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class ReleaseHistoryRepository(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    fun cached(): List<GithubReleaseHistoryItem> = runCatching {
        val array = JSONArray(preferences.getString(KEY_HISTORY, "[]"))
        (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            GithubReleaseHistoryItem(
                version = item.getString("version"),
                date = item.getString("date"),
                changesTr = item.getJSONArray("tr").toStringList(),
                changesEn = item.getJSONArray("en").toStringList(),
            )
        }
    }.getOrDefault(emptyList())

    fun save(history: List<GithubReleaseHistoryItem>) {
        val array = JSONArray()
        history.forEach { item ->
            array.put(JSONObject().apply {
                put("version", item.version)
                put("date", item.date)
                put("tr", JSONArray(item.changesTr))
                put("en", JSONArray(item.changesEn))
            })
        }
        preferences.edit().putString(KEY_HISTORY, array.toString()).apply()
    }

    companion object {
        private const val PREFERENCES = "release_history"
        private const val KEY_HISTORY = "github_history"
    }
}

private fun JSONArray.toStringList(): List<String> =
    (0 until length()).map { getString(it) }
