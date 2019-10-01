package com.mikejhill.intellij.movetab.actions

import com.intellij.openapi.actionSystem.AnActionEvent

class MoveTabLeft : MoveTab() {

    override fun actionPerformed(event: AnActionEvent) {
        super.perform(event, Direction.LEFT)
    }
}

