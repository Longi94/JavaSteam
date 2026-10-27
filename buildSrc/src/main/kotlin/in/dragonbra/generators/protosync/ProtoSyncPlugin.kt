package `in`.dragonbra.generators.protosync

import org.gradle.api.Plugin
import org.gradle.api.Project

class ProtoSyncPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("syncProtobufs", ProtoSyncTask::class.java)
    }
}
