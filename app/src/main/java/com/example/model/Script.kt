package com.example.model

import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Core Script entity for CueGreen Teleprompter.
 * Contains unique ID, content, creation timestamp, and last-modified timestamp.
 */
data class Script(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val wordCount: Int
        get() {
            val trimmed = content.trim()
            if (trimmed.isEmpty()) return 0
            return trimmed.split(Regex("\\s+")).filter { it.isNotBlank() }.size
        }

    val characterCount: Int
        get() = content.length

    val previewSnippet: String
        get() {
            val cleaned = content.trim().replace(Regex("\\s+"), " ")
            return if (cleaned.length <= 130) cleaned else cleaned.take(130) + "..."
        }

    val formattedModifiedDate: String
        get() {
            val formatter = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
            return formatter.format(Date(updatedAt))
        }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("id", id)
        json.put("title", title)
        json.put("content", content)
        json.put("createdAt", createdAt)
        json.put("updatedAt", updatedAt)
        return json
    }

    companion object {
        fun fromJson(json: JSONObject): Script? {
            return try {
                val id = json.optString("id", "")
                if (id.isEmpty()) return null
                val title = json.optString("title", "Untitled Script")
                val content = json.optString("content", "")
                val createdAt = json.optLong("createdAt", System.currentTimeMillis())
                val updatedAt = json.optLong("updatedAt", createdAt)
                Script(
                    id = id,
                    title = title,
                    content = content,
                    createdAt = createdAt,
                    updatedAt = updatedAt
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
