package com.mikejhill.intellij.movetab.settings

import com.intellij.ide.util.PropertiesComponent

object MoveTabSettings {
    private const val WRAP_AROUND_KEY = "com.mikejhill.intellij.movetab.wrapAround"
    private val properties: PropertiesComponent = PropertiesComponent.getInstance()

    var wrapAround: Boolean
        get() = properties.getBoolean(WRAP_AROUND_KEY, true)
        set(value) = properties.setValue(WRAP_AROUND_KEY, value, true)
}
