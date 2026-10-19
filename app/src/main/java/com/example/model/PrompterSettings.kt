package com.example.model

import org.json.JSONObject

/**
 * Persisted preferences for the CueGreen Teleprompter reading engine.
 */
data class PrompterSettings(
    val fontSizeSp: Int = 28,
    val lineHeightMultiplier: Float = 1.5f,
    val scrollSpeedWpm: Int = 130,
    val isBold: Boolean = true,
    val isMirrored: Boolean = false,
    val guidePositionPercent: Float = 0.35f,
    val countdownSeconds: Int = 3,
    val isDistractionFree: Boolean = false
) {
    /**
     * Converts WPM reading speed into a smooth vertical scrolling rate in pixels per second.
     * At 130 WPM, average reading rate is ~2.16 words/sec, which maps smoothly to ~45-55 dp/sec.
     */
    val pixelsPerSecond: Float
        get() {
            // Speed scaling based on font size and target words per minute
            val baseRate = scrollSpeedWpm.toFloat() / 130f
            val fontFactor = fontSizeSp / 28f
            return 50f * baseRate * fontFactor
        }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("fontSizeSp", fontSizeSp)
        json.put("lineHeightMultiplier", lineHeightMultiplier.toDouble())
        json.put("scrollSpeedWpm", scrollSpeedWpm)
        json.put("isBold", isBold)
        json.put("isMirrored", isMirrored)
        json.put("guidePositionPercent", guidePositionPercent.toDouble())
        json.put("countdownSeconds", countdownSeconds)
        json.put("isDistractionFree", isDistractionFree)
        return json
    }

    companion object {
        val DEFAULT = PrompterSettings()

        fun fromJson(json: JSONObject): PrompterSettings {
            return try {
                PrompterSettings(
                    fontSizeSp = json.optInt("fontSizeSp", DEFAULT.fontSizeSp)
                        .coerceIn(18, 56),
                    lineHeightMultiplier = json.optDouble("lineHeightMultiplier", DEFAULT.lineHeightMultiplier.toDouble())
                        .toFloat().coerceIn(1.1f, 2.5f),
                    scrollSpeedWpm = json.optInt("scrollSpeedWpm", DEFAULT.scrollSpeedWpm)
                        .coerceIn(60, 300),
                    isBold = json.optBoolean("isBold", DEFAULT.isBold),
                    isMirrored = json.optBoolean("isMirrored", DEFAULT.isMirrored),
                    guidePositionPercent = json.optDouble("guidePositionPercent", DEFAULT.guidePositionPercent.toDouble())
                        .toFloat().coerceIn(0.15f, 0.75f),
                    countdownSeconds = json.optInt("countdownSeconds", DEFAULT.countdownSeconds),
                    isDistractionFree = json.optBoolean("isDistractionFree", DEFAULT.isDistractionFree)
                )
            } catch (e: Exception) {
                DEFAULT
            }
        }
    }
}
