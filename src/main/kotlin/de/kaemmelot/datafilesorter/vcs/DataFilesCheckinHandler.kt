package de.kaemmelot.datafilesorter.vcs

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.application.readAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.checkin.CheckinHandler
import com.intellij.openapi.vcs.checkin.CommitCheck
import com.intellij.openapi.vcs.checkin.CommitInfo
import com.intellij.openapi.vcs.checkin.CommitProblem
import com.intellij.openapi.vcs.checkin.TextCommitProblem
import com.intellij.openapi.vcs.checkin.committedVirtualFiles
import com.intellij.openapi.vcs.ui.RefreshableOnComponent
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.isFile
import com.intellij.openapi.vfs.readText
import com.intellij.platform.util.progress.reportSequentialProgress
import de.kaemmelot.datafilesorter.DataFileSorterBundle
import de.kaemmelot.datafilesorter.PluginConstants
import de.kaemmelot.datafilesorter.PluginConstants.PROPERTIES_EXTENSION
import de.kaemmelot.datafilesorter.properties.PropertiesFile
import de.kaemmelot.datafilesorter.properties.PropertySettings
import de.kaemmelot.datafilesorter.properties.PropertySettingsService
import de.kaemmelot.datafilesorter.properties.SortPropertiesAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * VCS checkin handler to check sorting and formatting of data files.
 */
class DataFilesCheckinHandler(private val panel: CheckinProjectPanel) : CheckinHandler(), CommitCheck, DumbAware {

    companion object {
        private const val FILES_SORTED_CHECK_PROPERTY = PluginConstants.PLUGIN_ID + ".vcs.checkFiles"
    }

    private val log = logger<DataFilesCheckinHandler>()

    // This setting is saved as part of the workspace.xml
    private fun loadOrDefault(): Boolean =
        PropertiesComponent.getInstance(panel.project).getBoolean(FILES_SORTED_CHECK_PROPERTY, false)

    private fun save(value: Boolean) {
        val propertiesComponent = PropertiesComponent.getInstance(panel.project)
        if (value) {
            propertiesComponent.setValue(FILES_SORTED_CHECK_PROPERTY, true)
        } else {
            propertiesComponent.unsetValue(FILES_SORTED_CHECK_PROPERTY)
        }
    }

    override fun getBeforeCheckinConfigurationPanel(): RefreshableOnComponent {
        return VcsSettingsComponent(this::loadOrDefault, this::save)
    }

    override fun getExecutionOrder(): CommitCheck.ExecutionOrder = CommitCheck.ExecutionOrder.MODIFICATION

    override fun isEnabled(): Boolean = loadOrDefault()

    override suspend fun runCheck(commitInfo: CommitInfo): CommitProblem? {
        val filesToCheck: List<VirtualFile> =
            commitInfo.committedVirtualFiles.filter {
                it.isFile
                        && it.extension == PROPERTIES_EXTENSION
                        && it.isWritable
            }

        val invalidFiles: List<VirtualFile>
        try {
            invalidFiles = determineUnsortedOrUnformattedFiles(filesToCheck)
        } catch (missingLineSeparatorException: MissingLineSeparatorException) {
            return TextCommitProblem(
                DataFileSorterBundle.message(
                    "vcs.missingLineSeparator",
                    missingLineSeparatorException.file.presentableName
                )
            )
        }

        if (invalidFiles.isEmpty()) {
            return null
        }

        val dialog = SortAndFormatFilesChoiceDialog(panel.project, invalidFiles)
        dialog.show()

        return when (dialog.exitCode) {
            DialogWrapper.OK_EXIT_CODE -> {
                CoroutineScope(Dispatchers.Default).launch {
                    for (file in dialog.selectedFiles) {
                        SortPropertiesAction.sortAndFormatFile(file, panel.project)
                    }
                }
                return null
            }
            // skip all files
            SortAndFormatFilesChoiceDialog.SKIP_EXIT_CODE -> null
            // all forms of abort
            else -> TextCommitProblem(
                DataFileSorterBundle.message(
                    "vcs.choiceDialog.abortMessage",
                    invalidFiles.size
                )
            )
        }
    }

    private suspend fun determineUnsortedOrUnformattedFiles(filesToCheck: List<VirtualFile>): List<VirtualFile> {
        val unsortedFiles: MutableList<VirtualFile> = ArrayList(filesToCheck.size)

        withContext(Dispatchers.Default) {
            val settings = PropertySettingsService.getInstance(panel.project).state

            reportSequentialProgress(filesToCheck.size) { reporter ->
                for (file in filesToCheck) {
                    if (file.detectedLineSeparator == null) {
                        throw MissingLineSeparatorException(file)
                    } else if (!file.isWritable) {
                        log.error("Unexpected read-only file ${file.name}. Skipping this file.")
                        reporter.itemStep()
                        continue
                    }

                    reporter.itemStep(
                        DataFileSorterBundle.message("vcs.checkFilesProgress", file.name)
                    ) {
                        if (isFileNotSortedAndFormatted(file, settings)) {
                            unsortedFiles.add(file)
                        }
                    }
                }
            }
        }
        return unsortedFiles
    }

    private suspend fun isFileNotSortedAndFormatted(
        file: VirtualFile,
        settings: PropertySettings,
    ): Boolean {
        var actualFileContent = ""
        var expectedFileContent = ""
        readAction {
            actualFileContent = file.readText()
            val propertiesFile: PropertiesFile =
                PropertiesFile.fromCharSequence(
                    actualFileContent,
                    file.detectedLineSeparator!!,
                    settings
                )

            expectedFileContent = propertiesFile.toSortedString()
        }
        return actualFileContent != expectedFileContent
    }

}
