package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.Script
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray

/**
 * On-device local persistent storage repository for CueGreen scripts.
 * Emulates the persistence characteristics of browser localStorage using Android SharedPreferences.
 * Guaranteed to survive app restarts, process termination, and screen rotations.
 */
class ScriptStorage(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _scripts = MutableStateFlow<List<Script>>(emptyList())
    val scripts: StateFlow<List<Script>> = _scripts.asStateFlow()

    init {
        loadScriptsFromDisk()
    }

    private fun loadScriptsFromDisk() {
        val rawJson = prefs.getString(KEY_SCRIPTS_DATA, null)
        if (rawJson.isNullOrBlank()) {
            // First run: seed initial welcome and sample broadcast scripts
            val initialScripts = createInitialSeedScripts()
            _scripts.value = initialScripts
            persistToDisk(initialScripts)
        } else {
            try {
                val array = JSONArray(rawJson)
                val loadedList = mutableListOf<Script>()
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i)
                    if (obj != null) {
                        Script.fromJson(obj)?.let { loadedList.add(it) }
                    }
                }
                // Sort by most recently updated first
                _scripts.value = loadedList.sortedByDescending { it.updatedAt }
            } catch (e: Exception) {
                // Graceful fallback if data is corrupted
                val fallback = createInitialSeedScripts()
                _scripts.value = fallback
                persistToDisk(fallback)
            }
        }
    }

    @Synchronized
    private fun persistToDisk(list: List<Script>) {
        try {
            val jsonArray = JSONArray()
            list.forEach { script ->
                jsonArray.put(script.toJson())
            }
            prefs.edit()
                .putString(KEY_SCRIPTS_DATA, jsonArray.toString())
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Saves a script. If a script with the same ID already exists, updates it.
     * Otherwise prepends a new script.
     */
    @Synchronized
    fun saveScript(script: Script): Script {
        val currentList = _scripts.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.id == script.id }

        val finalScript = if (existingIndex >= 0) {
            // Update existing script without changing its ID or createdAt
            val existing = currentList[existingIndex]
            existing.copy(
                title = script.title.trim(),
                content = script.content,
                updatedAt = System.currentTimeMillis()
            )
        } else {
            // Insert new script
            script.copy(
                title = script.title.trim(),
                updatedAt = System.currentTimeMillis()
            )
        }

        if (existingIndex >= 0) {
            currentList[existingIndex] = finalScript
        } else {
            currentList.add(0, finalScript)
        }

        val sorted = currentList.sortedByDescending { it.updatedAt }
        _scripts.value = sorted
        persistToDisk(sorted)
        return finalScript
    }

    /**
     * Deletes a script by unique ID.
     */
    @Synchronized
    fun deleteScript(scriptId: String) {
        val updated = _scripts.value.filter { it.id != scriptId }
        _scripts.value = updated
        persistToDisk(updated)
    }

    /**
     * Retrieves a single script by ID.
     */
    fun getScriptById(scriptId: String): Script? {
        return _scripts.value.find { it.id == scriptId }
    }

    private fun createInitialSeedScripts(): List<Script> {
        val now = System.currentTimeMillis()
        return listOf(
            Script(
                id = "seed-script-1",
                title = "YouTube: Cinema Camera Tech Breakdown 2026",
                content = "Welcome back creators! Today we are dissecting the top broadcast mirror rigs and sensor advancements. Look directly at your camera lens, breathe steadily, and let the teleprompter guide your pacing naturally. Maintaining confident eye contact builds audience trust instantly.",
                createdAt = now - 3600000 * 24,
                updatedAt = now - 3600000 * 2
            ),
            Script(
                id = "seed-script-2",
                title = "Keynote Address: TeX Product Vision",
                content = "Good morning everyone. In high-stakes broadcast and digital media production, eye contact is trust. TeX puts professional prompt alignment right at the center of your mobile workflow. Here is what we have prepared for you today.",
                createdAt = now - 3600000 * 48,
                updatedAt = now - 3600000 * 5
            ),
            Script(
                id = "seed-script-3",
                title = "Quick Reel: 3 Rules for Studio Lighting",
                content = "Rule one: Key light at 45 degrees. Rule two: Soft fill to lift deep shadows. Rule three: Dial your cue screen brightness so your eyes remain sharp, natural, and engaged with the audience without glare.",
                createdAt = now - 3600000 * 72,
                updatedAt = now - 3600000 * 12
            )
        )
    }

    companion object {
        private const val PREFS_NAME = "cuegreen_scripts_store"
        private const val KEY_SCRIPTS_DATA = "cuegreen_saved_scripts_json"
    }
}
