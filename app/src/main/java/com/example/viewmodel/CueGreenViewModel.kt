package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PrompterSettingsStorage
import com.example.data.ScriptStorage
import com.example.model.NavSection
import com.example.model.PrompterSettings
import com.example.model.Script
import com.example.ui.screens.ScriptsViewMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class CueGreenUiState(
    val navigationStack: List<NavSection> = listOf(NavSection.SCRIPTS),
    val scriptsViewMode: ScriptsViewMode = ScriptsViewMode.DASHBOARD,
    val searchQuery: String = "",
    val editingScript: Script? = null,
    val draftTitle: String = "",
    val draftContent: String = "",
    val scriptValidationError: String? = null,
    val selectedPrompterScriptId: String? = null,
    val infoNotice: String? = null
) {
    val currentSection: NavSection
        get() = navigationStack.lastOrNull() ?: NavSection.SCRIPTS

    val canNavigateBack: Boolean
        get() = navigationStack.size > 1 ||
                (currentSection == NavSection.SCRIPTS && scriptsViewMode == ScriptsViewMode.EDITOR) ||
                (currentSection == NavSection.TELEPROMPTER && selectedPrompterScriptId != null)
}

class CueGreenViewModel(application: Application) : AndroidViewModel(application) {
    private val scriptStorage = ScriptStorage(application.applicationContext)
    private val prompterSettingsStorage = PrompterSettingsStorage(application.applicationContext)

    private val _uiState = MutableStateFlow(CueGreenUiState())
    val uiState: StateFlow<CueGreenUiState> = _uiState.asStateFlow()

    // Observable flow of all saved scripts from persistent local storage
    val allScripts: StateFlow<List<Script>> = scriptStorage.scripts

    // Observable flow of teleprompter viewing settings from persistent storage
    val prompterSettings: StateFlow<PrompterSettings> = prompterSettingsStorage.settings

    // Filtered scripts combined with search query
    val filteredScripts: StateFlow<List<Script>> = combine(
        allScripts,
        _uiState
    ) { scripts, state ->
        val query = state.searchQuery.trim().lowercase()
        if (query.isEmpty()) {
            scripts
        } else {
            scripts.filter { script ->
                script.title.lowercase().contains(query) ||
                        script.content.lowercase().contains(query)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Navigation Stack Operations
    fun selectSection(section: NavSection) {
        _uiState.update { state ->
            if (state.currentSection == section) {
                state
            } else {
                val newStack = state.navigationStack.filter { it != section } + section
                state.copy(navigationStack = newStack)
            }
        }
    }

    fun navigateBack(): Boolean {
        var handled = false
        _uiState.update { state ->
            // If in script editor mode, back handled by returning to dashboard
            if (state.currentSection == NavSection.SCRIPTS && state.scriptsViewMode == ScriptsViewMode.EDITOR) {
                handled = true
                state.copy(
                    scriptsViewMode = ScriptsViewMode.DASHBOARD,
                    editingScript = null,
                    draftTitle = "",
                    draftContent = "",
                    scriptValidationError = null
                )
            } else if (state.currentSection == NavSection.TELEPROMPTER && state.selectedPrompterScriptId != null) {
                // If in teleprompter reading view, return to teleprompter script selector
                handled = true
                state.copy(selectedPrompterScriptId = null)
            } else if (state.navigationStack.size > 1) {
                handled = true
                val newStack = state.navigationStack.dropLast(1)
                state.copy(navigationStack = newStack)
            } else {
                state
            }
        }
        return handled
    }

    // Script Management Operations (Milestone 2)
    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun startNewScript() {
        _uiState.update {
            it.copy(
                scriptsViewMode = ScriptsViewMode.EDITOR,
                editingScript = null,
                draftTitle = "",
                draftContent = "",
                scriptValidationError = null
            )
        }
    }

    fun openEditScript(script: Script) {
        _uiState.update {
            it.copy(
                scriptsViewMode = ScriptsViewMode.EDITOR,
                editingScript = script,
                draftTitle = script.title,
                draftContent = script.content,
                scriptValidationError = null
            )
        }
    }

    fun updateDraftTitle(title: String) {
        _uiState.update {
            it.copy(
                draftTitle = title,
                scriptValidationError = if (it.scriptValidationError != null && title.isNotBlank()) null else it.scriptValidationError
            )
        }
    }

    fun updateDraftContent(content: String) {
        _uiState.update {
            it.copy(
                draftContent = content,
                scriptValidationError = if (it.scriptValidationError != null && content.isNotBlank()) null else it.scriptValidationError
            )
        }
    }

    fun saveCurrentScript(title: String, content: String): Boolean {
        val trimmedTitle = title.trim()
        val trimmedContent = content.trim()

        // Validation Rule: Must have meaningful title or content
        if (trimmedTitle.isEmpty() && trimmedContent.isEmpty()) {
            _uiState.update {
                it.copy(scriptValidationError = "Please enter a script title or content before saving.")
            }
            return false
        }

        val finalTitle = if (trimmedTitle.isEmpty()) "Untitled Script" else trimmedTitle
        val currentState = _uiState.value
        val existingScript = currentState.editingScript

        val scriptToSave = if (existingScript != null) {
            existingScript.copy(
                title = finalTitle,
                content = content
            )
        } else {
            Script(
                title = finalTitle,
                content = content
            )
        }

        val saved = scriptStorage.saveScript(scriptToSave)

        _uiState.update {
            it.copy(
                scriptsViewMode = ScriptsViewMode.DASHBOARD,
                editingScript = null,
                draftTitle = "",
                draftContent = "",
                scriptValidationError = null,
                infoNotice = "Script \"${saved.title}\" saved successfully."
            )
        }
        return true
    }

    fun cancelEditor() {
        _uiState.update {
            it.copy(
                scriptsViewMode = ScriptsViewMode.DASHBOARD,
                editingScript = null,
                draftTitle = "",
                draftContent = "",
                scriptValidationError = null
            )
        }
    }

    fun deleteScript(scriptId: String) {
        val script = scriptStorage.getScriptById(scriptId)
        val title = script?.title ?: "Script"
        scriptStorage.deleteScript(scriptId)

        _uiState.update {
            it.copy(
                scriptsViewMode = ScriptsViewMode.DASHBOARD,
                editingScript = null,
                draftTitle = "",
                draftContent = "",
                scriptValidationError = null,
                selectedPrompterScriptId = if (it.selectedPrompterScriptId == scriptId) null else it.selectedPrompterScriptId,
                infoNotice = "\"$title\" deleted."
            )
        }
    }

    // Teleprompter Engine Operations (Milestone 3)
    fun selectPrompterScript(script: Script) {
        _uiState.update { it.copy(selectedPrompterScriptId = script.id) }
    }

    fun deselectPrompterScript() {
        _uiState.update { it.copy(selectedPrompterScriptId = null) }
    }

    fun updatePrompterSettings(settings: PrompterSettings) {
        prompterSettingsStorage.updateSettings(settings)
    }

    fun restorePrompterDefaults() {
        val defaults = prompterSettingsStorage.restoreDefaults()
        showNotice("Teleprompter preferences restored to default.")
    }

    fun requestCreateScriptFromPrompter() {
        selectSection(NavSection.SCRIPTS)
        startNewScript()
    }

    fun showNotice(message: String) {
        _uiState.update { it.copy(infoNotice = message) }
    }

    fun clearNotice() {
        _uiState.update { it.copy(infoNotice = null) }
    }
}
