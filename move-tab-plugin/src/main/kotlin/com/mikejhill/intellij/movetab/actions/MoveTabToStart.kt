package com.mikejhill.intellij.movetab.actions

import com.intellij.openapi.actionSystem.AnActionEvent

class MoveTabToStart : MoveTab() {
    override fun actionPerformed(event: AnActionEvent) {
        super.perform(event, MoveTabDirection.START)
    }
}
