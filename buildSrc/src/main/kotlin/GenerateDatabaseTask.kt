import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecResult
import org.jooq.Constants
import org.jooq.codegen.GenerationTool
import org.jooq.meta.jaxb.Configuration
import java.io.File
import javax.xml.XMLConstants
import javax.xml.bind.JAXBContext
import javax.xml.validation.SchemaFactory

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
    flywayTasks.info()
    flywayTasks.migrate()

    executeJooq()
  }

  private fun executeJooq(): ExecResult {
    val configFile = File(temporaryDir, "config.xml")
    return project.javaexec {
      main = "org.jooq.codegen.GenerationTool"
      classpath = jooqClasspath
      args = listOf(configFile.toString())
      workingDir = project.projectDir

      configFile.parentFile.mkdirs()
      writeConfiguration(configFile)
    }
  }

  private fun writeConfiguration(file: File) {
    val sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
    val schema = sf.newSchema(GenerationTool::class.java.getResource("/xsd/" + Constants.XSD_CODEGEN))

    val ctx = JAXBContext.newInstance(Configuration::class.java)
    val marshaller = ctx.createMarshaller()
    marshaller.schema = schema

    file.outputStream().use { marshaller.marshal(jooqXmlConfig, it) }
  }
}