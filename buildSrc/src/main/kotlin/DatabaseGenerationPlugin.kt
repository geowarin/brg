import jooq.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.kotlin.dsl.get

@Suppress("unused")
class DatabaseGenerationPlugin : Plugin<Project> {

  override fun apply(project: Project): Unit = project.run {
    val pluginProps = loadProperties(file("datagen.json5"))

    val jooqGradleConfig = addJooqConfiguration(project)

    val sourceSets = extensions.getByName("sourceSets") as org.gradle.api.tasks.SourceSetContainer
    val sourceSet = sourceSets["main"]

    val jooqXmlConfig = createJooqConfig(pluginProps, project)
    sourceSet.java.srcDir { jooqXmlConfig.generator.target.directory }
    project.tasks.getByName(sourceSet.compileJavaTaskName).dependsOn += "jooq.generate!"

    val task = project.tasks.create("jooq.generate!", GenerateDatabaseTask::class.java)
    task.pluginConfig = pluginProps
    task.jooqClasspath = jooqGradleConfig
    task.jooqXmlConfig = jooqXmlConfig
  }

  private fun addJooqConfiguration(project: Project): Configuration {
    val jooqRuntime = project.configurations.create("jooqRuntime")
    jooqRuntime.description =
      "The classpath used to invoke the jOOQ jooq.generator. Add your JDBC drivers or jooq.generator extensions here."
    project.dependencies.add(jooqRuntime.name, "javax.xml.bind:jaxb-api:2.3.1")
    project.dependencies.add(jooqRuntime.name, "javax.activation:activation:1.1.1")
    project.dependencies.add(jooqRuntime.name, "com.sun.xml.bind:jaxb-core:2.3.0.1")
    project.dependencies.add(jooqRuntime.name, "com.sun.xml.bind:jaxb-impl:2.3.0.1")
    return jooqRuntime
  }

}