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

package com.mikejhill.intellij.movetab

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.keymap.KeymapManager
import com.intellij.testFramework.LightPlatformTestCase
import javax.swing.KeyStroke
import com.mikejhill.intellij.movetab.actions.MoveTabLeft
import com.mikejhill.intellij.movetab.actions.MoveTabRight
import com.mikejhill.intellij.movetab.actions.MoveTabToEnd
import com.mikejhill.intellij.movetab.actions.MoveTabToStart
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Verifies that plugin.xml is loaded by the running IDE and registers what users rely on:
 * action IDs (referenced by users' custom keymaps), texts, default shortcuts, and the plugin identity.
 */
@RunWith(JUnit4::class)
class PluginDescriptorTest : LightPlatformTestCase() {

    private val pluginId = PluginId.getId("com.mikejhill.intellij.movetab")

    private val expectedActions = mapOf(
        MoveTabLeft::class.java to "Move Tab Left",
        MoveTabRight::class.java to "Move Tab Right",
        MoveTabToStart::class.java to "Move Tab to Start",
        MoveTabToEnd::class.java to "Move Tab to End",
    )

    @Test
    fun plugin_is_loaded() {
        val plugin = PluginManagerCore.getPlugin(pluginId)
        assertNotNull("plugin should be loaded", plugin)
        assertEquals("MoveTab", plugin!!.name)
    }

    @Test
    fun running_ide_is_within_supported_range() {
        val build = ApplicationInfo.getInstance().build
        println("Testing against ${ApplicationInfo.getInstance().fullApplicationName} ($build)")
        val sinceBuild = PluginManagerCore.getPlugin(pluginId)!!.sinceBuild!!
        assertTrue("IDE $build is older than since-build $sinceBuild", build.baselineVersion >= sinceBuild.substringBefore('.').toInt())
    }

    @Test
    fun all_actions_registered_with_stable_ids_and_texts() {
        val actionManager = ActionManager.getInstance()
        expectedActions.forEach { (actionClass, text) ->
            // Action IDs equal the class names; users' keymaps reference them, so they must not change.
            val action = actionManager.getAction(actionClass.name)
            assertNotNull("action ${actionClass.name} should be registered", action)
            assertInstanceOf(action, actionClass)
            assertEquals(text, action.templatePresentation.text)
        }
    }

    @Test
    fun default_keymap_binds_left_and_right_only() {
        val keymap = KeymapManager.getInstance().getKeymap(KeymapManager.DEFAULT_IDEA_KEYMAP)!!
        fun strokes(actionClass: Class<*>) =
            keymap.getShortcuts(actionClass.name).filterIsInstance<KeyboardShortcut>().map { it.firstKeyStroke }

        assertEquals(listOf(KeyStroke.getKeyStroke("ctrl shift PAGE_UP")), strokes(MoveTabLeft::class.java))
        assertEquals(listOf(KeyStroke.getKeyStroke("ctrl shift PAGE_DOWN")), strokes(MoveTabRight::class.java))
        assertEmpty(strokes(MoveTabToStart::class.java))
        assertEmpty(strokes(MoveTabToEnd::class.java))
    }
}
