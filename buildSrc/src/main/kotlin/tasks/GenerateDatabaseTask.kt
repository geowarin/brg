package tasks

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

  @InputDirectory
  lateinit var migrationDir: File

  @Internal
  lateinit var pluginConfig: PluginConfig

  init {
    group = TASKS_GROUP
    description = "Generates codez"
  }

  @TaskAction
  fun run() {
    val flywayTasks = FlywayTasks(pluginConfig, jooqClasspath, migrationDir)
    flywayTasks.printInfo()
    flywayTasks.migrate()

    val configFile = File(temporaryDir, "config.xml")
    val logConfigFile = File(temporaryDir, "logback.xml")
    val jooqXmlConfig = createJooqConfig(pluginConfig, jooqCodegenTargetDirectory.absolutePath)
    executeJooq(project, jooqXmlConfig, jooqClasspath, configFile, logConfigFile)
  }
}