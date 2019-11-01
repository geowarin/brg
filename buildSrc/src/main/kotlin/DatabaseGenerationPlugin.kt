import jooq.addJooqConfiguration
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.get

const val TASKS_GROUP = "codegen"

const val GENERATE_TASK_NAME = "jooqCodegen"
const val CLEAN_TASK_NAME = "cleanDatabase"
const val KT_CODEGEN_TASK_NAME = "kotlinCodegen"

const val CONFIG_FILE = "datagen.json5"

@Suppress("unused")
class DatabaseGenerationPlugin : Plugin<Project> {

  override fun apply(project: Project): Unit = project.run {
    val jooqGradleConfig = addJooqConfiguration(project)
    val codegenConfig = project.configurations.create("codegen")

    val kotlinCodegenTargetDirectory = "${project.buildDir}/generated-src/jooq/"
    val jooqCodegenTargetDirectory = "${project.buildDir}/generated-src/codegen/"

    val sourceSet = getSourceSet("main")
    sourceSet.java.srcDir { jooqCodegenTargetDirectory }
    sourceSet.java.srcDir { kotlinCodegenTargetDirectory }
    project.tasks.getByName(sourceSet.compileJavaTaskName).dependsOn += GENERATE_TASK_NAME

    val generateDatabaseTask = project.tasks.create(GENERATE_TASK_NAME, tasks.GenerateDatabaseTask::class.java)
    generateDatabaseTask.jooqClasspath = jooqGradleConfig
    generateDatabaseTask.jooqCodegenTargetDirectory = jooqCodegenTargetDirectory

    val cleanDbTask = project.tasks.create(CLEAN_TASK_NAME, tasks.CleanDbTask::class.java)
    cleanDbTask.jooqClasspath = jooqGradleConfig

    val kotlinCodegenTask = project.tasks.create(KT_CODEGEN_TASK_NAME, tasks.KotlinCodegenTask::class.java)
    kotlinCodegenTask.classpath = codegenConfig
    kotlinCodegenTask.kotlinCodegenTargetDirectory = kotlinCodegenTargetDirectory
  }
}
