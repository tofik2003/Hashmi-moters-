package com.hashmimotors.app.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lightweight, persisted recent-search history (max 12 entries, most recent
 * first). Backed by SharedPreferences so history survives app restarts.
 */
@Singleton
class SearchHistoryStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("hashmi_search_history", Context.MODE_PRIVATE)

    fun get(): List<String> =
        prefs.getString(KEY_RECENT, null)
            ?.split('|')
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?: emptyList()

    fun add(query: String) {
        val q = query.trim()
        if (q.isBlank()) return
        val updated = (listOf(q) + get().filter { !it.equals(q, ignoreCase = true) }).take(12)
        prefs.edit().putString(KEY_RECENT, updated.joinToString("|")).apply()
    }

    fun remove(query: String) {
        val updated = get().filter { !it.equals(query.trim(), ignoreCase = true) }
        prefs.edit().putString(KEY_RECENT, updated.joinToString("|")).apply()
    }

    fun clear() {
        prefs.edit().remove(KEY_RECENT).apply()
    }

    private companion object {
        const val KEY_RECENT = "recent"
    }
}
