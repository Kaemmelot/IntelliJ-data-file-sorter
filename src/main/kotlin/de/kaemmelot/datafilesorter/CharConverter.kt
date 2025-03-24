package de.kaemmelot.datafilesorter

import com.intellij.util.xmlb.Converter

class CharConverter: Converter<Char>() {

    override fun fromString(value: String): Char? {
        return if (value.isNotEmpty()) { value[0] } else { null }
    }

    override fun toString(value: Char): String = value.toString()
}
