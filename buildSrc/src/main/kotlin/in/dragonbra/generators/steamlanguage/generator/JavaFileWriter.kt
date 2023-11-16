package `in`.dragonbra.generators.steamlanguage.generator

import java.io.File
import java.io.FileWriter
import java.io.IOException

class JavaFileWriter(private val file: File) : FileWriter(file) {

    companion object {
        private const val INDENT_SIZE = 4
    }

    private var indent: Int = 0

    fun indent() {
        indent += INDENT_SIZE
    }

    fun unindent() {
        indent -= INDENT_SIZE
    }

    @Throws(IOException::class)
    fun writeln(string: String) {
        write("    ".repeat(indent))
        write(string)
        writeln()
    }

    @Throws(IOException::class)
    fun writeln() {
        write("\n")
    }
}
