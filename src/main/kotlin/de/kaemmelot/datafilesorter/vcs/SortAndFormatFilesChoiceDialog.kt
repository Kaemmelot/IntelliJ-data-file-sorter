package de.kaemmelot.datafilesorter.vcs

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.table.JBTable
import de.kaemmelot.datafilesorter.DataFileSorterBundle
import java.awt.event.ActionEvent
import javax.swing.Action
import javax.swing.JComponent
import javax.swing.JTable.AUTO_RESIZE_LAST_COLUMN

/**
 * Dialog to let the user choose what to do with unsorted or unformatted files.
 */
class SortAndFormatFilesChoiceDialog(project: Project, files: List<VirtualFile>) : DialogWrapper(project) {

    companion object {
        const val SKIP_EXIT_CODE = NEXT_USER_EXIT_CODE
    }

    private var skipAction: SkipAction? = null

    private val tableModel = FileSelectionTableModel(files)

    val selectedFiles: Set<VirtualFile>
        get() = tableModel.selectedFiles

    init {
        title = DataFileSorterBundle.message("vcs.choiceDialog.title")
        init()
    }

    override fun createActions(): Array<Action> {
        return arrayOf(okAction, skipAction!!, cancelAction)
    }

    override fun createDefaultActions() {
        super.createDefaultActions()
        skipAction = SkipAction()
        setOKButtonText(DataFileSorterBundle.message("vcs.choiceDialog.okAction"))
    }

    override fun createCenterPanel(): JComponent {
        val table = JBTable(tableModel)
        table.tableHeader.reorderingAllowed = false
        table.isStriped = true
        table.autoResizeMode = AUTO_RESIZE_LAST_COLUMN

        for (columnIndex in 0..<tableModel.columnCount) {
            tableModel.setColumnWidths(table.columnModel.getColumn(columnIndex), columnIndex)
        }

        return panel {
            row {
                label(DataFileSorterBundle.message("vcs.choiceDialog.description"))
            }
            row {
                scrollCell(table).align(Align.FILL).focused()
            }.resizableRow()
        }
    }

    private fun skipAction() {
        close(SKIP_EXIT_CODE)
    }

    private inner class SkipAction :
        DialogWrapper.DialogWrapperAction(DataFileSorterBundle.message("vcs.choiceDialog.skipAction")) {

        init {
            putValue(MAC_ACTION_ORDER, DEFAULT_ACTION_ORDER - 10)
        }

        override fun doAction(e: ActionEvent) {
            skipAction()
        }
    }
}
