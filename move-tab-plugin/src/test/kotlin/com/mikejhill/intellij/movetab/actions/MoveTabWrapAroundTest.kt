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
import com.mikejhill.intellij.movetab.settings.MoveTabSettings
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.Assertions.assertEquals
import javax.swing.JLabel

@ExtendWith(MockKExtension::class)
class MoveTabWrapAroundTest : BasePlatformTestCase() {

    private var tabList: MutableList<TabInfo> = mutableListOf()
    private var currentTab: TabInfo? = null

    @MockK lateinit var projectMock: Project
    @MockK lateinit var fileEditorManagerMock: FileEditorManagerEx
    @MockK lateinit var editorWindowMock: EditorWindow
    @MockK lateinit var editorCompositeMock: EditorComposite
    @MockK lateinit var tabComponentMock: JBEditorTabs
    @MockK lateinit var actionEventMock: AnActionEvent

    private var originalWrap: Boolean = true

    @BeforeEach
    fun initTest() {
        MockKAnnotations.init(this)
        prepareMocks()
        originalWrap = MoveTabSettings.wrapAround
    }

    @AfterEach
    fun restoreSetting() {
        MoveTabSettings.wrapAround = originalWrap
    }

    private fun prepareMocks() {
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
    fun `move left does not wrap when disabled`() {
        MoveTabSettings.wrapAround = false
        prepareTabList(2, 0)
        MoveTabLeft().actionPerformed(actionEventMock)
        validateTabList(tabList, 1, 2)
    }

    @Test
    fun `move right does not wrap when disabled`() {
        MoveTabSettings.wrapAround = false
        prepareTabList(2, 1)
        MoveTabRight().actionPerformed(actionEventMock)
        validateTabList(tabList, 1, 2)
    }

    private fun prepareTabList(count: Int, selectedIndex: Int) {
        tabList = (1..count).map { index ->
            val tab = TabInfo(JLabel(index.toString()))
            tab.setText(index.toString())
            tab
        }.toMutableList()
        currentTab = tabList[selectedIndex]
    }

    private fun validateTabList(list: List<TabInfo>, vararg expected: Int) {
        val actual = list.map { it.text }
        val exp = expected.map { it.toString() }
        assertEquals(exp, actual)
    }
}
