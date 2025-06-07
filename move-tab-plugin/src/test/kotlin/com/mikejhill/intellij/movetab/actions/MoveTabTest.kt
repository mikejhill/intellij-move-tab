package com.mikejhill.intellij.movetab.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.fileEditor.impl.EditorComposite
import com.intellij.openapi.fileEditor.impl.EditorWindow
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ActionCallback
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.ui.tabs.TabInfo
import com.intellij.ui.tabs.impl.JBEditorTabs
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.impl.annotations.MockK
import kotlin.test.assertEquals
import java.awt.Component
import javax.swing.JLabel

class MoveTabTest : BasePlatformTestCase() {

    // Stateful test properties
    var tabList: MutableList<TabInfo> = mutableListOf()
    var currentTab: TabInfo? = null

    // Mocks
    @MockK lateinit var projectMock: Project
    @MockK lateinit var fileEditorManagerMock: FileEditorManagerEx
    @MockK lateinit var editorWindowMock: EditorWindow
    @MockK lateinit var editorCompositeMock: EditorComposite
    @MockK lateinit var tabComponentMock: JBEditorTabs
    @MockK lateinit var actionEventMock: AnActionEvent

    override fun setUp() {
        super.setUp()
        MockKAnnotations.init(this)
        prepareMocks()
    }

    private fun prepareMocks() {
        // Prepare UI editor tab mocks
        mockkObject(FileEditorManagerEx.Companion)
        every { FileEditorManagerEx.getInstanceEx(projectMock) } returns fileEditorManagerMock
        every { fileEditorManagerMock.currentWindow } returns editorWindowMock
        every { editorWindowMock.selectedComposite?.component } returns tabComponentMock
        every { tabComponentMock.tabs } answers { tabList }
        every { tabComponentMock.selectedInfo } answers { currentTab }
        every { tabComponentMock.removeTab(any()) } answers { tabList.remove(args[0]); ActionCallback.DONE }
        every { tabComponentMock.addTab(any(), any()) } answers { tabList.add(args[1] as Int, args[0] as TabInfo); args[0] as TabInfo }
        every { actionEventMock.project } returns projectMock
    }

    fun testMoveLeftWith0Tabs() {
        prepareTabList(0, null)
        validateTabList(tabList)
        MoveTabLeft().actionPerformed(actionEventMock)
        validateTabList(tabList)
        MoveTabLeft().actionPerformed(actionEventMock)
        validateTabList(tabList)
    }

    fun testMoveLeftWith1Tab() {
        prepareTabList(1, 0)
        validateTabList(tabList, 1)
        MoveTabLeft().actionPerformed(actionEventMock)
        validateTabList(tabList, 1)
        MoveTabLeft().actionPerformed(actionEventMock)
        validateTabList(tabList, 1)
    }

    fun testMoveLeftWith2Tabs() {
        prepareTabList(2, 0)
        validateTabList(tabList, 1, 2)
        MoveTabLeft().actionPerformed(actionEventMock)
        assertEquals(currentTab, tabComponentMock.selectedInfo)
        validateTabList(tabList, 2, 1)
        MoveTabLeft().actionPerformed(actionEventMock)
        assertEquals(currentTab, tabComponentMock.selectedInfo)
        validateTabList(tabList, 1, 2)
    }

    fun testMoveLeftWith5Tabs() {
        prepareTabList(5, 0)
        validateTabList(tabList, 1, 2, 3, 4, 5)
        MoveTabLeft().actionPerformed(actionEventMock)
        validateTabList(tabList, 2, 3, 4, 5, 1)
        MoveTabLeft().actionPerformed(actionEventMock)
        validateTabList(tabList, 2, 3, 4, 1, 5)
        MoveTabLeft().actionPerformed(actionEventMock)
        validateTabList(tabList, 2, 3, 1, 4, 5)
        MoveTabLeft().actionPerformed(actionEventMock)
        validateTabList(tabList, 2, 1, 3, 4, 5)
        MoveTabLeft().actionPerformed(actionEventMock)
        validateTabList(tabList, 1, 2, 3, 4, 5)
        MoveTabLeft().actionPerformed(actionEventMock)
        validateTabList(tabList, 2, 3, 4, 5, 1)
    }

    fun testMoveLeftWith5TabsNoSelection() {
        prepareTabList(5, null)
        validateTabList(tabList, 1, 2, 3, 4, 5)
        MoveTabLeft().actionPerformed(actionEventMock)
        validateTabList(tabList, 1, 2, 3, 4, 5)
        MoveTabLeft().actionPerformed(actionEventMock)
        validateTabList(tabList, 1, 2, 3, 4, 5)
    }

    fun testMoveLeftWith5TabsSelectTab5() {
        prepareTabList(5, 4)
        validateTabList(tabList, 1, 2, 3, 4, 5)
        MoveTabLeft().actionPerformed(actionEventMock)
        validateTabList(tabList, 1, 2, 3, 5, 4)
        MoveTabLeft().actionPerformed(actionEventMock)
        validateTabList(tabList, 1, 2, 5, 3, 4)
        MoveTabLeft().actionPerformed(actionEventMock)
        validateTabList(tabList, 1, 5, 2, 3, 4)
        MoveTabLeft().actionPerformed(actionEventMock)
        validateTabList(tabList, 5, 1, 2, 3, 4)
        MoveTabLeft().actionPerformed(actionEventMock)
        validateTabList(tabList, 1, 2, 3, 4, 5)
        MoveTabLeft().actionPerformed(actionEventMock)
        validateTabList(tabList, 1, 2, 3, 5, 4)
    }

    fun testMoveRightWith0Tabs() {
        prepareTabList(0, null)
        validateTabList(tabList)
        MoveTabRight().actionPerformed(actionEventMock)
        validateTabList(tabList)
        MoveTabRight().actionPerformed(actionEventMock)
        validateTabList(tabList)
    }

    fun testMoveRightWith1Tab() {
        prepareTabList(1, 0)
        validateTabList(tabList, 1)
        MoveTabRight().actionPerformed(actionEventMock)
        validateTabList(tabList, 1)
        MoveTabRight().actionPerformed(actionEventMock)
        validateTabList(tabList, 1)
    }

    fun testMoveRightWith2Tabs() {
        prepareTabList(2, 0)
        validateTabList(tabList, 1, 2)
        MoveTabRight().actionPerformed(actionEventMock)
        assertEquals(currentTab, tabComponentMock.selectedInfo)
        validateTabList(tabList, 2, 1)
        MoveTabRight().actionPerformed(actionEventMock)
        assertEquals(currentTab, tabComponentMock.selectedInfo)
        validateTabList(tabList, 1, 2)
    }

    fun testMoveRightWith5Tabs() {
        prepareTabList(5, 0)
        validateTabList(tabList, 1, 2, 3, 4, 5)
        MoveTabRight().actionPerformed(actionEventMock)
        validateTabList(tabList, 2, 1, 3, 4, 5)
        MoveTabRight().actionPerformed(actionEventMock)
        validateTabList(tabList, 2, 3, 1, 4, 5)
        MoveTabRight().actionPerformed(actionEventMock)
        validateTabList(tabList, 2, 3, 4, 1, 5)
        MoveTabRight().actionPerformed(actionEventMock)
        validateTabList(tabList, 2, 3, 4, 5, 1)
        MoveTabRight().actionPerformed(actionEventMock)
        validateTabList(tabList, 1, 2, 3, 4, 5)
        MoveTabRight().actionPerformed(actionEventMock)
        validateTabList(tabList, 2, 1, 3, 4, 5)
    }

    fun testMoveRightWith5TabsNoSelection() {
        prepareTabList(5, null)
        validateTabList(tabList, 1, 2, 3, 4, 5)
        MoveTabRight().actionPerformed(actionEventMock)
        validateTabList(tabList, 1, 2, 3, 4, 5)
        MoveTabRight().actionPerformed(actionEventMock)
        validateTabList(tabList, 1, 2, 3, 4, 5)
    }

    fun testMoveRightWith5TabsSelectTab5() {
        prepareTabList(5, 4)
        validateTabList(tabList, 1, 2, 3, 4, 5)
        MoveTabRight().actionPerformed(actionEventMock)
        validateTabList(tabList, 5, 1, 2, 3, 4)
        MoveTabRight().actionPerformed(actionEventMock)
        validateTabList(tabList, 1, 5, 2, 3, 4)
        MoveTabRight().actionPerformed(actionEventMock)
        validateTabList(tabList, 1, 2, 5, 3, 4)
        MoveTabRight().actionPerformed(actionEventMock)
        validateTabList(tabList, 1, 2, 3, 5, 4)
        MoveTabRight().actionPerformed(actionEventMock)
        validateTabList(tabList, 1, 2, 3, 4, 5)
        MoveTabRight().actionPerformed(actionEventMock)
        validateTabList(tabList, 5, 1, 2, 3, 4)
    }

    fun testMoveToStartWith0Tabs() {
        prepareTabList(0, null)
        validateTabList(tabList)
        MoveTabToStart().actionPerformed(actionEventMock)
        validateTabList(tabList)
        MoveTabToStart().actionPerformed(actionEventMock)
        validateTabList(tabList)
    }

    fun testMoveToStartWith1Tab() {
        prepareTabList(1, 0)
        validateTabList(tabList, 1)
        MoveTabToStart().actionPerformed(actionEventMock)
        validateTabList(tabList, 1)
        MoveTabToStart().actionPerformed(actionEventMock)
        validateTabList(tabList, 1)
    }

    fun testMoveToStartWith2Tabs() {
        prepareTabList(2, 0)
        validateTabList(tabList, 1, 2)
        MoveTabToStart().actionPerformed(actionEventMock)
        validateTabList(tabList, 1, 2)
        MoveTabToStart().actionPerformed(actionEventMock)
        validateTabList(tabList, 1, 2)
    }

    fun testMoveToStartWith5Tabs() {
        prepareTabList(5, 0)
        validateTabList(tabList, 1, 2, 3, 4, 5)
        MoveTabToStart().actionPerformed(actionEventMock)
        validateTabList(tabList, 1, 2, 3, 4, 5)
        MoveTabToStart().actionPerformed(actionEventMock)
        validateTabList(tabList, 1, 2, 3, 4, 5)
    }

    fun testMoveToStartWith5TabsNoSelection() {
        prepareTabList(5, null)
        validateTabList(tabList, 1, 2, 3, 4, 5)
        MoveTabToStart().actionPerformed(actionEventMock)
        validateTabList(tabList, 1, 2, 3, 4, 5)
        MoveTabToStart().actionPerformed(actionEventMock)
        validateTabList(tabList, 1, 2, 3, 4, 5)
    }

    fun testMoveToStartWith5TabsSelectTab5() {
        prepareTabList(5, 4)
        validateTabList(tabList, 1, 2, 3, 4, 5)
        MoveTabToStart().actionPerformed(actionEventMock)
        validateTabList(tabList, 5, 1, 2, 3, 4)
        MoveTabToStart().actionPerformed(actionEventMock)
        validateTabList(tabList, 5, 1, 2, 3, 4)
    }

    fun testMoveToEndWith0Tabs() {
        prepareTabList(0, null)
        validateTabList(tabList)
        MoveTabToEnd().actionPerformed(actionEventMock)
        validateTabList(tabList)
        MoveTabToEnd().actionPerformed(actionEventMock)
        validateTabList(tabList)
    }

    fun testMoveToEndWith1Tab() {
        prepareTabList(1, 0)
        validateTabList(tabList, 1)
        MoveTabToEnd().actionPerformed(actionEventMock)
        validateTabList(tabList, 1)
        MoveTabToEnd().actionPerformed(actionEventMock)
        validateTabList(tabList, 1)
    }

    fun testMoveToEndWith2Tabs() {
        prepareTabList(2, 0)
        validateTabList(tabList, 1, 2)
        MoveTabToEnd().actionPerformed(actionEventMock)
        validateTabList(tabList, 2, 1)
        MoveTabToEnd().actionPerformed(actionEventMock)
        validateTabList(tabList, 2, 1)
    }

    fun testMoveToEndWith5Tabs() {
        prepareTabList(5, 0)
        validateTabList(tabList, 1, 2, 3, 4, 5)
        MoveTabToEnd().actionPerformed(actionEventMock)
        validateTabList(tabList, 2, 3, 4, 5, 1)
        MoveTabToEnd().actionPerformed(actionEventMock)
        validateTabList(tabList, 2, 3, 4, 5, 1)
    }

    fun testMoveToEndWith5TabsNoSelection() {
        prepareTabList(5, null)
        validateTabList(tabList, 1, 2, 3, 4, 5)
        MoveTabToEnd().actionPerformed(actionEventMock)
        validateTabList(tabList, 1, 2, 3, 4, 5)
        MoveTabToEnd().actionPerformed(actionEventMock)
        validateTabList(tabList, 1, 2, 3, 4, 5)
    }

    fun testMoveToEndWith5TabsSelectTab5() {
        prepareTabList(5, 4)
        validateTabList(tabList, 1, 2, 3, 4, 5)
        MoveTabToEnd().actionPerformed(actionEventMock)
        validateTabList(tabList, 1, 2, 3, 4, 5)
        MoveTabToEnd().actionPerformed(actionEventMock)
        validateTabList(tabList, 1, 2, 3, 4, 5)
    }

    private fun prepareTabList(count: Int, selectedIndex: Int?) {
        tabList = (1..count).map { tabNum ->
            val tabName = "${tabNum}"
            val tabInfo = TabInfo(JLabel(tabName))
            tabInfo.setText(tabName)
            return@map tabInfo
        }.toMutableList()
        currentTab = if (selectedIndex != null) tabList[selectedIndex] else null
    }

    private fun validateTabList(tabList: List<TabInfo>, vararg tabIds: Int) {
        val expectedTabIdList = tabIds.map { it.toString() }
        val actualTabIdList = tabList.map { it.text }
        assertEquals(expectedTabIdList, actualTabIdList)
    }

    private fun getTabComponent(window: EditorWindow): JBEditorTabs? {
        return window.selectedComposite?.component?.let {
            var cmp: Component? = it
            while (cmp != null && cmp !is JBEditorTabs) cmp = cmp.parent
            cmp
        } as JBEditorTabs?
    }
}
