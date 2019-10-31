import jooq.addJooqConfiguration
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.get
import tasks.GenerateDatabaseTask

const val TASKS_GROUP = "codegen"

const val GENERATE_TASK_NAME = "jooqCodegen"
const val CLEAN_TASK_NAME = "cleanDatabase"
const val CONFIG_FILE = "datagen.json5"

@Suppress("unused")
class DatabaseGenerationPlugin : Plugin<Project> {

  override fun apply(project: Project): Unit = project.run {
    val jooqGradleConfig = addJooqConfiguration(project)

    val jooqCodegenTargetDirectory = "${project.buildDir}/generated-src/jooq/"

    val sourceSet = getSourceSet("main")
    sourceSet.java.srcDir { jooqCodegenTargetDirectory }
    project.tasks.getByName(sourceSet.compileJavaTaskName).dependsOn += GENERATE_TASK_NAME

    val generateDatabaseTask = project.tasks.create(GENERATE_TASK_NAME, GenerateDatabaseTask::class.java)
    generateDatabaseTask.jooqClasspath = jooqGradleConfig
    generateDatabaseTask.jooqCodegenTargetDirectory = jooqCodegenTargetDirectory

    val task = project.tasks.create(CLEAN_TASK_NAME, tasks.CleanDbTask::class.java)
    task.jooqClasspath = jooqGradleConfig
  }

  private fun Project.getSourceSet(name: String): SourceSet {
    val sourceSets = extensions.getByName("sourceSets") as SourceSetContainer
    return sourceSets[name]
  }
}