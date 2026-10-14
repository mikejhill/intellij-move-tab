/*
 * MIT License
 *
 * Copyright (c) 2025 Michael Hill
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.mikejhill.intellij.movetab.settings

import com.intellij.openapi.options.Configurable
import com.intellij.testFramework.LightPlatformTestCase
import com.intellij.util.ui.UIUtil
import javax.swing.JCheckBox
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class MoveTabConfigurableTest : LightPlatformTestCase() {

    private lateinit var configurable: MoveTabConfigurable
    private lateinit var checkBox: JCheckBox

    override fun setUp() {
        super.setUp()
        MoveTabSettings.wrapAround = true
        configurable = MoveTabConfigurable()
        val component = configurable.createComponent()!!
        checkBox = UIUtil.findComponentsOfType(component, JCheckBox::class.java).single()
        configurable.reset()
    }

    override fun tearDown() {
        try {
            configurable.disposeUIResources()
            MoveTabSettings.wrapAround = true
        } finally {
            super.tearDown()
        }
    }

    @Test
    fun has_expected_display_name_and_label() {
        assertEquals("Move Tab", configurable.displayName)
        assertEquals(MoveTabConfigurable.WRAP_AROUND_LABEL, checkBox.text)
    }

    @Test
    fun checkbox_reflects_current_setting() {
        assertTrue(checkBox.isSelected)
        assertFalse(configurable.isModified)
    }

    @Test
    fun toggling_checkbox_marks_modified() {
        checkBox.doClick()
        assertTrue(configurable.isModified)
        checkBox.doClick()
        assertFalse(configurable.isModified)
    }

    @Test
    fun apply_persists_checkbox_state() {
        checkBox.doClick()
        configurable.apply()
        assertFalse(MoveTabSettings.wrapAround)
        assertFalse(configurable.isModified)
    }

    @Test
    fun reset_discards_unapplied_changes() {
        checkBox.doClick()
        configurable.reset()
        assertTrue(checkBox.isSelected)
        assertTrue(MoveTabSettings.wrapAround)
        assertFalse(configurable.isModified)
    }

    @Test
    fun reset_picks_up_external_setting_changes() {
        MoveTabSettings.wrapAround = false
        configurable.reset()
        assertFalse(checkBox.isSelected)
    }

    @Test
    fun is_registered_under_editor_settings() {
        val ep = Configurable.APPLICATION_CONFIGURABLE.extensionList
            .single { it.id == "com.mikejhill.intellij.movetab.settings.MoveTabConfigurable" }
        assertEquals("editor", ep.parentId)
        assertEquals(MoveTabConfigurable::class.java.name, ep.instanceClass)
        assertEquals("Move Tab", ep.displayName)
    }
}
