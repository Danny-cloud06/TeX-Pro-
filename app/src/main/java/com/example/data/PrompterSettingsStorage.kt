package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.PrompterSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

/**
 * On-device persistent storage for teleprompter viewing and pacing preferences.
 */
class PrompterSettingsStorage(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<PrompterSettings> = _settings.asStateFlow()

    private fun loadSettings(): PrompterSettings {
        val rawJson = prefs.getString(KEY_SETTINGS_DATA, null)
        return if (!rawJson.isNullOrBlank()) {
            try {
                PrompterSettings.fromJson(JSONObject(rawJson))
            } catch (e: Exception) {
                PrompterSettings.DEFAULT
            }
        } else {
            PrompterSettings.DEFAULT
        }
    }

    fun updateSettings(newSettings: PrompterSettings) {
        _settings.value = newSettings
        try {
            prefs.edit()
                .putString(KEY_SETTINGS_DATA, newSettings.toJson().toString())
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun restoreDefaults(): PrompterSettings {
        val defaults = PrompterSettings.DEFAULT
        updateSettings(defaults)
        return defaults
    }

    companion object {
        private const val PREFS_NAME = "cuegreen_prompter_prefs"
        private const val KEY_SETTINGS_DATA = "cuegreen_prompter_settings_json"
    }
}
