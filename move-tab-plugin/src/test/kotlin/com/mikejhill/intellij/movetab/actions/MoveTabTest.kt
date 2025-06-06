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
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import java.awt.Component
import javax.swing.JLabel

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
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

    @BeforeAll
    fun setup() {
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

    @Test
    fun `test_move_left_with_0_tabs`() {
        prepareTabList(0, null)
        validateTabList(tabList)
        MoveTabLeft().actionPerformed(actionEventMock)
        validateTabList(tabList)
        MoveTabLeft().actionPerformed(actionEventMock)
        validateTabList(tabList)
    }

    @Test
    fun `test_move_left_with_1_tab`() {
        prepareTabList(1, 0)
        validateTabList(tabList, 1)
        MoveTabLeft().actionPerformed(actionEventMock)
        validateTabList(tabList, 1)
        MoveTabLeft().actionPerformed(actionEventMock)
        validateTabList(tabList, 1)
    }

    @Test
    fun `test_move_left_with_2_tabs`() {
        prepareTabList(2, 0)
        validateTabList(tabList, 1, 2)
        MoveTabLeft().actionPerformed(actionEventMock)
        Assertions.assertEquals(currentTab, tabComponentMock.selectedInfo)
        validateTabList(tabList, 2, 1)
        MoveTabLeft().actionPerformed(actionEventMock)
        Assertions.assertEquals(currentTab, tabComponentMock.selectedInfo)
        validateTabList(tabList, 1, 2)
    }

    @Test
    fun `test_move_left_with_5_tabs`() {
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

    @Test
    fun `test_move_left_with_5_tabs_no_selection`() {
        prepareTabList(5, null)
        validateTabList(tabList, 1, 2, 3, 4, 5)
        MoveTabLeft().actionPerformed(actionEventMock)
        validateTabList(tabList, 1, 2, 3, 4, 5)
        MoveTabLeft().actionPerformed(actionEventMock)
        validateTabList(tabList, 1, 2, 3, 4, 5)
    }

    @Test
    fun `test_move_left_with_5_tabs_select_tab_5`() {
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

    @Test
    fun `test_move_right_with_0_tabs`() {
        prepareTabList(0, null)
        validateTabList(tabList)
        MoveTabRight().actionPerformed(actionEventMock)
        validateTabList(tabList)
        MoveTabRight().actionPerformed(actionEventMock)
        validateTabList(tabList)
    }

    @Test
    fun `test_move_right_with_1_tab`() {
        prepareTabList(1, 0)
        validateTabList(tabList, 1)
        MoveTabRight().actionPerformed(actionEventMock)
        validateTabList(tabList, 1)
        MoveTabRight().actionPerformed(actionEventMock)
        validateTabList(tabList, 1)
    }

    @Test
    fun `test_move_right_with_2_tabs`() {
        prepareTabList(2, 0)
        validateTabList(tabList, 1, 2)
        MoveTabRight().actionPerformed(actionEventMock)
        Assertions.assertEquals(currentTab, tabComponentMock.selectedInfo)
        validateTabList(tabList, 2, 1)
        MoveTabRight().actionPerformed(actionEventMock)
        Assertions.assertEquals(currentTab, tabComponentMock.selectedInfo)
        validateTabList(tabList, 1, 2)
    }

    @Test
    fun `test_move_right_with_5_tabs`() {
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

    @Test
    fun `test_move_right_with_5_tabs_no_selection`() {
        prepareTabList(5, null)
        validateTabList(tabList, 1, 2, 3, 4, 5)
        MoveTabRight().actionPerformed(actionEventMock)
        validateTabList(tabList, 1, 2, 3, 4, 5)
        MoveTabRight().actionPerformed(actionEventMock)
        validateTabList(tabList, 1, 2, 3, 4, 5)
    }

    @Test
    fun `test_move_right_with_5_tabs_select_tab_5`() {
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
        Assertions.assertIterableEquals(expectedTabIdList, actualTabIdList)
    }

    private fun getTabComponent(window: EditorWindow): JBEditorTabs? {
        return window.selectedComposite?.component?.let {
            var cmp: Component? = it
            while (cmp != null && cmp !is JBEditorTabs) cmp = cmp.parent
            cmp
        } as JBEditorTabs?
    }
}
