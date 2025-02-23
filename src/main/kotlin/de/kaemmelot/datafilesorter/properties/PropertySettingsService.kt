package de.kaemmelot.datafilesorter.properties

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
@State(
    name = "de.kaemmelot.datafilesorter.properties.PropertySettings",
    storages = [Storage(value = "DataFileSorter.xml", exportable = true)]
)
class PropertySettingsService :
    SimplePersistentStateComponent<PropertySettings>(PropertySettings()) {

    companion object {
        @JvmStatic
        fun getInstance(project: Project): PropertySettingsService = project.service<PropertySettingsService>()
    }

    var endWithNewline: Boolean
        get() = state.endWithNewline
        set(value) {
            state.endWithNewline = value
        }

    var separateGroups: Boolean
        get() = state.separateGroups
        set(value) {
            state.separateGroups = value
        }

    var indentValues: Boolean
        get() = state.indentValues
        set(value) {
            state.indentValues = value
        }

    var spaceAroundSeparator: Boolean
        get() = state.spaceAroundSeparator
        set(value) {
            state.spaceAroundSeparator = value
        }

    var forceSeparator: Boolean
        get() = state.forceSeparator
        set(value) {
            state.forceSeparator = value
        }

    var forcedSeparator: Char
        get() = state.forcedSeparator
        set(value) {
            state.forcedSeparator = value
        }
}
