package de.kaemmelot.datafilesorter.properties

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.readAndWriteAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.isFile
import com.intellij.openapi.vfs.readText
import com.intellij.openapi.vfs.writeText
import de.kaemmelot.datafilesorter.DataFileSorterBundle
import de.kaemmelot.datafilesorter.PluginConstants.PLUGIN_ID
import de.kaemmelot.datafilesorter.PluginConstants.PROPERTIES_EXTENSION
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SortPropertiesAction : AnAction(), DumbAware {
    companion object {
        suspend fun sortAndFormatFile(
            file: VirtualFile,
            project: Project,
        ) {
            readAndWriteAction {
                val propertiesFile: PropertiesFile =
                    PropertiesFile.fromCharSequence(
                        file.readText(),
                        file.detectedLineSeparator!!,
                        PropertySettingsService.getInstance(project).state
                    )

                val newContent: String = propertiesFile.toSortedString()

                writeAction {
                    WriteCommandAction.writeCommandAction(project)
                        .withName(DataFileSorterBundle.message("properties.sort.command"))
                        .withGroupId(PLUGIN_ID)
                        .compute<Unit, Throwable> { file.writeText(newContent) }
                }
            }
        }
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project: Project = event.getData(CommonDataKeys.PROJECT) ?: return // abort if missing
        val file: VirtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE) ?: return // abort if missing
        file.detectedLineSeparator ?: return // abort if missing

        CoroutineScope(Dispatchers.Default).launch {
            sortAndFormatFile(file, project)
        }
    }

    override fun update(event: AnActionEvent) {
        val project: Project? = event.getData(CommonDataKeys.PROJECT)
        val file: VirtualFile? = event.getData(CommonDataKeys.VIRTUAL_FILE)
        // visible if project exists and the file is a properties file
        val visible = project != null && file != null && file.isFile && file.extension == PROPERTIES_EXTENSION
        // enabled if there is a detected line separator and the file can be modified
        val enabled = visible && file!!.detectedLineSeparator != null && file.isWritable

        event.presentation.isVisible = visible
        event.presentation.isEnabled = enabled
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}