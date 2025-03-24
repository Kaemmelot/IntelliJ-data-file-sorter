package de.kaemmelot.datafilesorter.properties

import com.intellij.util.xmlb.annotations.OptionTag
import de.kaemmelot.datafilesorter.CharConverter

data class PropertySettings(
    @OptionTag @JvmField val endWithNewline: Boolean = true,
    @OptionTag @JvmField val separateGroups: Boolean = true,
    @OptionTag @JvmField val indentValues: Boolean = true,
    @OptionTag @JvmField val spaceAroundSeparator: Boolean = true,
    @OptionTag @JvmField val forceSeparator: Boolean = false,
    @OptionTag(converter = CharConverter::class) @JvmField val forcedSeparator: Char = '='
)
