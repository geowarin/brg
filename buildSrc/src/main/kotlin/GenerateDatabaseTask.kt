import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.TaskAction

open class GenerateDatabaseTask() : DefaultTask() {
    lateinit var gradleConfig: Configuration
    lateinit var pluginConfig: PluginConfig

    init {
        group = "codegen"
        description = "Generates codez"
    }

    @TaskAction
    fun run() {
        FlywayTasks(pluginConfig, gradleConfig, "filesystem:${project.projectDir}/migration")
                .info()
    }

}