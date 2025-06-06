package com.mikejhill.intellij.movetab.settings

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MoveTabSettingsTest : BasePlatformTestCase() {

    @Test
    fun `default wrapAround is true`() {
        assertTrue(MoveTabSettings.wrapAround)
    }

    @Test
    fun `setting wrapAround persists`() {
        val original = MoveTabSettings.wrapAround
        try {
            MoveTabSettings.wrapAround = false
            assertFalse(MoveTabSettings.wrapAround)
            MoveTabSettings.wrapAround = true
            assertTrue(MoveTabSettings.wrapAround)
        } finally {
            MoveTabSettings.wrapAround = original
        }
    }
}
