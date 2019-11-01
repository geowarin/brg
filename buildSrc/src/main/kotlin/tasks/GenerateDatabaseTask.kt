package tasks

import CONFIG_FILE
import PluginConfig
import TASKS_GROUP
import flyway.FlywayTasks
import jooq.createJooqConfig
import jooq.executeJooq
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.*
import java.io.File

open class GenerateDatabaseTask : DefaultTask() {
  @OutputDirectory
  lateinit var jooqCodegenTargetDirectory: File

  @InputFiles
  @Classpath
  lateinit var jooqClasspath: FileCollection

  @Internal
  lateinit var pluginConfig: PluginConfig

  init {
    group = TASKS_GROUP
    description = "Generates codez"
  }

  @TaskAction
  fun run() {
    val flywayTasks = FlywayTasks(pluginConfig, jooqClasspath, "filesystem:${project.projectDir}/migration")
    flywayTasks.printInfo()
    flywayTasks.migrate()

    val configFile = File(temporaryDir, "config.xml")
    val jooqXmlConfig = createJooqConfig(pluginConfig, jooqCodegenTargetDirectory.absolutePath)
    executeJooq(project, jooqXmlConfig, jooqClasspath, configFile)
  }
}