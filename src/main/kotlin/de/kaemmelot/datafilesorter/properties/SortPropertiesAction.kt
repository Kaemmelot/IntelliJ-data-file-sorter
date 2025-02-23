package de.kaemmelot.datafilesorter.properties

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.readAndWriteAction
import com.intellij.openapi.command.UndoConfirmationPolicy
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import de.kaemmelot.datafilesorter.DataFileSorterBundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SortPropertiesAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project: Project = event.getData(CommonDataKeys.PROJECT) ?: return // abort if missing
        val editor: Editor = event.getData(CommonDataKeys.EDITOR) ?: return // abort if missing
        val lineSeparator: String = editor.virtualFile.detectedLineSeparator ?: return // abort if missing
        val fileContentSequence = editor.document.immutableCharSequence

        CoroutineScope(Dispatchers.Default).launch {
            readAndWriteAction {
                val propertiesFile: PropertiesFile =
                    PropertiesFile.fromCharSequence(
                        fileContentSequence,
                        lineSeparator,
                        PropertySettingsService.getInstance(project).state
                    )

                val newContent: String = propertiesFile.toSortedString()

                writeAction {
                    WriteCommandAction.writeCommandAction(project)
                        .withName(DataFileSorterBundle.message("dataFileSorter.properties.sort.command"))
                        .withGroupId("de.kaemmelot.datafilesorter")
                        .withUndoConfirmationPolicy(UndoConfirmationPolicy.REQUEST_CONFIRMATION)
                        .compute<Unit, Throwable> { editor.document.setText(newContent) }
                }
            }
        }
    }

    override fun update(event: AnActionEvent) {
        val project: Project? = event.getData(CommonDataKeys.PROJECT)
        val editor: Editor? = event.getData(CommonDataKeys.EDITOR)
        // visible if project and editor are accessible and the file is a properties file
        val visible = project != null && editor != null && editor.virtualFile.extension == "properties"
        // enabled if there is a detected line separator and the file can be modified
        val enabled = visible && editor!!.virtualFile.detectedLineSeparator != null && editor.document.isWritable

        event.presentation.isVisible = visible
        event.presentation.isEnabled = enabled
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    // no need for indexing
    override fun isDumbAware(): Boolean = true
}