package tasks

import TASKS_GROUP
import codegen.executeCodegen
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File

open class KotlinCodegenTask : DefaultTask() {
  @InputFiles
  @Classpath
  lateinit var classpath: FileCollection

  @Internal
  lateinit var kotlinCodegenTargetDirectory: String

  init {
    group = TASKS_GROUP
    description = "Generate kotlin helpers"
  }

  @TaskAction
  fun run() {
//    val pluginConfig = loadProperties(project.file(CONFIG_FILE))
    executeCodegen(
      project,
      classpath,
      kotlinCodegenTargetDirectory,
      "com.geowarin.model.DefaultCatalog"
    )
  }
}