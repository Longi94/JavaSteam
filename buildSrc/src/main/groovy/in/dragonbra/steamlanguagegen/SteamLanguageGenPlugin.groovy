package in.dragonbra.steamlanguagegen

import org.gradle.api.Plugin
import org.gradle.api.Project

@SuppressWarnings("GroovyUnusedDeclaration")
class SteamLanguageGenPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.getTasks().register("generateSteamLanguage", SteamLanguageGenTask.class)
    }
}