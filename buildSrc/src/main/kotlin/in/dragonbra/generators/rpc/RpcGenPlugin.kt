package `in`.dragonbra.generators.rpc

import org.gradle.api.Plugin
import org.gradle.api.Project

class RpcGenPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("generateRpcMethods", RpcGenTask::class.java)
    }
}
