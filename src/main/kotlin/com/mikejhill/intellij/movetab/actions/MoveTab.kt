package com.mikejhill.intellij.movetab.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.fileEditor.impl.EditorWindow
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.tabs.TabInfo
import com.intellij.ui.tabs.impl.JBEditorTabs
import java.awt.Component
import java.util.function.Consumer

abstract class MoveTab : AnAction(), DumbAware {

    protected fun perform(event: AnActionEvent, direction: Direction) {
        val manager: FileEditorManagerEx = event.project?.let { FileEditorManagerEx.getInstanceEx(it) } ?: return
        val window: EditorWindow = manager.currentWindow ?: return
        val files: Array<VirtualFile> = window.files

        // Guard-clause: Handle trivial cases
        if (!manager.hasOpenedFile() || files.size == 1) {
            return
        }

        // Get editor tab component
        val tabComponent: JBEditorTabs = getTabComponent(window) ?: return
        val tabList = tabComponent.tabs
        val currentTab = tabComponent.selectedInfo

        // Get existing and replacement tab indices
        val existingTabIndex = tabList.indexOf(currentTab)
        val newTabIndex = getNewTabIndex(existingTabIndex, direction, tabList)

        // Reorder tabs list
        tabList.removeAt(existingTabIndex)
        tabList.add(newTabIndex, currentTab)

        // Remove and replace all tabs
        tabComponent.removeAllTabs()
        tabList.forEach(Consumer { tabComponent.addTab(it) })
        manager.openFile(files[existingTabIndex], true, true)
    }

    private fun getTabComponent(window: EditorWindow): JBEditorTabs? {
        return window.selectedComposite?.component?.let {
            var cmp: Component? = it
            while (cmp != null && cmp !is JBEditorTabs) cmp = cmp.parent
            cmp
        } as JBEditorTabs?
    }

    private fun getNewTabIndex(existingTabIndex: Int, direction: Direction, tabList: List<TabInfo>): Int {
        val targetIndex = existingTabIndex + when (direction) {
            Direction.LEFT -> -1
            Direction.RIGHT -> 1
        }
        return when {
            targetIndex < 0 -> tabList.size - 1
            targetIndex >= tabList.size -> 0
            else -> targetIndex
        }
    }

    enum class Direction { LEFT, RIGHT }
}
