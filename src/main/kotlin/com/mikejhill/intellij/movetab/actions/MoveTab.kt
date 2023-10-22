package com.mikejhill.intellij.movetab.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.fileEditor.impl.EditorWindow
import com.intellij.openapi.project.DumbAware
import com.intellij.ui.tabs.TabInfo
import com.intellij.ui.tabs.impl.JBEditorTabs
import java.awt.Component


abstract class MoveTab : AnAction(), DumbAware {

    protected fun perform(event: AnActionEvent, direction: Direction) {
        val manager: FileEditorManagerEx = event.project?.let { FileEditorManagerEx.getInstanceEx(it) } ?: return
        val window: EditorWindow = manager.currentWindow ?: return

        // Get editor tab component
        val tabComponent: JBEditorTabs = getTabComponent(window) ?: return
        val tabList = tabComponent.tabs
        val currentTab = tabComponent.selectedInfo

        // Guard-clause: Stop if no tab selected
        if (currentTab == null) {
            return
        }

        // Get tab movement instructions
        val origTabIndex = tabList.indexOf(currentTab)
        val tabMoveCommand = getTabMoveCommand(origTabIndex, direction, tabList)

        // To avoid effects from the current tab being removed and recreated, move other tabs instead
        if (tabMoveCommand.wrap) {
            // Move all other tabs
            val newTabList = tabList.toMutableList()
            newTabList.removeAt(tabMoveCommand.origIndex)
            newTabList.add(tabMoveCommand.newIndex, currentTab)
            newTabList.forEach { if (it != currentTab) tabComponent.removeTab(it) }
            newTabList.forEachIndexed { index, tabInfo -> tabComponent.addTab(tabInfo, index) }
        } else {
            // Move single sibling tab
            val siblingTab = tabComponent.getTabAt(tabMoveCommand.newIndex)
            tabComponent.removeTab(siblingTab)
            tabComponent.addTab(siblingTab, tabMoveCommand.origIndex)
        }
    }

    private fun getTabComponent(window: EditorWindow): JBEditorTabs? {
        return window.selectedComposite?.component?.let {
            var cmp: Component? = it
            while (cmp != null && cmp !is JBEditorTabs) cmp = cmp.parent
            cmp
        } as JBEditorTabs?
    }

    private fun getTabMoveCommand(origIndex: Int, direction: Direction, tabList: List<TabInfo>): TabMoveCommand {
        val targetIndex = origIndex + when (direction) {
            Direction.LEFT -> -1
            Direction.RIGHT -> 1
        }
        return when {
            targetIndex < 0 -> TabMoveCommand(origIndex, tabList.size - 1, true, direction)
            targetIndex >= tabList.size -> TabMoveCommand(origIndex, 0, true, direction)
            else -> TabMoveCommand(origIndex, targetIndex, false, direction)
        }
    }

    data class TabMoveCommand(val origIndex: Int, val newIndex: Int, val wrap: Boolean, val direction: Direction)
    enum class Direction { LEFT, RIGHT }
}
