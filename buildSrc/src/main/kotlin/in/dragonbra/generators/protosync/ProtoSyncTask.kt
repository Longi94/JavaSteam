package `in`.dragonbra.generators.protosync

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

/**
 * Re-downloads every .proto file listed in `protosync-whitelist.txt` (repository root)
 * from [SteamTracking/Protobufs](https://github.com/SteamTracking/Protobufs) and
 * re-applies the JavaSteam transformations:
 *
 * - imports are rewritten to this repository's proto source-root layout
 * - `option java_package` is injected, `cc_generic_services` is replaced
 *   with `java_generic_services = false`
 * - tab indentation is converted to two spaces
 *
 * The whitelist is the manifest of vetted protos: only listed files are written
 * (created if not present locally yet). Upstream imports of unlisted files are dropped
 * and reported as vetting candidates - protoc fails the build if a listed proto
 * actually uses their types, which is the prompt to vet the file and whitelist it.
 *
 * Usage: `gradlew syncProtobufs`
 */
@Suppress("unused")
abstract class ProtoSyncTask : DefaultTask {

    private data class ProtoModule(
        val upstreamDir: String,
        val localDir: String,
        val pkg: String,
        val packageLine: String? = null,
    )

    private companion object {
        const val REPO_URL = "https://github.com/SteamTracking/Protobufs.git"
        const val PROTO_ROOT = "src/main/proto/in/dragonbra/javasteam/protobufs"

        val MODULES = listOf(
            ProtoModule("steam", PROTO_ROOT, "steamclient"),
            ProtoModule("webui", "javasteam-protobufs-webui/$PROTO_ROOT", "webui", packageLine = "package webui;"),
            ProtoModule("csgo", "javasteam-cs/$PROTO_ROOT", "cs"),
            ProtoModule("dota2", "javasteam-dota2/$PROTO_ROOT", "dota2"),
            ProtoModule("tf2", "javasteam-tf/$PROTO_ROOT", "tf"),
            ProtoModule("deadlock", "javasteam-deadlock/$PROTO_ROOT", "deadlock"),
        )

        // Header options owned by the transform: stripped from upstream (and from any
        // previously transformed file) before the canonical block is inserted.
        // additional_includes only exists in Valve's own descriptor.proto, protoc rejects it.
        val MANAGED_OPTIONS = Regex(
            """^option\s+(optimize_for|cc_generic_services|py_generic_services|java_generic_services|java_package|additional_includes)\b.*"""
        )

        // Field options that only exist in Valve's own descriptor.proto; bare (non-extension)
        // names like these are unresolvable with a standard protoc and must be dropped.
        val VALVE_FIELD_OPTIONS = Regex("""(,\s*)?(boxed_type|synthetic_default)\s*=\s*("[^"]*"|[^,\[\]]+)""")

        // Root-qualified references like `optional .CFoo` / `[(.description) = ...]` break once a
        // `package` line is injected; they get rewritten relative to the injected package.
        val ROOT_TYPE_REF = Regex("""([\s(<,])\.(?=[A-Z])""")
        val ROOT_EXT_OPTION = Regex("""\(\.(?=[a-z_])""")

        val IMPORT_LINE = Regex("""^import( public| weak)?\s+"([^"]+)";?\s*$""")
    }

    private val rootDir: File = project.projectDir
    private val checkoutDir: File = project.layout.buildDirectory.dir("steamtracking").get().asFile

    @Inject
    constructor() {
        group = "protobufs"
        description = "Updates all .proto files from SteamTracking/Protobufs"
    }

    @TaskAction
    fun sync() {
        val upstream = obtainUpstream()
        val sha = git(upstream, "rev-parse", "HEAD").trim()
        println("Syncing protobufs from SteamTracking/Protobufs @ $sha")

        val whitelist = loadWhitelist()
        val missing = mutableListOf<String>()
        val added = mutableListOf<String>()
        val candidates = mutableListOf<String>()
        val unmanaged = mutableListOf<String>()
        var count = 0

        MODULES.forEach { module ->
            val listed = whitelist[module.pkg].orEmpty()
            val localDir = rootDir.resolve(module.localDir).resolve(module.pkg)
            val localFiles = localDir.listFiles { file -> file.extension == "proto" }
                ?: throw GradleException("Proto directory not found: $localDir")

            localFiles.filter { it.name !in listed }.forEach { unmanaged.add("${module.pkg}/${it.name}") }

            // Line endings of the current checkout, so synced files diff cleanly
            val eol = localFiles.firstOrNull()?.readText()?.let { if (it.contains("\r\n")) "\r\n" else "\n" } ?: "\n"

            listed.forEach { name ->
                val upstreamFile = upstream.resolve(module.upstreamDir).resolve(name)
                if (!upstreamFile.isFile) {
                    missing.add("${module.upstreamDir}/$name")
                    return@forEach
                }

                // Imports of unlisted files are dropped unless a local copy exists anyway;
                // dropped ones are reported as vetting candidates.
                val transformed = transform(upstreamFile.readText(), module) { importName ->
                    val drop = importName !in listed && !localDir.resolve(importName).isFile
                    if (drop) {
                        candidates.add("${module.pkg}/$importName (imported by $name)")
                    }
                    drop
                }
                val local = localDir.resolve(name)
                if (!local.isFile) {
                    added.add("${module.localDir}/${module.pkg}/$name")
                }
                local.writeText(transformed.replace("\n", eol))
                count++
            }
        }

        println("Synced $count proto files @ $sha")

        if (added.isNotEmpty()) {
            println("Added ${added.size} newly whitelisted file(s):")
            added.forEach { println("  $it") }
        }

        if (candidates.isNotEmpty()) {
            println("Skipped ${candidates.size} upstream import(s) of files not in protosync-whitelist.txt;")
            println("protoc will fail if their types are used - vet them and add them to the whitelist:")
            candidates.forEach { println("  $it") }
        }

        if (unmanaged.isNotEmpty()) {
            println("${unmanaged.size} local proto file(s) are not whitelisted and were left untouched:")
            unmanaged.forEach { println("  $it") }
        }

        if (missing.isNotEmpty()) {
            logger.warn(
                "WARNING: ${missing.size} whitelisted proto file(s) do not exist upstream " +
                    "(renamed or deleted from SteamTracking, needs manual attention):"
            )
            missing.forEach { logger.warn("  $it") }
        }
    }

    private fun transform(content: String, module: ProtoModule, dropImport: (String) -> Boolean): String {
        val importPrefix = "in/dragonbra/javasteam/protobufs/${module.pkg}"
        val imports = mutableListOf<String>()
        val body = mutableListOf<String>()

        content.lines().forEach { raw ->
            val line = raw.trimEnd()
            val trimmed = line.trim()
            val importMatch = IMPORT_LINE.matchEntire(trimmed)
            when {
                importMatch != null -> {
                    val modifier = importMatch.groupValues[1]
                    val file = importMatch.groupValues[2]
                    val baseName = file.substringAfterLast('/')
                    when {
                        file.startsWith("google/") -> imports.add("import$modifier \"$file\";")
                        !dropImport(baseName) -> imports.add("import$modifier \"$importPrefix/$baseName\";")
                    }
                }
                MANAGED_OPTIONS.matches(trimmed) -> Unit
                module.packageLine != null && trimmed.startsWith("package ") -> Unit
                else -> body.add(transformBodyLine(line, module))
            }
        }

        while (body.isNotEmpty() && body.first().isBlank()) body.removeAt(0)
        while (body.isNotEmpty() && body.last().isBlank()) body.removeAt(body.lastIndex)

        return buildString {
            imports.forEach { appendLine(it) }
            if (imports.isNotEmpty()) appendLine()
            module.packageLine?.let {
                appendLine(it)
                appendLine()
            }
            appendLine("option java_package = \"in.dragonbra.javasteam.protobufs.${module.pkg}\";")
            appendLine()
            appendLine("option optimize_for = SPEED;")
            appendLine("option java_generic_services = false;")
            appendLine()
            body.forEach { appendLine(it) }
        }
    }

    private fun loadWhitelist(): Map<String, Set<String>> {
        val file = rootDir.resolve("protosync-whitelist.txt")
        if (!file.isFile) {
            throw GradleException("Whitelist not found: $file")
        }

        val sections = mutableMapOf<String, MutableSet<String>>()
        var current: MutableSet<String>? = null

        file.readLines().map { it.trim() }.forEach { line ->
            when {
                line.isEmpty() || line.startsWith("#") -> Unit
                line.startsWith("[") && line.endsWith("]") -> {
                    val pkg = line.substring(1, line.length - 1)
                    if (MODULES.none { it.pkg == pkg }) {
                        throw GradleException("Unknown module [$pkg] in protosync-whitelist.txt")
                    }
                    current = sections.getOrPut(pkg) { mutableSetOf() }
                }
                else -> (current ?: throw GradleException("Entry before any [module] section in protosync-whitelist.txt: $line"))
                    .add(line)
            }
        }

        return sections
    }

    private fun transformBodyLine(raw: String, module: ProtoModule): String {
        var line = stripValveFieldOptions(raw.replace("\t", "  "))

        if (module.packageLine != null) {
            line = line
                .replace(ROOT_TYPE_REF, "$1")
                .replace(ROOT_EXT_OPTION, "(${module.pkg}.")
        }

        return line
    }

    private fun stripValveFieldOptions(line: String): String {
        if (!VALVE_FIELD_OPTIONS.containsMatchIn(line)) {
            return line
        }

        return line
            .replace(VALVE_FIELD_OPTIONS, "")
            .replace(Regex("""\[\s*,\s*"""), "[")
            .replace(Regex("""\s*,\s*]"""), "]")
            .replace(Regex("""\s*\[\s*]"""), "")
    }

    private fun obtainUpstream(): File {
        if (checkoutDir.resolve(".git").isDirectory) {
            git(checkoutDir, "fetch", "--depth", "1", "origin", "master")
            git(checkoutDir, "reset", "--hard", "origin/master")
        } else {
            checkoutDir.parentFile.mkdirs()
            git(checkoutDir.parentFile, "clone", "--depth", "1", REPO_URL, checkoutDir.name)
        }

        return checkoutDir
    }

    private fun git(workingDir: File, vararg args: String): String {
        val process = ProcessBuilder("git", *args)
            .directory(workingDir)
            .redirectErrorStream(true)
            .start()

        val output = process.inputStream.bufferedReader().readText()
        if (process.waitFor() != 0) {
            throw GradleException("git ${args.joinToString(" ")} failed:\n$output")
        }

        return output
    }
}
