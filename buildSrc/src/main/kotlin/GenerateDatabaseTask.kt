import flyway.FlywayTasks
import jooq.executeJooq
import org.flywaydb.core.api.MigrationState
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.jooq.meta.jaxb.Configuration
import java.io.File

open class GenerateDatabaseTask : DefaultTask() {
  @Internal
  lateinit var pluginConfig: PluginConfig

  @Internal
  lateinit var jooqXmlConfig: Configuration

  @InputFiles
  @Classpath
  lateinit var jooqClasspath: FileCollection

  init {
    group = "codegen"
    description = "Generates codez"
  }

  @TaskAction
  fun run() {
    val flywayTasks = FlywayTasks(pluginConfig, jooqClasspath, "filesystem:${project.projectDir}/migration")
    val migrationState = flywayTasks.info()

    if (migrationState != MigrationState.SUCCESS) {
      flywayTasks.migrate()

      val configFile = File(temporaryDir, "config.xml")
      executeJooq(project, jooqXmlConfig, jooqClasspath, configFile)
    }
  }


}