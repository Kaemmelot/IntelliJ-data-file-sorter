package de.kaemmelot.datafilesorter.settings

import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import de.kaemmelot.datafilesorter.DataFileSorterBundle
import de.kaemmelot.datafilesorter.properties.PropertySettingsService
import javax.swing.JComponent

class DataFileSorterConfigurable(private val project: Project) : SearchableConfigurable {
    private var settingsComponent: SettingsComponent? = null

    override fun createComponent(): JComponent {
        settingsComponent = SettingsComponent()
        return settingsComponent!!.panel
    }

    override fun reset() {
        super.reset()
        val propertySettingsService = PropertySettingsService.getInstance(project)
        settingsComponent!!.endWithNewline = propertySettingsService.endWithNewline
        settingsComponent!!.separateGroups = propertySettingsService.separateGroups
        settingsComponent!!.indentValues = propertySettingsService.indentValues
        settingsComponent!!.spaceAroundSeparator = propertySettingsService.spaceAroundSeparator
        settingsComponent!!.forceSeparator = propertySettingsService.forceSeparator
        settingsComponent!!.forcedSeparator = propertySettingsService.forcedSeparator
    }

    override fun isModified(): Boolean {
        val propertySettingsService = PropertySettingsService.getInstance(project)
        return propertySettingsService.endWithNewline != settingsComponent!!.endWithNewline
                || propertySettingsService.separateGroups != settingsComponent!!.separateGroups
                || propertySettingsService.indentValues != settingsComponent!!.indentValues
                || propertySettingsService.spaceAroundSeparator != settingsComponent!!.spaceAroundSeparator
                || propertySettingsService.forceSeparator != settingsComponent!!.forceSeparator
                || propertySettingsService.forcedSeparator != settingsComponent!!.forcedSeparator
    }

    override fun apply() {
        val propertySettingsService = PropertySettingsService.getInstance(project)
        propertySettingsService.endWithNewline = settingsComponent!!.endWithNewline
        propertySettingsService.separateGroups = settingsComponent!!.separateGroups
        propertySettingsService.indentValues = settingsComponent!!.indentValues
        propertySettingsService.spaceAroundSeparator = settingsComponent!!.spaceAroundSeparator
        propertySettingsService.forceSeparator = settingsComponent!!.forceSeparator
        propertySettingsService.forcedSeparator = settingsComponent!!.forcedSeparator
    }

    override fun getDisplayName(): String = DataFileSorterBundle.message("settings.name")

    override fun getId(): String = "de.kaemmelot.datafilesorter.settings.DataFileSorterConfigurable"

}