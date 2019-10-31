import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import nu.studer.gradle.jooq.JooqEdition
import nu.studer.gradle.jooq.JooqPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.get
import java.io.File

@Suppress("unused")
class DatabaseGenerationPlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = project.run {
        val pluginProps = loadProperties(file("datagen.json5"))
        JooqPlugin().apply(project)

        dependencies.add("jooqRuntime", "org.postgresql:postgresql:42.2.1")

        val sourceSets = extensions.getByName("sourceSets") as org.gradle.api.tasks.SourceSetContainer
        jooq {
            version = "3.11.11"
            edition = JooqEdition.OSS
            "sample"(sourceSets["main"]) {
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
                    }
                    strategy {
                        name = "org.jooq.codegen.DefaultGeneratorStrategy"
                    }
                }
            }
        }

        val task = project.tasks.create("generate!", GenerateDatabaseTask::class.java)
        task.gradleConfig = configurations["jooqRuntime"]
        task.pluginConfig = pluginProps
    }

    private fun loadProperties(f: File): PluginConfig {
        val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build();
        return moshi.adapter(PluginConfig::class.java)
                .lenient()
                .fromJson(f.readText())!!
    }
}