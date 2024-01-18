package `in`.dragonbra.generators.versions.generator

import com.sun.xml.internal.ws.Closeable
import `in`.dragonbra.generators.util.JavaFileWriter
import java.io.File
import java.io.Flushable
import java.io.IOException

class JavaGen(
    private val pkg: String,
    private val destination: File
) : Closeable, Flushable {

    private var writer: JavaFileWriter? = null

    @Throws(IOException::class)
    fun emit(classname: String, version: String) {
        if (!destination.exists() && !destination.isDirectory() && !destination.mkdirs()) {
            throw IllegalStateException("Couldn't create folders")
        }

        val file = File(destination, "$classname.java")

        writer = JavaFileWriter(file)
        writer?.run {
            writeln("package $pkg;")
            writeln()
            writeln("public class $classname {")
            indent()
            writeln("public static String getVersion() {")
            indent()
            writeln("return \"$version\";")
            unindent()
            writeln("}")
            unindent()
            writeln("}")
        }
    }

    override fun close() {
        writer?.close()
    }

    override fun flush() {
        writer?.flush()
    }
}
