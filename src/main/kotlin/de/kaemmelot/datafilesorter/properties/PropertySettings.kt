package de.kaemmelot.datafilesorter.properties

import com.intellij.openapi.components.BaseState

class PropertySettings : BaseState() {
    var endWithNewline by property(true)
    var separateGroups by property(true)
    var indentValues by property(true)
    var spaceAroundSeparator by property(true)
    var forceSeparator by property(false)
    private var forcedSeparatorString by string("=")

    var forcedSeparator: Char
        get() = forcedSeparatorString?.get(0) ?: '='
        set(value) {
            forcedSeparatorString = value.toString()
        }
}
