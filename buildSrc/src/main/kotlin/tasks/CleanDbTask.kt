package tasks

import CONFIG_FILE
import TASKS_GROUP
import flyway.FlywayTasks
import loadProperties
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

open class CleanDbTask : DefaultTask() {
  @InputFiles
  @Classpath
  lateinit var jooqClasspath: FileCollection

  init {
    group = TASKS_GROUP
    description = "Cleans the database with flyway clean"
  }

  @TaskAction
  fun run() {
    val pluginConfig = loadProperties(project.file(CONFIG_FILE))
    val flywayTasks = FlywayTasks(pluginConfig, jooqClasspath, "filesystem:${project.projectDir}/migration")
    flywayTasks.clean()
  }
}