package de.kaemmelot.datafilesorter.properties

import com.intellij.openapi.components.SerializablePersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import de.kaemmelot.datafilesorter.PluginConstants

@Service(Service.Level.PROJECT)
@State(
    name = PluginConstants.PLUGIN_ID + ".properties.PropertySettings",
    storages = [Storage(value = PluginConstants.PLUGIN_PROJECT_SETTINGS_FILE, exportable = true)]
)
class PropertySettingsService :
    SerializablePersistentStateComponent<PropertySettings>(PropertySettings()) {

    companion object {
        @JvmStatic
        fun getInstance(project: Project): PropertySettingsService = project.service<PropertySettingsService>()
    }

    var endWithNewline: Boolean
        get() = state.endWithNewline
        set(value) {
            updateState {
                it.copy(endWithNewline = value)
            }
        }

    var separateGroups: Boolean
        get() = state.separateGroups
        set(value) {
            updateState {
                it.copy(separateGroups = value)
            }
        }

    var indentValues: Boolean
        get() = state.indentValues
        set(value) {
            updateState {
                it.copy(indentValues = value)
            }
        }

    var spaceAroundSeparator: Boolean
        get() = state.spaceAroundSeparator
        set(value) {
            updateState {
                it.copy(spaceAroundSeparator = value)
            }
        }

    var forceSeparator: Boolean
        get() = state.forceSeparator
        set(value) {
            updateState {
                it.copy(forceSeparator = value)
            }
        }

    var forcedSeparator: Char
        get() = state.forcedSeparator
        set(value) {
            updateState {
                it.copy(forcedSeparator = value)
            }
        }
}
