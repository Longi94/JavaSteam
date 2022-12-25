package in.dragonbra.versiongen

import org.gradle.api.Plugin
import org.gradle.api.Project

@SuppressWarnings("GroovyUnusedDeclaration")
class VersionGenPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.getTasks().register("generateProjectVersion", VersionGenTask.class)
    }
}
