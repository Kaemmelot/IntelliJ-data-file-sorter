package de.kaemmelot.datafilesorter.properties

import java.util.*
import java.util.regex.Pattern

private val preferredSeparatorPattern = Pattern.compile("[=:]")
private val whiteSpacePattern = Pattern.compile(" ")

class PropertiesFile internal constructor(
    internal val groups: SortedMap<String, PropertyGroup>,
    internal val lineSeparator: String,
    internal val settings: PropertySettings?,
    internal val trailingComment: String?
) {

    companion object {
        /**
         * Read the sequence that is supposed to be a properties file with the expected line separator.
         * @return new instance of a [PropertiesFile]
         */
        @JvmStatic
        fun fromCharSequence(
            propertyLines: CharSequence,
            lineSeparator: String,
            settings: PropertySettings? = null
        ): PropertiesFile {
            val groups = TreeMap<String, PropertyGroup>()
            var trailingComment: String? = null
            // reduce memory/time consumption by avoiding a copy of the file
            val lineIterator: Iterator<String> = propertyLines.splitToSequence(lineSeparator).iterator()

            while (lineIterator.hasNext()) {
                val line: String = lineIterator.next()
                if (line.isBlank()) {
                    continue
                }

                var comment: String? = null
                var trimmedLine = line.trimStart()
                if (trimmedLine.startsWith('#')) {
                    // comment for the next key value pair
                    val (readComment, nextLine) = readUntilCommentEnds(lineIterator, lineSeparator)
                    comment = (trimmedLine + readComment).trimEnd()
                    if (nextLine == null) {
                        trailingComment = comment
                        break
                    }
                    trimmedLine = nextLine
                }

                // find key, value (or the first line of the value) and separator
                var keyValue: Array<String> = preferredSeparatorPattern.split(trimmedLine, 2)
                var key: String = keyValue.first().trim()
                var separator: Char
                if (key.contains(' ')) {
                    // fallback to whitespace separator
                    keyValue = whiteSpacePattern.split(trimmedLine, 2)
                    key = keyValue.first().trimEnd()
                    separator = ' '
                } else {
                    val untrimmedKeyLength = keyValue.first().length
                    separator = trimmedLine.subSequence(untrimmedKeyLength, untrimmedKeyLength + 1)[0]
                }
                var value: String = keyValue[1].trimStart()

                // get all remaining lines of the value
                value += readUntilLineEnds(line, lineIterator, lineSeparator)

                val groupName = key.substringBefore('.')
                val propertyGroup = groups.computeIfAbsent(groupName) { PropertyGroup(groupName) }
                propertyGroup.addProperty(PropertyEntry(key, value, comment, separator))
            }

            return PropertiesFile(groups, lineSeparator, settings, trailingComment)
        }

        /**
         * Read until the end of a comment. The comment will end with the next line that is not empty and does not start
         * with a # symbol. The end of the comment is trimmed.
         * @return comment and next line (if exists) after comment, trimmed at the start
         */
        private fun readUntilCommentEnds(
            lineIterator: Iterator<String>,
            lineSeparator: String
        ): Pair<String, String?> {
            var line: String
            var value = ""
            while (lineIterator.hasNext()) {
                line = lineIterator.next()
                val trimmedLine = line.trimStart()
                // keep empty lines in comments
                if (trimmedLine.isNotEmpty() && !trimmedLine.startsWith('#')) {
                    // comment ended
                    return Pair(value, trimmedLine)
                }
                value += lineSeparator + trimmedLine
            }
            return Pair(value, null)
        }

        /**
         * Read until a line ends without a \ symbol.
         */
        private fun readUntilLineEnds(
            firstLine: String,
            lineIterator: Iterator<String>,
            lineSeparator: String
        ): String {
            var line = firstLine
            var value = ""
            while (doesLineContinue(line) && lineIterator.hasNext()) {
                line = lineIterator.next()
                value += lineSeparator + line
            }
            return value
        }

        /**
         * Check if the line ends with a \ symbol. Needs to be the last symbol, whitespace is not allowed.
         */
        private fun doesLineContinue(line: String): Boolean {
            if (!line.endsWith('\\')) {
                return false
            }
            var backslashCount = 0
            for (i in line.length - 1 downTo 0) {
                if (line[i] != '\\') {
                    break
                }
                backslashCount++
            }
            return backslashCount % 2 == 1
        }
    }

    /**
     * Combine all property entries back into a single string in the order of the given collection.
     */
    fun toSortedString(): String {
        val lines = ArrayList<String>()

        val separateGroups = settings?.separateGroups != false
        val indentValues = settings?.indentValues != false
        for (groupEntry in groups) {
            val maxKeyLength: Int? = if (indentValues) {
                groupEntry.value.properties.keys.maxOf { it.length }
            } else {
                null
            }

            for (property in groupEntry.value.properties.values) {
                if (property.comment != null) {
                    lines.add(property.comment)
                }

                val separator: Char = determineSeparator(settings, property)
                val spaceBeforeSep: String = determineSpaceBeforeSeparator(separator, settings)
                val spaceAfterSep: String = determineSpaceAfterSeparator(property, separator, settings)
                val indent: String = if (maxKeyLength != null) {
                    " ".repeat(maxKeyLength - property.key.length)
                } else {
                    ""
                }

                lines.add(property.key + indent + spaceBeforeSep + separator + spaceAfterSep + property.value)
            }

            if (separateGroups) {
                lines.add("")
            }
        }

        if (trailingComment != null) {
            lines.add(trailingComment)
            if (separateGroups) {
                lines.add("")
            }
        }

        val endWithNewline = settings?.endWithNewline != false
        if (separateGroups && !endWithNewline) {
            // one empty line to much
            lines.removeLast()
        } else if (!separateGroups && endWithNewline) {
            // add new line at the end
            lines.add("")
        }

        return lines.joinToString(lineSeparator)
    }

    private fun determineSeparator(
        settings: PropertySettings?,
        property: PropertyEntry
    ) = if (settings?.forceSeparator == true) {
        settings.forcedSeparator
    } else {
        property.separator
    }

    private fun determineSpaceBeforeSeparator(separator: Char, settings: PropertySettings?) =
        if (settings?.spaceAroundSeparator != false && separator != ' ') {
            " "
        } else {
            ""
        }

    private fun determineSpaceAfterSeparator(property: PropertyEntry, separator: Char, settings: PropertySettings?) =
        if (settings?.spaceAroundSeparator != false && separator != ' ' && property.value.isNotBlank()) {
            " "
        } else {
            ""
        }
}
