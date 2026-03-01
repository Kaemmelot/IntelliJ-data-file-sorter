package de.kaemmelot.datafilesorter.properties

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.readAndEdtWriteAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.UndoConfirmationPolicy
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import de.kaemmelot.datafilesorter.DataFileSorterBundle
import de.kaemmelot.datafilesorter.PluginConstants
import de.kaemmelot.datafilesorter.properties.model.PropertiesFile

/**
 * Sort and format a single file with a regular read and write lock.
 */
suspend fun sortAndFormatFile(
    document: Document,
    project: Project,
) {
    readAndEdtWriteAction {
        val newContent: String = determineNewContent(document, project)

        writeAction {
            WriteCommandAction.writeCommandAction(project)
                .withName(DataFileSorterBundle.message("properties.sort.command"))
                .withGroupId(PluginConstants.PLUGIN_ID)
                .run<Throwable> { updateDocumentContent(newContent, document, project) }
        }
    }
}

/**
 * Sort and format a single file. Includes a workaround for the write lock, because that cannot be acquired in the VCS diff viewer.
 */
fun sortAndFormatFileForDiffViewer(
    document: Document,
    project: Project,
) {
    val newContent: String = determineNewContent(document, project)

    ApplicationManager.getApplication().runWriteAction {
        CommandProcessor.getInstance().executeCommand(
            project,
            { updateDocumentContent(newContent, document, project) },
            DataFileSorterBundle.message("properties.sort.command"),
            PluginConstants.PLUGIN_ID,
            UndoConfirmationPolicy.DO_NOT_REQUEST_CONFIRMATION
        )
    }
}

private fun determineNewContent(document: Document, project: Project): String {
    val propertiesFile: PropertiesFile =
        PropertiesFile.fromCharSequence(
            document.text,
            "\n", // documents always use unix line endings
            PropertySettingsService.getInstance(project).state
        )

    return propertiesFile.toSortedString()
}

private fun updateDocumentContent(newContent: String, document: Document, project: Project) {
    document.setText(newContent)
    PsiDocumentManager.getInstance(project).commitDocument(document)
}