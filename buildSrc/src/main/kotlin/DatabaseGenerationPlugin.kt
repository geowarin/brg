import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.kotlin.dsl.get
import java.io.File

@Suppress("unused")
class DatabaseGenerationPlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = project.run {
        val pluginProps = loadProperties(file("datagen.json5"))

        val jooqGradleConfig = addJooqConfiguration(project)
        dependencies.add("jooqRuntime", "org.postgresql:postgresql:42.2.1")

        val sourceSets = extensions.getByName("sourceSets") as org.gradle.api.tasks.SourceSetContainer
        val sourceSet = sourceSets["main"]
        val jooqXmlConfig = jooqConfig {
            jdbc {
                driver = pluginProps.driver
                url = pluginProps.url
                user = pluginProps.user
                password = pluginProps.password
            }
            generator {
                name = "org.jooq.codegen.DefaultGenerator"
                database {
                    includes = "brg_.*"
                    excludes = ""
                }
                target {
                    packageName = "com.geowarin.model"
                    directory = "${project.buildDir}/generated-src/jooq/"
                }
                strategy {
                    name = "org.jooq.codegen.DefaultGeneratorStrategy"
                }
            }
        }
        sourceSet.java.srcDir { jooqXmlConfig.generator.target.directory }
        project.tasks.getByName(sourceSet.compileJavaTaskName).dependsOn += "generate!"

        val task = project.tasks.create("generate!", GenerateDatabaseTask::class.java)
        task.pluginConfig = pluginProps
        task.jooqClasspath = jooqGradleConfig
        task.jooqXmlConfig = jooqXmlConfig
    }

    private fun loadProperties(f: File): PluginConfig {
        val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
        return moshi.adapter(PluginConfig::class.java)
                .lenient()
                .fromJson(f.readText())!!
    }

    private fun addJooqConfiguration(project: Project): Configuration {
        val jooqRuntime = project.configurations.create("jooqRuntime")
        jooqRuntime.description = "The classpath used to invoke the jOOQ generator. Add your JDBC drivers or generator extensions here."
        project.dependencies.add(jooqRuntime.name, "org.jooq:jooq-codegen:3.11.9")
        project.dependencies.add(jooqRuntime.name, "javax.xml.bind:jaxb-api:2.3.1")
        project.dependencies.add(jooqRuntime.name, "javax.activation:activation:1.1.1")
        project.dependencies.add(jooqRuntime.name, "com.sun.xml.bind:jaxb-core:2.3.0.1")
        project.dependencies.add(jooqRuntime.name, "com.sun.xml.bind:jaxb-impl:2.3.0.1")
        return jooqRuntime
    }

}