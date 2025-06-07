package com.mikejhill.intellij.movetab.settings

import com.intellij.openapi.options.Configurable
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel
import java.awt.BorderLayout

class MoveTabConfigurable : Configurable {
    private lateinit var wrapCheckBox: JCheckBox

    override fun createComponent(): JComponent {
        wrapCheckBox = JCheckBox("Wrap around when reaching first or last tab", MoveTabSettings.wrapAround)
        return JPanel(BorderLayout()).apply { add(wrapCheckBox, BorderLayout.NORTH) }
    }

    override fun isModified(): Boolean = wrapCheckBox.isSelected != MoveTabSettings.wrapAround

    override fun apply() {
        MoveTabSettings.wrapAround = wrapCheckBox.isSelected
    }

    override fun reset() {
        wrapCheckBox.isSelected = MoveTabSettings.wrapAround
    }

    override fun getDisplayName(): String = "Move Tab"
}
