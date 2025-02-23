package de.kaemmelot.datafilesorter.settings

import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.selected
import de.kaemmelot.datafilesorter.DataFileSorterBundle
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel

private const val COMMON_ROW_DESCRIPTION = ""

class SettingsComponent {
    val panel: JPanel
    private val endWithNewlineCheckBox: JCheckBox
    private val separateGroupsCheckBox: JCheckBox
    private val indentValuesCheckBox: JCheckBox
    private val spaceAroundSeparatorCheckBox: JCheckBox
    private val forceSeparatorCheckBox: JCheckBox
    private val forcedSeparatorComboBox: JComboBox<Char>

    var endWithNewline: Boolean
        get() = endWithNewlineCheckBox.isSelected
        set(value) {
            endWithNewlineCheckBox.isSelected = value
        }

    var separateGroups: Boolean
        get() = separateGroupsCheckBox.isSelected
        set(value) {
            separateGroupsCheckBox.isSelected = value
        }

    var indentValues: Boolean
        get() = indentValuesCheckBox.isSelected
        set(value) {
            indentValuesCheckBox.isSelected = value
        }

    var spaceAroundSeparator: Boolean
        get() = spaceAroundSeparatorCheckBox.isSelected
        set(value) {
            spaceAroundSeparatorCheckBox.isSelected = value
        }

    var forceSeparator: Boolean
        get() = forceSeparatorCheckBox.isSelected
        set(value) {
            forceSeparatorCheckBox.isSelected = value
        }

    var forcedSeparator: Char
        get() = forcedSeparatorComboBox.selectedItem as Char
        set(value) {
            forcedSeparatorComboBox.selectedItem = value
        }

    init {
        var endWithNewline: Cell<JCheckBox>? = null
        var separateGroups: Cell<JCheckBox>? = null
        var indentValues: Cell<JCheckBox>? = null
        var spaceAroundSeparator: Cell<JCheckBox>? = null
        var forceSeparator: Cell<JCheckBox>? = null
        var forcedSeparatorLabel: Cell<JLabel>? = null
        var forcedSeparator: Cell<JComboBox<Char>>? = null
        panel = panel {
            group(DataFileSorterBundle.message("dataFileSorter.settings.properties.name")) {
                row(COMMON_ROW_DESCRIPTION) {
                    endWithNewline = checkBox(DataFileSorterBundle.message("dataFileSorter.settings.properties.endWithNewline"))
                }
                row(COMMON_ROW_DESCRIPTION) {
                    separateGroups = checkBox(DataFileSorterBundle.message("dataFileSorter.settings.properties.separateGroups"))
                }
                row(COMMON_ROW_DESCRIPTION) {
                    indentValues = checkBox(DataFileSorterBundle.message("dataFileSorter.settings.properties.intendValues"))
                }
                row(COMMON_ROW_DESCRIPTION) {
                    spaceAroundSeparator =
                        checkBox(DataFileSorterBundle.message("dataFileSorter.settings.properties.spaceAroundSeparator"))
                }
                row(COMMON_ROW_DESCRIPTION) {
                    forceSeparator = checkBox(DataFileSorterBundle.message("dataFileSorter.settings.properties.forceSeparator"))
                }
                panel {
                    row(COMMON_ROW_DESCRIPTION) {
                        enabledIf(forceSeparator!!.component.selected)
                        forcedSeparatorLabel =
                            label(DataFileSorterBundle.message("dataFileSorter.settings.properties.forcedSeparator"))
                        forcedSeparator = comboBox(listOf('=', ':'))
                    }
                }
            }
        }
        endWithNewlineCheckBox = endWithNewline!!.component
        separateGroupsCheckBox = separateGroups!!.component
        indentValuesCheckBox = indentValues!!.component
        spaceAroundSeparatorCheckBox = spaceAroundSeparator!!.component
        forceSeparatorCheckBox = forceSeparator!!.component
        forcedSeparatorComboBox = forcedSeparator!!.component
        // start disabled
        forcedSeparatorComboBox.isEnabled = false
        forcedSeparatorLabel!!.component.isEnabled = false
    }
}
