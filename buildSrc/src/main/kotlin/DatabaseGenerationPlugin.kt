import flyway.migrationDir
import jooq.addJooqConfiguration
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

const val TASKS_GROUP = "codegen"

const val GENERATE_TASK_NAME = "jooqCodegen"
const val CLEAN_TASK_NAME = "cleanDatabase"
const val KT_CODEGEN_TASK_NAME = "kotlinCodegen"

@Suppress("unused")
class DatabaseGenerationPlugin : Plugin<Project> {

  override fun apply(project: Project): Unit = project.run {
    val jooqGradleConfig = addJooqConfiguration(project)
    val codegenConfig = project.configurations.create("codegen")

    val kotlinCodegenTargetDirectory = "${project.buildDir}/generated-src/codegen/"
    val jooqCodegenTargetDirectory = "${project.buildDir}/generated-src/jooq/"

    val pluginConfig = extensions.create("codegen", PluginConfig::class.java)

    val sourceSet = getSourceSet("main")
    sourceSet.java.srcDir { jooqCodegenTargetDirectory }
    sourceSet.kotlin.srcDir { kotlinCodegenTargetDirectory }

    val compileJavaTask = project.tasks.getByName(sourceSet.compileJavaTaskName)
    val compileKotlinTask = project.tasks.getByName(sourceSet.getCompileTaskName("kotlin"))

    val generateDatabaseTask = tasks.create(GENERATE_TASK_NAME, tasks.GenerateDatabaseTask::class.java)
    generateDatabaseTask.jooqClasspath = jooqGradleConfig
    generateDatabaseTask.jooqCodegenTargetDirectory = File(jooqCodegenTargetDirectory)
    generateDatabaseTask.pluginConfig = pluginConfig
    generateDatabaseTask.migrationDir = migrationDir

//    generateDatabaseTask.outputs.upToDateWhen {
//      val flywayTasks = FlywayTasks(pluginConfig, jooqGradleConfig, migrationDir)
//      val migrationState = flywayTasks.migrationState()
//      migrationState == MigrationState.SUCCESS
//    }

    val cleanDbTask = tasks.create(CLEAN_TASK_NAME, tasks.CleanDbTask::class.java)
    cleanDbTask.jooqClasspath = jooqGradleConfig
    cleanDbTask.pluginConfig = pluginConfig

    val kotlinCodegenTask = tasks.create(KT_CODEGEN_TASK_NAME, tasks.KotlinCodegenTask::class.java)
    kotlinCodegenTask.classpath = codegenConfig
    kotlinCodegenTask.kotlinCodegenTargetDirectory = File(kotlinCodegenTargetDirectory)
    kotlinCodegenTask.pluginConfig = pluginConfig

    compileJavaTask.dependsOn += generateDatabaseTask
//    kotlinCodegenTask.mustRunAfter(compileJavaTask)

//    kotlinCodegenTask.dependsOn += compileJavaTask
//    compileKotlinTask.dependsOn += kotlinCodegenTask
  }
}
