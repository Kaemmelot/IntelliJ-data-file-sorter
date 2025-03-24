package de.kaemmelot.datafilesorter.vcs

import com.intellij.openapi.vfs.VirtualFile

class MissingLineSeparatorException(val file: VirtualFile) : Exception()
