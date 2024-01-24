package `in`.dragonbra.generators.steamlanguage

import org.gradle.api.Plugin
import org.gradle.api.Project

@SuppressWarnings("GroovyUnusedDeclaration")
class SteamLanguageGenPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("generateSteamLanguage", SteamLanguageGenTask::class.java)
    }
}
