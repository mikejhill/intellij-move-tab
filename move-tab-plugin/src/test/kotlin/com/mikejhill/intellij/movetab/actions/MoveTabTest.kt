package com.mikejhill.intellij.movetab.actions

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.ActionManagerEx
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.fileEditor.impl.FileEditorManagerImpl
import com.intellij.openapi.project.impl.ProjectImpl
import com.intellij.testFramework.HeavyPlatformTestCase
import com.intellij.testFramework.registerOrReplaceServiceInstance
import java.nio.charset.Charset
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class MoveTabTest : HeavyPlatformTestCase() {

    companion object {
        val ACTION_MOVE_LEFT = MoveTabLeft::class.qualifiedName!!
        val ACTION_MOVE_RIGHT = MoveTabRight::class.qualifiedName!!
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

    private fun invokeAction(actionName: String) {
        @Suppress("DEPRECATION")
        val dataContext = ApplicationManager.getApplication()!!.service<DataManager>().dataContext
        val actionEvent = AnActionEvent(
            dataContext,
            Presentation(),
            ActionPlaces.EDITOR_TAB,
            ActionUiKind.NONE,
            null,
            0,
            ActionManagerEx.getInstanceEx()
        )
        val action = ActionManagerEx.getInstanceEx().getAction(actionName)
        ActionUtil.invokeAction(action, actionEvent, null)
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
}
