package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.NavSection
import com.example.model.PrompterSettings
import com.example.model.Script
import com.example.ui.screens.ScriptsViewMode
import com.example.viewmodel.CueGreenViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private val app: Application
    get() = ApplicationProvider.getApplicationContext()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("TeX", appName)
  }

  @Test
  fun `test navigation state based switching and backstack`() {
    val viewModel = CueGreenViewModel(app)

    // Default screen is SCRIPTS
    assertEquals(NavSection.SCRIPTS, viewModel.uiState.value.currentSection)
    assertFalse(viewModel.uiState.value.canNavigateBack)

    // Switch to TELEPROMPTER
    viewModel.selectSection(NavSection.TELEPROMPTER)
    assertEquals(NavSection.TELEPROMPTER, viewModel.uiState.value.currentSection)
    assertTrue(viewModel.uiState.value.canNavigateBack)

    // Switch to STUDIO
    viewModel.selectSection(NavSection.STUDIO)
    assertEquals(NavSection.STUDIO, viewModel.uiState.value.currentSection)

    // Switch to SETTINGS
    viewModel.selectSection(NavSection.SETTINGS)
    assertEquals(NavSection.SETTINGS, viewModel.uiState.value.currentSection)

    // Pop back in history: should go back to STUDIO, then TELEPROMPTER, then SCRIPTS
    val backHandled1 = viewModel.navigateBack()
    assertTrue(backHandled1)
    assertEquals(NavSection.STUDIO, viewModel.uiState.value.currentSection)

    val backHandled2 = viewModel.navigateBack()
    assertTrue(backHandled2)
    assertEquals(NavSection.TELEPROMPTER, viewModel.uiState.value.currentSection)

    val backHandled3 = viewModel.navigateBack()
    assertTrue(backHandled3)
    assertEquals(NavSection.SCRIPTS, viewModel.uiState.value.currentSection)
    assertFalse(viewModel.uiState.value.canNavigateBack)

    // Further back press should return false since already at root
    val backHandled4 = viewModel.navigateBack()
    assertFalse(backHandled4)
    assertEquals(NavSection.SCRIPTS, viewModel.uiState.value.currentSection)
  }

  @Test
  fun `test script creation, editing, and deletion in storage`() {
    val viewModel = CueGreenViewModel(app)

    // 1. Start new script
    viewModel.startNewScript()
    assertEquals(ScriptsViewMode.EDITOR, viewModel.uiState.value.scriptsViewMode)
    assertNull(viewModel.uiState.value.editingScript)

    // 2. Validation check: saving empty title and content fails
    val saveEmptyResult = viewModel.saveCurrentScript("", "")
    assertFalse(saveEmptyResult)
    assertNotNull(viewModel.uiState.value.scriptValidationError)

    // 3. Save valid script
    val testTitle = "Live Studio Intro Episode 1"
    val testContent = "Hello and welcome to our live broadcasting session. Pacing is key to audience engagement."
    val saveResult = viewModel.saveCurrentScript(testTitle, testContent)
    assertTrue(saveResult)
    assertEquals(ScriptsViewMode.DASHBOARD, viewModel.uiState.value.scriptsViewMode)

    // Verify it exists in allScripts
    val savedScript = viewModel.allScripts.value.find { it.title == testTitle }
    assertNotNull(savedScript)
    assertEquals(testContent, savedScript!!.content)
    assertEquals(14, savedScript.wordCount)

    // 4. Edit existing script
    viewModel.openEditScript(savedScript)
    assertEquals(ScriptsViewMode.EDITOR, viewModel.uiState.value.scriptsViewMode)
    assertEquals(savedScript.id, viewModel.uiState.value.editingScript?.id)

    val updatedTitle = "Live Studio Intro Episode 1 (Updated)"
    val updatedContent = "Updated content with additional broadcast cues and timing instructions."
    viewModel.saveCurrentScript(updatedTitle, updatedContent)

    // Verify same ID preserved and no duplicates created
    val scriptsWithMatchingId = viewModel.allScripts.value.filter { it.id == savedScript.id }
    assertEquals(1, scriptsWithMatchingId.size)
    assertEquals(updatedTitle, scriptsWithMatchingId[0].title)
    assertEquals(updatedContent, scriptsWithMatchingId[0].content)

    // 5. Delete script
    viewModel.deleteScript(savedScript.id)
    val deletedScriptCheck = viewModel.allScripts.value.find { it.id == savedScript.id }
    assertNull(deletedScriptCheck)
  }

  @Test
  fun `test script model word and character counts`() {
    val script = Script(
      id = "test-123",
      title = "Pacing Test",
      content = "One two three four five."
    )
    assertEquals(5, script.wordCount)
    assertEquals(24, script.characterCount)

    val emptyScript = Script(
      id = "test-empty",
      title = "Empty",
      content = "   "
    )
    assertEquals(0, emptyScript.wordCount)
    assertEquals(3, emptyScript.characterCount)
  }

  @Test
  fun `test teleprompter settings model and persistence`() {
    val viewModel = CueGreenViewModel(app)

    val defaultSettings = viewModel.prompterSettings.value
    assertEquals(28, defaultSettings.fontSizeSp)
    assertEquals(1.5f, defaultSettings.lineHeightMultiplier, 0.01f)
    assertEquals(130, defaultSettings.scrollSpeedWpm)
    assertTrue(defaultSettings.isBold)
    assertFalse(defaultSettings.isMirrored)
    assertEquals(0.35f, defaultSettings.guidePositionPercent, 0.01f)
    assertEquals(3, defaultSettings.countdownSeconds)

    // Verify speed conversion to pixels per second
    assertEquals(50f, defaultSettings.pixelsPerSecond, 0.01f)

    // Update settings
    val customSettings = defaultSettings.copy(
      fontSizeSp = 36,
      scrollSpeedWpm = 180,
      isMirrored = true,
      countdownSeconds = 5
    )
    viewModel.updatePrompterSettings(customSettings)

    val updated = viewModel.prompterSettings.value
    assertEquals(36, updated.fontSizeSp)
    assertEquals(180, updated.scrollSpeedWpm)
    assertTrue(updated.isMirrored)
    assertEquals(5, updated.countdownSeconds)

    // Restore defaults
    viewModel.restorePrompterDefaults()
    val restored = viewModel.prompterSettings.value
    assertEquals(28, restored.fontSizeSp)
    assertEquals(130, restored.scrollSpeedWpm)
    assertFalse(restored.isMirrored)
  }

  @Test
  fun `test teleprompter script selection and back navigation`() {
    val viewModel = CueGreenViewModel(app)
    viewModel.selectSection(NavSection.TELEPROMPTER)

    val scripts = viewModel.allScripts.value
    assertTrue("At least one seed script should exist", scripts.isNotEmpty())

    val firstScript = scripts[0]
    viewModel.selectPrompterScript(firstScript)

    assertEquals(firstScript.id, viewModel.uiState.value.selectedPrompterScriptId)
    assertTrue(viewModel.uiState.value.canNavigateBack)

    // Navigate back: should deselect the script and return to the selector
    val handled = viewModel.navigateBack()
    assertTrue(handled)
    assertNull(viewModel.uiState.value.selectedPrompterScriptId)
  }

  @Test
  fun `test studio settings default values and video model calculations`() {
    val studioSettings = com.example.model.StudioSettings()
    assertEquals(com.example.model.CameraLensFacing.FRONT, studioSettings.lensFacing)
    assertTrue(studioSettings.isAudioEnabled)
    assertTrue(studioSettings.isPreviewMirrored)
    assertEquals(com.example.model.RecordingQuality.STANDARD, studioSettings.recordingQuality)
    assertEquals(3, studioSettings.countdownSeconds)

    val tempFile = java.io.File(app.cacheDir, "test_video.mp4")
    tempFile.writeText("sample video content")
    val recordedVideo = com.example.model.RecordedVideo(
      file = tempFile,
      durationSeconds = 125
    )
    assertEquals("02:05", recordedVideo.formattedDuration)
    assertTrue(recordedVideo.formattedDate.isNotEmpty())
    assertTrue(recordedVideo.fileSizeMb.contains("MB"))
    tempFile.delete()
  }

  @Test
  fun `test ai background settings and mode presets`() {
    val defaultSettings = com.example.model.AiBackgroundSettings()
    assertFalse(defaultSettings.isAiEnabled)
    assertEquals(com.example.model.BackgroundMode.ORIGINAL, defaultSettings.mode)
    assertFalse(defaultSettings.isEffectActive)
    assertEquals("AI: OFF", defaultSettings.statusBadgeText)

    // Test Green Screen Chroma Key
    val greenScreen = defaultSettings.copy(
      mode = com.example.model.BackgroundMode.GREEN_SCREEN,
      isAiEnabled = true
    )
    assertTrue(greenScreen.isEffectActive)
    assertEquals("AI: CHROMA GREEN", greenScreen.statusBadgeText)

    // Test Depth Blur
    val blurSettings = defaultSettings.copy(
      mode = com.example.model.BackgroundMode.BLUR,
      blurIntensity = com.example.model.BlurIntensity.STRONG,
      isAiEnabled = true
    )
    assertTrue(blurSettings.isEffectActive)
    assertEquals("AI: BLUR (STRONG)", blurSettings.statusBadgeText)

    // Test Virtual Studio Presets
    val virtualSet = defaultSettings.copy(
      mode = com.example.model.BackgroundMode.VIRTUAL_STUDIO,
      virtualStudio = com.example.model.VirtualStudioPreset.NEWSROOM,
      isAiEnabled = true
    )
    assertTrue(virtualSet.isEffectActive)
    assertTrue(virtualSet.statusBadgeText.contains("NEWSROOM"))

    // Test Solid Colors
    val solidColors = com.example.model.SolidColorPresets.options
    assertTrue(solidColors.isNotEmpty())
    assertTrue(solidColors.any { it.name.contains("Chroma Green") })
    assertTrue(solidColors.any { it.name.contains("Chroma Blue") })
  }

  @Test
  fun `test composition config default values and layer styling`() {
    val config = com.example.model.CompositionConfig()
    assertTrue(config.isPersonVisible)
    assertTrue(config.isPersonMirrored)
    assertEquals(0.36f, config.personScale, 0.01f)
    assertEquals(com.example.model.PersonLayerShape.CUTOUT, config.personShape)
    assertEquals(com.example.model.LayerBorderAccent.CUE_GREEN, config.borderAccent)
    assertTrue(config.isPrompterVisible)
    assertEquals(130, config.prompterSpeedWpm)
    assertEquals(0.75f, config.prompterOpacity, 0.01f)

    // Test position presets
    val bottomRight = com.example.model.PersonPresetPosition.BOTTOM_RIGHT
    assertEquals(0.68f, bottomRight.targetOffset.x, 0.01f)
    assertEquals(0.68f, bottomRight.targetOffset.y, 0.01f)

    val centerStage = com.example.model.PersonPresetPosition.CENTER
    assertEquals(0.36f, centerStage.targetOffset.x, 0.01f)
    assertEquals(0.40f, centerStage.targetOffset.y, 0.01f)

    // Test gameplay presets
    assertEquals(3, com.example.model.GameplayPreset.entries.size)
    val cyberRunner = com.example.model.GameplayPreset.CYBER_RUNNER
    assertEquals("Neon Cyber Runner", cyberRunner.title)
  }

  @Test
  fun `test composition export result formatting and source detection`() {
    val tempFile = java.io.File(app.cacheDir, "comp_test.mp4")
    tempFile.writeText("sample mp4 data")
    val export = com.example.model.CompositionExportResult(
      file = tempFile,
      durationSeconds = 65,
      title = "Cyber Runner Commentary"
    )
    assertEquals("01:05", export.formattedDuration)
    assertEquals("Cyber Runner Commentary", export.title)

    val imageConfig = com.example.model.CompositionConfig(
      backgroundMedia = com.example.model.BackgroundMedia(
        sourceType = com.example.model.CompositionSourceType.CUSTOM_IMAGE
      )
    )
    assertTrue(imageConfig.isCustomImage)
    assertFalse(imageConfig.isCustomVideo)

    tempFile.delete()
  }
}


