package de.kaemmelot.datafilesorter.vcs

import com.intellij.openapi.vfs.VirtualFile
import de.kaemmelot.datafilesorter.DataFileSorterBundle
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.table.AbstractTableModel
import javax.swing.table.TableColumn

class FileSelectionTableModel(private val files: List<VirtualFile>) : AbstractTableModel() {

    private val internalFileSelection: MutableSet<VirtualFile> = files.toMutableSet()

    val selectedFiles: Set<VirtualFile> get() = internalFileSelection

    override fun getRowCount(): Int = files.size

    override fun getColumnCount(): Int = 2

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        if (rowIndex >= files.size || rowIndex < 0) {
            throw IndexOutOfBoundsException(rowIndex)
        }
        return when (columnIndex) {
            0 -> internalFileSelection.contains(files[rowIndex])
            1 -> files[rowIndex].name
            else -> throw IndexOutOfBoundsException(columnIndex)
        }
    }

    override fun getColumnName(columnIndex: Int): String {
        return when (columnIndex) {
            0 -> ""
            1 -> DataFileSorterBundle.message("vcs.choiceDialog.table.fileNameColumn")
            else -> throw IndexOutOfBoundsException(columnIndex)
        }
    }

    override fun getColumnClass(columnIndex: Int): Class<*> {
        return when (columnIndex) {
            0 -> Boolean::class.javaObjectType
            1 -> String::class.javaObjectType
            else -> throw IndexOutOfBoundsException(columnIndex)
        }
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean = columnIndex == 0

    override fun setValueAt(selected: Any?, rowIndex: Int, columnIndex: Int) {
        if (rowIndex < 0 || rowIndex >= files.size) {
            throw IndexOutOfBoundsException(rowIndex)
        } else if (columnIndex != 0) {
            throw IndexOutOfBoundsException(columnIndex)
        } else if (selected == null) {
            throw IllegalArgumentException("Value is null")
        } else if (selected !is Boolean) {
            throw IllegalArgumentException("Value is of unexpected type ${selected::class.java}")
        }

        if (selected) {
            internalFileSelection.add(files[rowIndex])
        } else {
            internalFileSelection.remove(files[rowIndex])
        }
    }

    fun setColumnWidths(column: TableColumn, columnIndex: Int) {
        if (columnIndex == 0) {
            val size = JCheckBox().preferredSize.width
            column.width = size
            column.preferredWidth = size
            column.minWidth = size
            column.maxWidth = size
        } else {
            var preferredWidth = JLabel(getColumnName(columnIndex)).preferredSize.width
            for (rowIndex in 0..<rowCount) {
                val widthInRow = JLabel(getValueAt(rowIndex, columnIndex) as String).preferredSize.width
                preferredWidth = maxOf(widthInRow, preferredWidth)
            }
            // respect cell padding
            // TODO how to determine cell padding dynamically?
            preferredWidth += 20
            column.width = maxOf(200, preferredWidth)
            column.preferredWidth = maxOf(200, preferredWidth)
            column.minWidth = maxOf(200, preferredWidth)
        }
    }
}
