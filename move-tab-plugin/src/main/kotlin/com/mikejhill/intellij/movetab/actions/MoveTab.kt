package com.mikejhill.intellij.movetab.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.fileEditor.impl.EditorWindow
import com.intellij.openapi.project.DumbAware
import com.intellij.ui.tabs.TabInfo
import com.intellij.ui.tabs.impl.JBEditorTabs
import com.mikejhill.intellij.movetab.settings.MoveTabSettings
import java.awt.Component


abstract class MoveTab : AnAction(), DumbAware {

    protected fun perform(event: AnActionEvent, direction: MoveTabDirection) {
        val manager: FileEditorManagerEx = event.project?.let { FileEditorManagerEx.getInstanceEx(it) } ?: return
        val window: EditorWindow = manager.currentWindow ?: return

        // Get editor tab component
        val tabComponent: JBEditorTabs = getTabComponent(window) ?: return
        val origTabList = tabComponent.tabs
        val currentTab = tabComponent.selectedInfo

        // Guard-clause: Stop if no tab selected
        if (currentTab == null) {
            return
        }

        // Get tab movement instructions
        val newTabList = getNewTabList(origTabList, currentTab, direction)

        // To avoid effects from the current tab being removed and recreated, move other tabs instead
        val movedTabList =
            origTabList.filterIndexed { index, tabInfo -> tabInfo != newTabList[index] && tabInfo != currentTab }
        movedTabList.forEach { tabComponent.removeTab(it) }
        newTabList.forEachIndexed { index, tabInfo ->
            if (tabInfo in movedTabList) tabComponent.addTab(tabInfo, index)
        }
    }

    private fun getTabComponent(window: EditorWindow): JBEditorTabs? {
        return window.selectedComposite?.component?.let {
            var cmp: Component? = it
            while (cmp != null && cmp !is JBEditorTabs) cmp = cmp.parent
            cmp
        } as JBEditorTabs?
    }


    private fun getNewTabList(
        origTabList: List<TabInfo>,
        currentTab: TabInfo,
        direction: MoveTabDirection
    ): List<TabInfo> {
        // Get new tab index
        val origIndex = origTabList.indexOf(currentTab)
        val targetIndex = origIndex + when (direction) {
            MoveTabDirection.LEFT -> -1
            MoveTabDirection.RIGHT -> 1
        }
        val newIndex = when {
            targetIndex < 0 -> if (MoveTabSettings.wrapAround) origTabList.size - 1 else origIndex
            targetIndex >= origTabList.size -> if (MoveTabSettings.wrapAround) 0 else origIndex
            else -> targetIndex
        }

        // Get mutated list
        return origTabList.toMutableList()
            .apply { removeAt(origIndex) }
            .apply { add(newIndex, currentTab) }
            .toList()
    }

    enum class MoveTabDirection { LEFT, RIGHT }
}
