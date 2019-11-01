package flyway

import PluginConfig
import createClassLoader
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.MigrationInfo
import org.flywaydb.core.api.MigrationState
import org.flywaydb.core.api.MigrationVersion
import org.flywaydb.core.api.configuration.FluentConfiguration
import org.flywaydb.core.internal.info.MigrationInfoDumper
import org.gradle.api.file.FileCollection

class FlywayTasks(pluginConfig: PluginConfig, gradleConfig: FileCollection, migrationDir: String) {
  private val flyway: Flyway = flyway(pluginConfig, gradleConfig, migrationDir)

  fun info(): MigrationState {
    val info = flyway.info()
    val current: MigrationInfo? = info.current()
    val currentSchemaVersion = current?.version ?: MigrationVersion.EMPTY
    println("Schema version: $currentSchemaVersion")
    println(MigrationInfoDumper.dumpToAsciiTable(info.all()))
    return current?.state ?: MigrationState.UNDONE
  }

  fun migrate() {
    flyway.migrate()
  }

  fun clean() {
    flyway.clean()
  }
}

private fun flyway(pluginConfig: PluginConfig, gradleConfig: FileCollection, migrationDir: String): Flyway {
  val flywayConfig = FluentConfiguration()
    .dataSource(pluginConfig.url, pluginConfig.user, pluginConfig.password)
    .locations(migrationDir)
    .schemas(*pluginConfig.schemas.toTypedArray())

  return Flyway.configure(createClassLoader(gradleConfig)).configuration(flywayConfig).load()
}
