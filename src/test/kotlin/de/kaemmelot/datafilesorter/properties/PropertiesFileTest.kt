package de.kaemmelot.datafilesorter.properties

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

private const val LINE_SEPARATOR = "\n"

class PropertiesFileTest {

    @Test
    fun shouldSortPropertiesWithEquals() {
        val propertiesFile = PropertiesFile.fromCharSequence(
            """
                key4.anotherkey1=value4
            key3   =  value:3
                key2.subkey5.2 =value=2
            key1.subkey2= value1
            key5  = value5
            """.trimIndent(), LINE_SEPARATOR
        )
        assertEquals(listOf("key1", "key2", "key3", "key4", "key5"), propertiesFile.groups.map { it.key })
        assertEquals(propertiesFile.groups.map { it.key }, propertiesFile.groups.map { it.value.groupKey })
        assertEquals(
            listOf("key1.subkey2", "key2.subkey5.2", "key3", "key4.anotherkey1", "key5"),
            propertiesFile.groups.flatMap { it.value.properties.keys })
        assertEquals(
            propertiesFile.groups.flatMap { it.value.properties.keys },
            propertiesFile.groups.flatMap { it.value.properties.values }.map { it.key })
        assertEquals(
            listOf("value1", "value=2", "value:3", "value4", "value5"),
            propertiesFile.groups.flatMap { it.value.properties.values }.map { it.value },
        )
        assertEquals(5, propertiesFile.groups.flatMap { it.value.properties.values }.count { it.separator == '=' })
    }

    @Test
    fun shouldSortPropertiesWithColon() {
        val propertiesFile = PropertiesFile.fromCharSequence(
            """
                key4.anotherkey1:value4
            key3   :  value:3
                key2.subkey5.2 :value=2
            key1.subkey2: value1
            key5  : value5
            """.trimIndent(), LINE_SEPARATOR
        )
        assertEquals(listOf("key1", "key2", "key3", "key4", "key5"), propertiesFile.groups.map { it.key })
        assertEquals(propertiesFile.groups.map { it.key }, propertiesFile.groups.map { it.value.groupKey })
        assertEquals(
            listOf("key1.subkey2", "key2.subkey5.2", "key3", "key4.anotherkey1", "key5"),
            propertiesFile.groups.flatMap { it.value.properties.keys })
        assertEquals(
            propertiesFile.groups.flatMap { it.value.properties.keys },
            propertiesFile.groups.flatMap { it.value.properties.values }.map { it.key })
        assertEquals(
            listOf("value1", "value=2", "value:3", "value4", "value5"),
            propertiesFile.groups.flatMap { it.value.properties.values }.map { it.value },
        )
        assertEquals(5, propertiesFile.groups.flatMap { it.value.properties.values }.count { it.separator == ':' })
    }

    @Test
    fun shouldSortPropertiesWithWhitespace() {
        val propertiesFile = PropertiesFile.fromCharSequence(
            """
                key4.anotherkey1 value4
            key3      value:3
                key2.subkey5.2  value=2
            key1.subkey2 value1
            key5    value:5
            """.trimIndent(), LINE_SEPARATOR
        )
        assertEquals(listOf("key1", "key2", "key3", "key4", "key5"), propertiesFile.groups.map { it.key })
        assertEquals(propertiesFile.groups.map { it.key }, propertiesFile.groups.map { it.value.groupKey })
        assertEquals(
            listOf("key1.subkey2", "key2.subkey5.2", "key3", "key4.anotherkey1", "key5"),
            propertiesFile.groups.flatMap { it.value.properties.keys })
        assertEquals(
            propertiesFile.groups.flatMap { it.value.properties.keys },
            propertiesFile.groups.flatMap { it.value.properties.values }.map { it.key })
        assertEquals(
            listOf("value1", "value=2", "value:3", "value4", "value:5"),
            propertiesFile.groups.flatMap { it.value.properties.values }.map { it.value },
        )
        assertEquals(5, propertiesFile.groups.flatMap { it.value.properties.values }.count { it.separator == ' ' })
    }

    @Test
    fun shouldRecognizeMultilineValues() {
        val propertiesFile = PropertiesFile.fromCharSequence(
            """
            key3.subkey = value:3\
            value3 line2\\\
            value3 line3
            key2      value=2\\
            key1= value1\\\\\
            value1 line2
            """.trimIndent(), LINE_SEPARATOR
        )
        assertEquals(listOf("key1", "key2", "key3"), propertiesFile.groups.map { it.key })
        assertEquals(propertiesFile.groups.map { it.key }, propertiesFile.groups.map { it.value.groupKey })
        assertEquals(listOf("key1", "key2", "key3.subkey"), propertiesFile.groups.flatMap { it.value.properties.keys })
        assertEquals(
            propertiesFile.groups.flatMap { it.value.properties.keys },
            propertiesFile.groups.flatMap { it.value.properties.values }.map { it.key })
        assertEquals(
            listOf(
                "value1\\\\\\\\\\\nvalue1 line2",
                "value=2\\\\",
                "value:3\\\nvalue3 line2\\\\\\\nvalue3 line3"
            ),
            propertiesFile.groups.flatMap { it.value.properties.values }.map { it.value },
        )
        assertEquals(
            listOf('=', ' ', '='),
            propertiesFile.groups.flatMap { it.value.properties.values }.map { it.separator })
    }

    @Test
    fun shouldRecognizeComments() {
        val propertiesFile = PropertiesFile.fromCharSequence(
            """
                # Comment1 key3
                
                # Comment2 key3
                
            key3.subkey = value3
            key2 = value2
            # Comment key1
            key1 = value1
            """.trimIndent(), LINE_SEPARATOR
        )

        assertEquals(listOf("key1", "key2", "key3"), propertiesFile.groups.map { it.key })
        assertEquals(propertiesFile.groups.map { it.key }, propertiesFile.groups.map { it.value.groupKey })
        assertEquals(listOf("key1", "key2", "key3.subkey"), propertiesFile.groups.flatMap { it.value.properties.keys })
        assertEquals(
            propertiesFile.groups.flatMap { it.value.properties.keys },
            propertiesFile.groups.flatMap { it.value.properties.values }.map { it.key })
        assertEquals(
            listOf("# Comment key1", null, "# Comment1 key3\n\n# Comment2 key3"),
            propertiesFile.groups.flatMap { it.value.properties.values }.map { it.comment })
        assertEquals(
            listOf(
                "value1", "value2", "value3"
            ),
            propertiesFile.groups.flatMap { it.value.properties.values }.map { it.value },
        )
        assertEquals(3, propertiesFile.groups.flatMap { it.value.properties.values }.count { it.separator == '=' })
    }

    @Test
    fun shouldIgnoreMisleadingSeparators() {
        val propertiesFile = PropertiesFile.fromCharSequence(
            """
            # = HEADER\
    
            key1 = value1 = value2\
            value3 \
            value4
            """.trimIndent(), LINE_SEPARATOR
        )


        assertEquals(listOf("key1"), propertiesFile.groups.map { it.key })
        assertEquals(propertiesFile.groups.map { it.key }, propertiesFile.groups.map { it.value.groupKey })
        assertEquals(listOf("key1"), propertiesFile.groups.flatMap { it.value.properties.keys })
        assertEquals(
            propertiesFile.groups.flatMap { it.value.properties.keys },
            propertiesFile.groups.flatMap { it.value.properties.values }.map { it.key })
        assertEquals(listOf("key1"), propertiesFile.groups.map { it.key })
        assertEquals(
            listOf("# = HEADER\\"),
            propertiesFile.groups.flatMap { it.value.properties.values }.map { it.comment })
        assertEquals(
            listOf(
                "value1 = value2\\\nvalue3 \\\nvalue4"
            ),
            propertiesFile.groups.flatMap { it.value.properties.values }.map { it.value },
        )
        assertEquals(1, propertiesFile.groups.flatMap { it.value.properties.values }.count { it.separator == '=' })
    }

    @Test
    fun shouldCollectTrailingComment() {
        val propertiesFile = PropertiesFile.fromCharSequence(
            """
            # Last comment
            
            """.trimIndent(), LINE_SEPARATOR
        )
        assertEquals(0, propertiesFile.groups.size)
        assertEquals("# Last comment", propertiesFile.trailingComment)
    }

    @Test
    fun shouldSortCaseSensitive() {
        val propertiesFile = PropertiesFile.fromCharSequence(
            """
            key = value1
            Key = value2
            key.key = value3
            key.Key = value4
            """.trimIndent(), LINE_SEPARATOR
        )

        assertEquals(listOf("Key", "key"), propertiesFile.groups.map { it.key })
        assertEquals(listOf("key", "key.Key", "key.key"), propertiesFile.groups["key"]?.properties?.map { it.key })
    }

    @Test
    fun shouldMergeProperties() {
        val propertiesFile: PropertiesFile = artificialTestFile(null)

        assertEquals(
            """
            key1.someLongerKey = some value
            # Comment 1
            key1.subkey1       = value1\
            value1.1
            
            key2 :
            
            # Whitespace
            # and long comment
            key3.anothersubkey value3
            
        """.trimIndent(),
            propertiesFile.toSortedString()
        )
    }

    @Test
    fun shouldMergePropertiesWithForcedSeparator() {
        val settings = PropertySettings()
        settings.forceSeparator = true
        settings.forcedSeparator = '='

        val propertiesFile: PropertiesFile = artificialTestFile(settings)

        assertEquals(
            """
            key1.someLongerKey = some value
            # Comment 1
            key1.subkey1       = value1\
            value1.1
            
            key2 =
            
            # Whitespace
            # and long comment
            key3.anothersubkey = value3
            
        """.trimIndent(),
            propertiesFile.toSortedString()
        )
    }

    @Test
    fun shouldMergePropertiesWithoutNewLine() {
        val settings = PropertySettings()
        settings.endWithNewline = false

        val propertiesFile: PropertiesFile = artificialTestFile(settings)

        assertEquals(
            """
            key1.someLongerKey = some value
            # Comment 1
            key1.subkey1       = value1\
            value1.1
            
            key2 :
            
            # Whitespace
            # and long comment
            key3.anothersubkey value3
        """.trimIndent(),
            propertiesFile.toSortedString()
        )
    }

    @Test
    fun shouldMergePropertiesWithoutSeparatorSpaces() {
        val settings = PropertySettings()
        settings.spaceAroundSeparator = false

        val propertiesFile: PropertiesFile = artificialTestFile(settings)

        assertEquals(
            """
            key1.someLongerKey=some value
            # Comment 1
            key1.subkey1      =value1\
            value1.1
            
            key2:
            
            # Whitespace
            # and long comment
            key3.anothersubkey value3
            
        """.trimIndent(),
            propertiesFile.toSortedString()
        )
    }

    @Test
    fun shouldMergePropertiesWithoutSeparatedGroups() {
        val settings = PropertySettings()
        settings.separateGroups = false

        val propertiesFile: PropertiesFile = artificialTestFile(settings)

        assertEquals(
            """
            key1.someLongerKey = some value
            # Comment 1
            key1.subkey1       = value1\
            value1.1
            key2 :
            # Whitespace
            # and long comment
            key3.anothersubkey value3
            
        """.trimIndent(),
            propertiesFile.toSortedString()
        )
    }

    @Test
    fun shouldMergePropertiesWithoutIndent() {
        val settings = PropertySettings()
        settings.indentValues = false

        val propertiesFile: PropertiesFile = artificialTestFile(settings)

        assertEquals(
            """
            key1.someLongerKey = some value
            # Comment 1
            key1.subkey1 = value1\
            value1.1
            
            key2 :
            
            # Whitespace
            # and long comment
            key3.anothersubkey value3
            
        """.trimIndent(),
            propertiesFile.toSortedString()
        )
    }

    @Test
    fun shouldInsertTrailingComment() {
        val propertiesFile = PropertiesFile(TreeMap(), LINE_SEPARATOR, null, "# Trailing comment")
        assertEquals(
            """
            # Trailing comment
            
        """.trimIndent(), propertiesFile.toSortedString()
        )
    }

    private fun artificialTestFile(settings: PropertySettings?): PropertiesFile {
        val groups: SortedMap<String, PropertyGroup> = TreeMap()

        var group = PropertyGroup("key1")
        group.addProperty(
            PropertyEntry(
                "key1.subkey1",
                "value1\\\nvalue1.1",
                "# Comment 1",
                '='
            )
        )
        group.addProperty(
            PropertyEntry(
                "key1.someLongerKey",
                "some value",
                null,
                '='
            )
        )
        groups["key1"] = group

        group = PropertyGroup("key2")
        group.addProperty(
            PropertyEntry(
                "key2",
                "",
                null,
                ':'
            )
        )
        groups["key2"] = group

        group = PropertyGroup("key3")
        group.addProperty(
            PropertyEntry(
                "key3.anothersubkey",
                "value3",
                "# Whitespace\n# and long comment",
                ' '
            )
        )
        groups["key3"] = group
        return PropertiesFile(groups, LINE_SEPARATOR, settings, null)
    }
}
