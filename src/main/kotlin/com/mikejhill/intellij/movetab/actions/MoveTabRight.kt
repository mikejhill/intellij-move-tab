package com.mikejhill.intellij.movetab.actions

import com.intellij.openapi.actionSystem.AnActionEvent

class MoveTabRight : MoveTab() {

    override fun actionPerformed(event: AnActionEvent) {
        super.perform(event, Direction.RIGHT)
    }
}

