package de.kaemmelot.datafilesorter.properties

import kotlinx.collections.immutable.toImmutableMap
import java.util.*

/**
 * A group of properties that all start with the same first key substring.
 */
class PropertyGroup(val groupKey: String) : Comparable<PropertyGroup> {

    private val _properties: SortedMap<String, PropertyEntry> = TreeMap()

    /**
     * Properties identified and sorted by their key.
     */
    val properties: Map<String, PropertyEntry>
        get() = _properties.toImmutableMap()

    fun addProperty(property: PropertyEntry) {
        _properties[property.key] = property
    }

    override fun compareTo(other: PropertyGroup): Int {
        return groupKey.compareTo(other.groupKey)
    }
}
