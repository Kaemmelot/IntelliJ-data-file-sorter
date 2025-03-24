package de.kaemmelot.datafilesorter.vcs

import com.intellij.openapi.options.UnnamedConfigurable
import com.intellij.openapi.vcs.ui.RefreshableOnComponent
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import de.kaemmelot.datafilesorter.DataFileSorterBundle
import javax.swing.JComponent

/**
 * Checkbox component for the VCS settings panel. Allows to toggle the option to check the sorting and formatting of data files.
 */
class VcsSettingsComponent(private val getter: () -> Boolean, private val setter: (Boolean) -> Unit) :
    RefreshableOnComponent, UnnamedConfigurable {

    private val filesSortedCheckBox = JBCheckBox(DataFileSorterBundle.message("vcs.checkFilesDescription"))

    // RefreshableOnComponent

    override fun saveState(): Unit = setter(filesSortedCheckBox.isSelected)

    override fun restoreState() {
        filesSortedCheckBox.isSelected = getter()
    }

    override fun getComponent(): JComponent =
        panel {
            row {
                cell(filesSortedCheckBox).also {
                    it.bindSelected(getter, setter)
                }
            }
        }

    // UnnamedConfigurable

    override fun createComponent(): JComponent = component

    override fun isModified(): Boolean = filesSortedCheckBox.isSelected != getter()

    override fun apply(): Unit = saveState()
}
