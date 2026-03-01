package de.kaemmelot.datafilesorter.properties

import com.intellij.diff.tools.util.DiffDataKeys
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.isFile
import de.kaemmelot.datafilesorter.PluginConstants.PROPERTIES_EXTENSION
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * IntelliJ action to sort a ".properties" file.
 */
class SortPropertiesAction : AnAction(), DumbAware {

    override fun actionPerformed(event: AnActionEvent) {
        val project: Project = event.getData(CommonDataKeys.PROJECT) ?: return // abort if missing
        val file: VirtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE) ?: return // abort if missing
        val document: Document = FileDocumentManager.getInstance().getDocument(file) ?: return // abort if not loadable

        val isInMergeDiffViewer = event.getData(DiffDataKeys.MERGE_VIEWER) != null

        if (!isInMergeDiffViewer) {
            CoroutineScope(Dispatchers.Default).launch {
                sortAndFormatFile(document, project)
            }
        } else {
            sortAndFormatFileForDiffViewer(document, project)
        }
    }

    override fun update(event: AnActionEvent) {
        val project: Project? = event.getData(CommonDataKeys.PROJECT)
        val file: VirtualFile? = event.getData(CommonDataKeys.VIRTUAL_FILE)
        val document: Document? = file?.let { FileDocumentManager.getInstance().getDocument(it) }

        // visible if project exists and the file is a properties file
        val visible = project != null && file != null && file.isFile && file.extension == PROPERTIES_EXTENSION
        // enabled if the document is writable
        val enabled = visible && document != null && document.isWritable

        event.presentation.isVisible = visible
        event.presentation.isEnabled = enabled
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}