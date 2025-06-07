package com.mikejhill.intellij.movetab.actions

import com.intellij.openapi.actionSystem.AnActionEvent

class MoveTabToEnd : MoveTab() {
    override fun actionPerformed(event: AnActionEvent) {
        super.perform(event, MoveTabDirection.END)
    }
}
