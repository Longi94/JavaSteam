package `in`.dragonbra.generators.steamlanguage.generator

import java.io.File
import java.io.FileWriter
import java.io.IOException

class JavaFileWriter(file: File) : FileWriter(file) {

    companion object {
        private const val INDENTATION = "    "
    }

    private var indent: String = ""

    fun indent() {
        indent += INDENTATION
    }

    fun unindent() {
        indent = indent.substring(INDENTATION.length)
    }

    @Throws(IOException::class)
    fun writeln(string: String) {
        write(indent)
        write(string)
        writeln()
    }

    @Throws(IOException::class)
    fun writeln() {
        write("\n")
    }
}
