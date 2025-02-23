package de.kaemmelot.datafilesorter.properties

/**
 * Single key/value property with an optional comment.
 */
class PropertyEntry(val key: String, val value: String, val comment: String?, val separator: Char) :
    Comparable<PropertyEntry> {

    override fun compareTo(other: PropertyEntry): Int {
        return key.compareTo(other.key)
    }
}
