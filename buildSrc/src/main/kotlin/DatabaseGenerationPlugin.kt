import nu.studer.gradle.jooq.JooqEdition
import nu.studer.gradle.jooq.JooqPlugin
import org.flywaydb.gradle.FlywayExtension
import org.flywaydb.gradle.FlywayPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.get

@Suppress("unused")
class DatabaseGenerationPlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = project.run {
        FlywayPlugin().apply(project)

        val flywayExtension = extensions.getByName("flyway") as FlywayExtension
        flywayExtension.url = "jdbc:postgresql://localhost:5432/postgres"
        flywayExtension.user = "postgres"
        flywayExtension.password = ""
        flywayExtension.driver = "org.postgresql.Driver"
        flywayExtension.schemas = arrayOf("flyway", "brg_security")

        JooqPlugin().apply(project)

        dependencies.add("jooqRuntime", "org.postgresql:postgresql:42.2.1")

        val sourceSets = extensions.getByName("sourceSets") as org.gradle.api.tasks.SourceSetContainer
        jooq {
            version = "3.11.11"
            edition = JooqEdition.OSS
            "sample"(sourceSets["main"]) {
                jdbc {
                    driver = "org.postgresql.Driver"
                    url = "jdbc:postgresql://localhost:5432/postgres"
                    user = "postgres"
                    password = ""
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

        project.tasks.register("generate!", GenerateDatabaseTask::class.java)
    }
}