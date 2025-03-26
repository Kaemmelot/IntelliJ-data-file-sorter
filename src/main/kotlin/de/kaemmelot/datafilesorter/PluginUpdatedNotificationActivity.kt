package de.kaemmelot.datafilesorter

import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

/** Activity that shows a balloon notification after the very first startup of a new plugin version */
class PluginUpdatedNotificationActivity : ProjectActivity {

    companion object {
        private const val LAST_USED_VERSION = PluginConstants.PLUGIN_ID + ".lastUsedVersion"
        private const val NOTIFICATION_GROUP = PluginConstants.PLUGIN_ID + ".pluginUpdate"
    }

    override suspend fun execute(project: Project) {
        val lastUsedVersion = PropertiesComponent.getInstance().getValue(LAST_USED_VERSION)
        // show only once
        if (PluginConstants.VERSION == lastUsedVersion) {
            return
        }

        PropertiesComponent.getInstance().setValue(LAST_USED_VERSION, PluginConstants.VERSION)

        NotificationGroupManager.getInstance().getNotificationGroup(NOTIFICATION_GROUP)
            .createNotification(
                DataFileSorterBundle.message("pluginUpdate.title"),
                DataFileSorterBundle.message("pluginUpdate.description", PluginConstants.VERSION),
                NotificationType.INFORMATION
            )
            .notify(project)
    }
}
