package tasks

import PluginConfig
import TASKS_GROUP
import flyway.FlywayTasks
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

open class CleanDbTask : DefaultTask() {
  @InputFiles
  @Classpath
  lateinit var jooqClasspath: FileCollection

  @Internal
  lateinit var pluginConfig: PluginConfig

  init {
    group = TASKS_GROUP
    description = "Cleans the database with flyway clean"
  }

  @TaskAction
  fun run() {
    val flywayTasks = FlywayTasks(pluginConfig, jooqClasspath)
    flywayTasks.clean()
  }
}