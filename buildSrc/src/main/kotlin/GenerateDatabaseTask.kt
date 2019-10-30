import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.get

open class GenerateDatabaseTask: DefaultTask() {

    init {
        group = "codegen"
        description = "Generates codez"
        dependsOn += project.tasks["flywayMigrate"]
    }

    @TaskAction
    fun run() {
        println("I'm ${project.name}")
    }
}