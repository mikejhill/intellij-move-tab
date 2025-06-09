package com.mikejhill.intellij.movetab.actions

import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.ActionManagerEx
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.editor.ex.util.EditorUtil
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.fileEditor.impl.FileEditorManagerImpl
import com.intellij.openapi.project.impl.ProjectImpl
import com.intellij.testFramework.HeavyPlatformTestCase
import com.intellij.testFramework.registerOrReplaceServiceInstance
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.nio.charset.Charset

@RunWith(JUnit4::class)
class MoveTabTest : HeavyPlatformTestCase() {

    companion object {
        const val ACTION_MOVE_LEFT = "com.mikejhill.intellij.movetab.actions.MoveTabLeft"
        const val ACTION_MOVE_RIGHT = "com.mikejhill.intellij.movetab.actions.MoveTabRight"
    }

    override fun setUp() {
        super.setUp()

        // Register file editor manager; default TestEditorManagerImpl does not implement window semantics
        @Suppress("UnstableApiUsage")
        val fileEditorManager = FileEditorManagerImpl(project, (project as ProjectImpl).activityScope)
        this.project.registerOrReplaceServiceInstance(
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

    private fun selectFile(position: Int) {
        val fileEditorManager = FileEditorManagerEx.getInstanceEx(project)
        val file = fileEditorManager.currentWindow!!.fileList[position - 1]
        fileEditorManager.currentWindow!!.setSelectedComposite(file, true)
    }

    private fun getFileOrder(): IntArray {
        val fileEditorManager = FileEditorManagerEx.getInstanceEx(project)
        return fileEditorManager.currentWindow!!.fileList.map { Regex("""\d+""").find(it.name)!!.value.toInt() }
            .toIntArray()
    }

    private fun invokeAction(actionName: String) {
        val actionManager = ActionManagerEx.getInstanceEx()
        val action = actionManager.getAction(actionName)
        val fileEditorManager = FileEditorManager.getInstance(project)
        val dataContext = EditorUtil.getEditorEx(fileEditorManager.selectedEditor)
            .let { EditorUtil.getEditorDataContext(it!!) }
        val actionEvent = AnActionEvent(
            dataContext,
            Presentation(),
            ActionPlaces.EDITOR_TAB,
            ActionUiKind.NONE,
            null,
            0,
            actionManager
        )
        ActionUtil.invokeAction(action, actionEvent, null)
    }


    /* ********************************************************************** */
    /* ********************************************************************** */


    @Test
    fun example_test() {
        openFiles(5)
        selectFile(3)
        invokeAction(ACTION_MOVE_LEFT)
        assertOrderedEquals(getFileOrder(), intArrayOf(1, 3, 2, 4, 5))
    }

//    @Test
//    fun test_move_left_with_0_tabs() {
//        prepareTabList(0, null)
//        validateTabList(tabList)
//        MoveTabLeft().actionPerformed(actionEventMock)
//        validateTabList(tabList)
//        MoveTabLeft().actionPerformed(actionEventMock)
//        validateTabList(tabList)
//    }
//
//    @Test
//    fun test_move_left_with_1_tab() {
//        prepareTabList(1, 0)
//        validateTabList(tabList, 1)
//        MoveTabLeft().actionPerformed(actionEventMock)
//        validateTabList(tabList, 1)
//        MoveTabLeft().actionPerformed(actionEventMock)
//        validateTabList(tabList, 1)
//    }
//
//    @Test
//    fun test_move_left_with_2_tabs() {
//        prepareTabList(2, 0)
//        validateTabList(tabList, 1, 2)
//        MoveTabLeft().actionPerformed(actionEventMock)
//        assertEquals(currentTab, tabComponentMock.selectedInfo)
//        validateTabList(tabList, 2, 1)
//        MoveTabLeft().actionPerformed(actionEventMock)
//        assertEquals(currentTab, tabComponentMock.selectedInfo)
//        validateTabList(tabList, 1, 2)
//    }
//
//    @Test
//    fun test_move_left_with_5_tabs() {
//        prepareTabList(5, 0)
//        validateTabList(tabList, 1, 2, 3, 4, 5)
//        MoveTabLeft().actionPerformed(actionEventMock)
//        validateTabList(tabList, 2, 3, 4, 5, 1)
//        MoveTabLeft().actionPerformed(actionEventMock)
//        validateTabList(tabList, 2, 3, 4, 1, 5)
//        MoveTabLeft().actionPerformed(actionEventMock)
//        validateTabList(tabList, 2, 3, 1, 4, 5)
//        MoveTabLeft().actionPerformed(actionEventMock)
//        validateTabList(tabList, 2, 1, 3, 4, 5)
//        MoveTabLeft().actionPerformed(actionEventMock)
//        validateTabList(tabList, 1, 2, 3, 4, 5)
//        MoveTabLeft().actionPerformed(actionEventMock)
//        validateTabList(tabList, 2, 3, 4, 5, 1)
//    }
//
//    @Test
//    fun test_move_left_with_5_tabs_no_selection() {
//        prepareTabList(5, null)
//        validateTabList(tabList, 1, 2, 3, 4, 5)
//        MoveTabLeft().actionPerformed(actionEventMock)
//        validateTabList(tabList, 1, 2, 3, 4, 5)
//        MoveTabLeft().actionPerformed(actionEventMock)
//        validateTabList(tabList, 1, 2, 3, 4, 5)
//    }
//
//    @Test
//    fun test_move_left_with_5_tabs_select_tab_5() {
//        prepareTabList(5, 4)
//        validateTabList(tabList, 1, 2, 3, 4, 5)
//        MoveTabLeft().actionPerformed(actionEventMock)
//        validateTabList(tabList, 1, 2, 3, 5, 4)
//        MoveTabLeft().actionPerformed(actionEventMock)
//        validateTabList(tabList, 1, 2, 5, 3, 4)
//        MoveTabLeft().actionPerformed(actionEventMock)
//        validateTabList(tabList, 1, 5, 2, 3, 4)
//        MoveTabLeft().actionPerformed(actionEventMock)
//        validateTabList(tabList, 5, 1, 2, 3, 4)
//        MoveTabLeft().actionPerformed(actionEventMock)
//        validateTabList(tabList, 1, 2, 3, 4, 5)
//        MoveTabLeft().actionPerformed(actionEventMock)
//        validateTabList(tabList, 1, 2, 3, 5, 4)
//    }
//
//    @Test
//    fun test_move_right_with_0_tabs() {
//        prepareTabList(0, null)
//        validateTabList(tabList)
//        MoveTabRight().actionPerformed(actionEventMock)
//        validateTabList(tabList)
//        MoveTabRight().actionPerformed(actionEventMock)
//        validateTabList(tabList)
//    }
//
//    @Test
//    fun test_move_right_with_1_tab() {
//        prepareTabList(1, 0)
//        validateTabList(tabList, 1)
//        MoveTabRight().actionPerformed(actionEventMock)
//        validateTabList(tabList, 1)
//        MoveTabRight().actionPerformed(actionEventMock)
//        validateTabList(tabList, 1)
//    }
//
//    @Test
//    fun test_move_right_with_2_tabs() {
//        prepareTabList(2, 0)
//        validateTabList(tabList, 1, 2)
//        MoveTabRight().actionPerformed(actionEventMock)
//        assertEquals(currentTab, tabComponentMock.selectedInfo)
//        validateTabList(tabList, 2, 1)
//        MoveTabRight().actionPerformed(actionEventMock)
//        assertEquals(currentTab, tabComponentMock.selectedInfo)
//        validateTabList(tabList, 1, 2)
//    }
//
//    @Test
//    fun test_move_right_with_5_tabs() {
//        prepareTabList(5, 0)
//        validateTabList(tabList, 1, 2, 3, 4, 5)
//        MoveTabRight().actionPerformed(actionEventMock)
//        validateTabList(tabList, 2, 1, 3, 4, 5)
//        MoveTabRight().actionPerformed(actionEventMock)
//        validateTabList(tabList, 2, 3, 1, 4, 5)
//        MoveTabRight().actionPerformed(actionEventMock)
//        validateTabList(tabList, 2, 3, 4, 1, 5)
//        MoveTabRight().actionPerformed(actionEventMock)
//        validateTabList(tabList, 2, 3, 4, 5, 1)
//        MoveTabRight().actionPerformed(actionEventMock)
//        validateTabList(tabList, 1, 2, 3, 4, 5)
//        MoveTabRight().actionPerformed(actionEventMock)
//        validateTabList(tabList, 2, 1, 3, 4, 5)
//    }
//
//    @Test
//    fun test_move_right_with_5_tabs_no_selection() {
//        prepareTabList(5, null)
//        validateTabList(tabList, 1, 2, 3, 4, 5)
//        MoveTabRight().actionPerformed(actionEventMock)
//        validateTabList(tabList, 1, 2, 3, 4, 5)
//        MoveTabRight().actionPerformed(actionEventMock)
//        validateTabList(tabList, 1, 2, 3, 4, 5)
//    }
//
//    @Test
//    fun test_move_right_with_5_tabs_select_tab_5() {
//        prepareTabList(5, 4)
//        validateTabList(tabList, 1, 2, 3, 4, 5)
//        MoveTabRight().actionPerformed(actionEventMock)
//        validateTabList(tabList, 5, 1, 2, 3, 4)
//        MoveTabRight().actionPerformed(actionEventMock)
//        validateTabList(tabList, 1, 5, 2, 3, 4)
//        MoveTabRight().actionPerformed(actionEventMock)
//        validateTabList(tabList, 1, 2, 5, 3, 4)
//        MoveTabRight().actionPerformed(actionEventMock)
//        validateTabList(tabList, 1, 2, 3, 5, 4)
//        MoveTabRight().actionPerformed(actionEventMock)
//        validateTabList(tabList, 1, 2, 3, 4, 5)
//        MoveTabRight().actionPerformed(actionEventMock)
//        validateTabList(tabList, 5, 1, 2, 3, 4)
//    }
//
//    private fun prepareTabList(count: Int, selectedIndex: Int?) {
//        tabList = (1..count).map { tabNum ->
//            val tabName = "${tabNum}"
//            val tabInfo = TabInfo(JLabel(tabName))
//            tabInfo.setText(tabName)
//            return@map tabInfo
//        }.toMutableList()
//        currentTab = if (selectedIndex != null) tabList[selectedIndex] else null
//    }
//
//    private fun validateTabList(tabList: List<TabInfo>, vararg tabIds: Int) {
//        val expectedTabIdList = tabIds.map { it.toString() }
//        val actualTabIdList = tabList.map { it.text }
//        assertOrderedEquals(actualTabIdList, expectedTabIdList)
//    }
//
//    private fun getTabComponent(window: EditorWindow): JBEditorTabs? {
//        return window.selectedComposite?.component?.let {
//            var cmp: Component? = it
//            while (cmp != null && cmp !is JBEditorTabs) cmp = cmp.parent
//            cmp
//        } as JBEditorTabs?
//    }
}
