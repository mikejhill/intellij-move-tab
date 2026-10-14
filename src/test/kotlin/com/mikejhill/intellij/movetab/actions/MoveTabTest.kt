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

package com.mikejhill.intellij.movetab.actions

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.fileEditor.impl.FileEditorManagerImpl
import com.intellij.openapi.project.impl.ProjectImpl
import com.intellij.testFramework.HeavyPlatformTestCase
import com.intellij.testFramework.registerOrReplaceServiceInstance
import java.nio.charset.Charset
import com.mikejhill.intellij.movetab.settings.MoveTabSettings
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class MoveTabTest : HeavyPlatformTestCase() {

    companion object {
        val ACTION_MOVE_LEFT = MoveTabLeft::class.qualifiedName!!
        val ACTION_MOVE_RIGHT = MoveTabRight::class.qualifiedName!!
        val ACTION_MOVE_TO_START = MoveTabToStart::class.qualifiedName!!
        val ACTION_MOVE_TO_END = MoveTabToEnd::class.qualifiedName!!
    }

    override fun setUp() {
        super.setUp()

        // Register file editor manager; default TestEditorManagerImpl does not implement window semantics
        @Suppress("UnstableApiUsage")
        val fileEditorManager = FileEditorManagerImpl(project, (project as ProjectImpl).activityScope)
        project.registerOrReplaceServiceInstance(
            FileEditorManager::class.java,
            fileEditorManager,
            testRootDisposable
        )
    }

    override fun tearDown() {
        MoveTabSettings.wrapAround = true
        super.tearDown()
    }


    /* ********************************************************************** */
    /* ********************************************************************** */


    private fun openFiles(count: Int) {
        val fileEditorManager = FileEditorManager.getInstance(project)
        (1..count).forEach {
            fileEditorManager.openFile(createTempVirtualFile("File${it}.txt", null, "", Charset.defaultCharset()))
        }
    }

    private fun closeFile(position: Int) {
        val fileEditorManager = FileEditorManagerEx.getInstanceEx(project)
        val file = fileEditorManager.currentWindow!!.fileList[position - 1]
        fileEditorManager.closeFile(file)
    }

    private fun selectFile(position: Int) {
        val fileEditorManager = FileEditorManagerEx.getInstanceEx(project)
        val file = fileEditorManager.currentWindow!!.fileList[position - 1]
        fileEditorManager.currentWindow!!.setSelectedComposite(file, true)
    }

    private fun getFileOrder(): IntArray {
        val fileEditorManager = FileEditorManagerEx.getInstanceEx(project)
        val currentWindow =
            fileEditorManager.currentWindow ?: return intArrayOf() // If no windows are open, assume empty
        return currentWindow.fileList
            .map { Regex("""\d+""").find(it.name)!!.value.toInt() }
            .toIntArray()
    }

    private fun createEvent(withProject: Boolean = true): AnActionEvent {
        val dataContext = if (withProject) SimpleDataContext.getProjectContext(project) else SimpleDataContext.EMPTY_CONTEXT
        return AnActionEvent.createEvent(dataContext, Presentation(), ActionPlaces.EDITOR_TAB, ActionUiKind.NONE, null)
    }

    private fun invokeAction(actionName: String) {
        val action = ActionManager.getInstance().getAction(actionName)
        val event = createEvent()
        action.update(event)
        assertTrue("$actionName should be enabled when a project is available", event.presentation.isEnabled)
        action.actionPerformed(event)
    }

    private fun selectedFileNumber(): Int {
        val file = FileEditorManagerEx.getInstanceEx(project).currentWindow!!.selectedFile!!
        return Regex("""\d+""").find(file.name)!!.value.toInt()
    }


    /* ********************************************************************** */
    /* ********************************************************************** */


    @Test
    fun move_left_when_no_window() {
        invokeAction(ACTION_MOVE_LEFT)
        assertOrderedEquals(getFileOrder(), intArrayOf())
    }

    @Test
    fun move_left_when_empty() {
        // Create window, but remove all editors
        openFiles(1)
        closeFile(1)
        invokeAction(ACTION_MOVE_LEFT)
        assertOrderedEquals(getFileOrder(), intArrayOf())
    }

    @Test
    fun move_left_single_tab() {
        openFiles(1)
        selectFile(1)
        invokeAction(ACTION_MOVE_LEFT)
        assertOrderedEquals(getFileOrder(), intArrayOf(1))
    }

    @Test
    fun move_right_single_tab() {
        openFiles(1)
        selectFile(1)
        invokeAction(ACTION_MOVE_RIGHT)
        assertOrderedEquals(getFileOrder(), intArrayOf(1))
    }

    @Test
    fun move_left_from_first_two_tabs() {
        openFiles(2)
        selectFile(1)
        invokeAction(ACTION_MOVE_LEFT)
        assertOrderedEquals(getFileOrder(), intArrayOf(2, 1))
    }

    @Test
    fun move_right_from_last_two_tabs() {
        openFiles(2)
        selectFile(2)
        invokeAction(ACTION_MOVE_RIGHT)
        assertOrderedEquals(getFileOrder(), intArrayOf(2, 1))
    }

    @Test
    fun move_left_from_first() {
        openFiles(5)
        selectFile(1)
        invokeAction(ACTION_MOVE_LEFT)
        assertOrderedEquals(getFileOrder(), intArrayOf(2, 3, 4, 5, 1))
    }

    @Test
    fun move_left_from_middle() {
        openFiles(5)
        selectFile(3)
        invokeAction(ACTION_MOVE_LEFT)
        assertOrderedEquals(getFileOrder(), intArrayOf(1, 3, 2, 4, 5))
    }

    @Test
    fun move_left_from_last() {
        openFiles(5)
        selectFile(5)
        invokeAction(ACTION_MOVE_LEFT)
        assertOrderedEquals(getFileOrder(), intArrayOf(1, 2, 3, 5, 4))
    }

    @Test
    fun move_left_cycle() {
        openFiles(5)
        selectFile(5)
        invokeAction(ACTION_MOVE_LEFT)
        assertOrderedEquals(getFileOrder(), intArrayOf(1, 2, 3, 5, 4))
        invokeAction(ACTION_MOVE_LEFT)
        assertOrderedEquals(getFileOrder(), intArrayOf(1, 2, 5, 3, 4))
        invokeAction(ACTION_MOVE_LEFT)
        assertOrderedEquals(getFileOrder(), intArrayOf(1, 5, 2, 3, 4))
        invokeAction(ACTION_MOVE_LEFT)
        assertOrderedEquals(getFileOrder(), intArrayOf(5, 1, 2, 3, 4))
        invokeAction(ACTION_MOVE_LEFT)
        assertOrderedEquals(getFileOrder(), intArrayOf(1, 2, 3, 4, 5))
    }

    @Test
    fun move_right_when_no_window() {
        invokeAction(ACTION_MOVE_RIGHT)
        assertOrderedEquals(getFileOrder(), intArrayOf())
    }

    @Test
    fun move_right_when_empty() {
        // Create window, but remove all editors
        openFiles(1)
        closeFile(1)
        invokeAction(ACTION_MOVE_RIGHT)
        assertOrderedEquals(getFileOrder(), intArrayOf())
    }


    @Test
    fun move_right_from_first() {
        openFiles(5)
        selectFile(1)
        invokeAction(ACTION_MOVE_RIGHT)
        assertOrderedEquals(getFileOrder(), intArrayOf(2, 1, 3, 4, 5))
    }

    @Test
    fun move_right_from_middle() {
        openFiles(5)
        selectFile(3)
        invokeAction(ACTION_MOVE_RIGHT)
        assertOrderedEquals(getFileOrder(), intArrayOf(1, 2, 4, 3, 5))
    }

    @Test
    fun move_right_from_last() {
        openFiles(5)
        selectFile(5)
        invokeAction(ACTION_MOVE_RIGHT)
        assertOrderedEquals(getFileOrder(), intArrayOf(5, 1, 2, 3, 4))
    }

    @Test
    fun move_right_cycle() {
        openFiles(5)
        selectFile(1)
        invokeAction(ACTION_MOVE_RIGHT)
        assertOrderedEquals(getFileOrder(), intArrayOf(2, 1, 3, 4, 5))
        invokeAction(ACTION_MOVE_RIGHT)
        assertOrderedEquals(getFileOrder(), intArrayOf(2, 3, 1, 4, 5))
        invokeAction(ACTION_MOVE_RIGHT)
        assertOrderedEquals(getFileOrder(), intArrayOf(2, 3, 4, 1, 5))
        invokeAction(ACTION_MOVE_RIGHT)
        assertOrderedEquals(getFileOrder(), intArrayOf(2, 3, 4, 5, 1))
        invokeAction(ACTION_MOVE_RIGHT)
        assertOrderedEquals(getFileOrder(), intArrayOf(1, 2, 3, 4, 5))
    }


    @Test
    fun move_left_and_right() {
        openFiles(5)
        selectFile(3)
        invokeAction(ACTION_MOVE_LEFT)
        assertOrderedEquals(getFileOrder(), intArrayOf(1, 3, 2, 4, 5))
        invokeAction(ACTION_MOVE_RIGHT)
        assertOrderedEquals(getFileOrder(), intArrayOf(1, 2, 3, 4, 5))
        invokeAction(ACTION_MOVE_LEFT)
        assertOrderedEquals(getFileOrder(), intArrayOf(1, 3, 2, 4, 5))
        invokeAction(ACTION_MOVE_LEFT)
        assertOrderedEquals(getFileOrder(), intArrayOf(3, 1, 2, 4, 5))
        invokeAction(ACTION_MOVE_RIGHT)
        assertOrderedEquals(getFileOrder(), intArrayOf(1, 3, 2, 4, 5))
        invokeAction(ACTION_MOVE_RIGHT)
        assertOrderedEquals(getFileOrder(), intArrayOf(1, 2, 3, 4, 5))
        invokeAction(ACTION_MOVE_RIGHT)
        invokeAction(ACTION_MOVE_RIGHT) // Validate "checking" the order on intermediate moves does not impact (Schrödinger's cat)
        assertOrderedEquals(getFileOrder(), intArrayOf(1, 2, 4, 5, 3))
    }


    /* ********************************************************************** */
    /* Move to Start / Move to End                                             */
    /* ********************************************************************** */


    @Test
    fun move_to_start_when_no_window() {
        invokeAction(ACTION_MOVE_TO_START)
        assertOrderedEquals(getFileOrder(), intArrayOf())
    }

    @Test
    fun move_to_end_when_no_window() {
        invokeAction(ACTION_MOVE_TO_END)
        assertOrderedEquals(getFileOrder(), intArrayOf())
    }

    @Test
    fun move_to_start_single_tab() {
        openFiles(1)
        selectFile(1)
        invokeAction(ACTION_MOVE_TO_START)
        assertOrderedEquals(getFileOrder(), intArrayOf(1))
    }

    @Test
    fun move_to_end_single_tab() {
        openFiles(1)
        selectFile(1)
        invokeAction(ACTION_MOVE_TO_END)
        assertOrderedEquals(getFileOrder(), intArrayOf(1))
    }

    @Test
    fun move_to_start_from_first() {
        openFiles(5)
        selectFile(1)
        invokeAction(ACTION_MOVE_TO_START)
        assertOrderedEquals(getFileOrder(), intArrayOf(1, 2, 3, 4, 5))
    }

    @Test
    fun move_to_start_from_middle() {
        openFiles(5)
        selectFile(3)
        invokeAction(ACTION_MOVE_TO_START)
        assertOrderedEquals(getFileOrder(), intArrayOf(3, 1, 2, 4, 5))
    }

    @Test
    fun move_to_start_from_last() {
        openFiles(5)
        selectFile(5)
        invokeAction(ACTION_MOVE_TO_START)
        assertOrderedEquals(getFileOrder(), intArrayOf(5, 1, 2, 3, 4))
    }

    @Test
    fun move_to_end_from_first() {
        openFiles(5)
        selectFile(1)
        invokeAction(ACTION_MOVE_TO_END)
        assertOrderedEquals(getFileOrder(), intArrayOf(2, 3, 4, 5, 1))
    }

    @Test
    fun move_to_end_from_middle() {
        openFiles(5)
        selectFile(3)
        invokeAction(ACTION_MOVE_TO_END)
        assertOrderedEquals(getFileOrder(), intArrayOf(1, 2, 4, 5, 3))
    }

    @Test
    fun move_to_end_from_last() {
        openFiles(5)
        selectFile(5)
        invokeAction(ACTION_MOVE_TO_END)
        assertOrderedEquals(getFileOrder(), intArrayOf(1, 2, 3, 4, 5))
    }

    @Test
    fun move_to_start_then_end() {
        openFiles(5)
        selectFile(3)
        invokeAction(ACTION_MOVE_TO_START)
        assertOrderedEquals(getFileOrder(), intArrayOf(3, 1, 2, 4, 5))
        invokeAction(ACTION_MOVE_TO_END)
        assertOrderedEquals(getFileOrder(), intArrayOf(1, 2, 4, 5, 3))
    }


    /* ********************************************************************** */
    /* Wrap-around disabled                                                    */
    /* ********************************************************************** */


    @Test
    fun move_left_no_wrap_from_first() {
        MoveTabSettings.wrapAround = false
        openFiles(5)
        selectFile(1)
        invokeAction(ACTION_MOVE_LEFT)
        assertOrderedEquals(getFileOrder(), intArrayOf(1, 2, 3, 4, 5))
    }

    @Test
    fun move_right_no_wrap_from_last() {
        MoveTabSettings.wrapAround = false
        openFiles(5)
        selectFile(5)
        invokeAction(ACTION_MOVE_RIGHT)
        assertOrderedEquals(getFileOrder(), intArrayOf(1, 2, 3, 4, 5))
    }

    @Test
    fun move_left_no_wrap_from_middle() {
        MoveTabSettings.wrapAround = false
        openFiles(5)
        selectFile(3)
        invokeAction(ACTION_MOVE_LEFT)
        assertOrderedEquals(getFileOrder(), intArrayOf(1, 3, 2, 4, 5))
    }

    @Test
    fun move_right_no_wrap_from_middle() {
        MoveTabSettings.wrapAround = false
        openFiles(5)
        selectFile(3)
        invokeAction(ACTION_MOVE_RIGHT)
        assertOrderedEquals(getFileOrder(), intArrayOf(1, 2, 4, 3, 5))
    }

    @Test
    fun move_left_wrap_enabled_still_wraps() {
        openFiles(5)
        selectFile(1)
        invokeAction(ACTION_MOVE_LEFT)
        assertOrderedEquals(getFileOrder(), intArrayOf(2, 3, 4, 5, 1))
    }

    @Test
    fun move_right_wrap_enabled_still_wraps() {
        openFiles(5)
        selectFile(5)
        invokeAction(ACTION_MOVE_RIGHT)
        assertOrderedEquals(getFileOrder(), intArrayOf(5, 1, 2, 3, 4))
    }


    /* ********************************************************************** */
    /* Selection, enablement, and edge cases                                   */
    /* ********************************************************************** */


    @Test
    fun moved_tab_stays_selected() {
        openFiles(5)
        selectFile(2)
        listOf(ACTION_MOVE_RIGHT, ACTION_MOVE_RIGHT, ACTION_MOVE_TO_START, ACTION_MOVE_LEFT, ACTION_MOVE_TO_END).forEach {
            invokeAction(it)
            assertEquals("selected tab after $it", 2, selectedFileNumber())
        }
        assertOrderedEquals(getFileOrder(), intArrayOf(1, 3, 4, 5, 2))
    }

    @Test
    fun moving_does_not_close_or_duplicate_tabs() {
        openFiles(6)
        selectFile(4)
        repeat(13) { invokeAction(if (it % 3 == 0) ACTION_MOVE_LEFT else ACTION_MOVE_RIGHT) }
        assertOrderedEquals(getFileOrder().sorted(), listOf(1, 2, 3, 4, 5, 6))
        assertEquals(6, FileEditorManager.getInstance(project).openFiles.size)
    }

    @Test
    fun actions_disabled_without_project() {
        listOf(ACTION_MOVE_LEFT, ACTION_MOVE_RIGHT, ACTION_MOVE_TO_START, ACTION_MOVE_TO_END).forEach {
            val event = createEvent(withProject = false)
            ActionManager.getInstance().getAction(it).update(event)
            assertFalse("$it should be disabled without a project", event.presentation.isEnabled)
        }
    }

    @Test
    fun action_without_project_is_a_no_op() {
        openFiles(3)
        selectFile(1)
        ActionManager.getInstance().getAction(ACTION_MOVE_RIGHT).actionPerformed(createEvent(withProject = false))
        assertOrderedEquals(getFileOrder(), intArrayOf(1, 2, 3))
    }

    @Test
    fun no_wrap_two_tabs_at_edges() {
        MoveTabSettings.wrapAround = false
        openFiles(2)
        selectFile(1)
        invokeAction(ACTION_MOVE_LEFT)
        assertOrderedEquals(getFileOrder(), intArrayOf(1, 2))
        selectFile(2)
        invokeAction(ACTION_MOVE_RIGHT)
        assertOrderedEquals(getFileOrder(), intArrayOf(1, 2))
    }

    @Test
    fun move_to_start_and_end_ignore_wrap_setting() {
        MoveTabSettings.wrapAround = false
        openFiles(4)
        selectFile(3)
        invokeAction(ACTION_MOVE_TO_START)
        assertOrderedEquals(getFileOrder(), intArrayOf(3, 1, 2, 4))
        invokeAction(ACTION_MOVE_TO_END)
        assertOrderedEquals(getFileOrder(), intArrayOf(1, 2, 4, 3))
    }
}
