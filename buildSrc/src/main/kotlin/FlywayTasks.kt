import org.flywaydb.core.Flyway
import org.flywaydb.core.api.MigrationVersion
import org.flywaydb.core.api.configuration.FluentConfiguration
import org.flywaydb.core.internal.info.MigrationInfoDumper
import org.gradle.api.file.FileCollection
import java.net.URLClassLoader

class FlywayTasks(pluginConfig: PluginConfig, gradleConfig: FileCollection, migrationDir: String) {
    private val flyway: Flyway = flyway(pluginConfig, gradleConfig, migrationDir)

    fun info() {
        val info = flyway.info()
        val current = info.current()
        val currentSchemaVersion = current?.version ?: MigrationVersion.EMPTY
        println("Schema version: $currentSchemaVersion")
        println(MigrationInfoDumper.dumpToAsciiTable(info.all()))
    }
}

private fun flyway(pluginConfig: PluginConfig, gradleConfig: FileCollection, migrationDir: String): Flyway {
    val flywayConfig = FluentConfiguration()
            .dataSource(pluginConfig.url, pluginConfig.user, pluginConfig.password)
            .locations(migrationDir)

    return Flyway.configure(createClassLoader(gradleConfig)).configuration(flywayConfig).load()
}

private fun createClassLoader(configuration: FileCollection): ClassLoader {
    val urls = configuration.files.map { it.toURI().toURL() }
    return URLClassLoader(urls.toTypedArray())
}