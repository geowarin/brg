package tasks

import PluginConfig
import TASKS_GROUP
import codegen.executeCodegen
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.*
import java.io.File

open class KotlinCodegenTask : DefaultTask() {
  @InputFiles
  @Classpath
  lateinit var classpath: FileCollection

  @OutputDirectory
  lateinit var kotlinCodegenTargetDirectory: File

  init {
    group = TASKS_GROUP
    description = "Generate kotlin helpers"
  }

  @TaskAction
  fun run() {
    executeCodegen(
      project,
      classpath,
      kotlinCodegenTargetDirectory.absolutePath,
      "com.geowarin.model.DefaultCatalog"
    )
  }
}