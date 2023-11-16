package `in`.dragonbra.generators.versions

import org.gradle.api.Plugin
import org.gradle.api.Project

class VersionGenPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.create("generateProjectVersion", VersionGenTask::class.java)
    }
}
