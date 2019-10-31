package tasks

import CONFIG_FILE
import TASKS_GROUP
import flyway.FlywayTasks
import jooq.createJooqConfig
import jooq.executeJooq
import loadProperties
import org.flywaydb.core.api.MigrationState
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File

open class GenerateDatabaseTask : DefaultTask() {
  @Internal
  lateinit var jooqCodegenTargetDirectory: String

  @InputFiles
  @Classpath
  lateinit var jooqClasspath: FileCollection

  init {
    group = TASKS_GROUP
    description = "Generates codez"
  }

  @TaskAction
  fun run() {
    val pluginConfig = loadProperties(project.file(CONFIG_FILE))

    val flywayTasks = FlywayTasks(pluginConfig, jooqClasspath, "filesystem:${project.projectDir}/migration")
    val migrationState = flywayTasks.info()

    if (migrationState != MigrationState.SUCCESS) {
      flywayTasks.migrate()

      val configFile = File(temporaryDir, "config.xml")
      val jooqXmlConfig = createJooqConfig(pluginConfig, jooqCodegenTargetDirectory)
      executeJooq(project, jooqXmlConfig, jooqClasspath, configFile)
    }
  }


}