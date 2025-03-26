package de.kaemmelot.datafilesorter

import com.intellij.ide.plugins.PluginManagerCore.getPlugin
import com.intellij.openapi.extensions.PluginId

object PluginConstants {

    const val PLUGIN_ID = "de.kaemmelot.datafilesorter"
    val INTELLIJ_PLUGIN_ID = PluginId.getId(PLUGIN_ID)
    /** File name for all kind of settings for this plugin */
    const val PLUGIN_SETTINGS_FILE = "DataFileSorter.xml"

    /** File extension for properties files */
    const val PROPERTIES_EXTENSION = "properties"

    val VERSION: String

    init {
        val plugin = getPlugin(INTELLIJ_PLUGIN_ID)

        VERSION = plugin?.version ?: "{error}"
    }
}
