package jooq

import PluginConfig
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.process.ExecResult
import org.jooq.Constants
import org.jooq.codegen.GenerationTool
import org.jooq.meta.jaxb.Configuration
import java.io.File
import javax.xml.XMLConstants
import javax.xml.bind.JAXBContext
import javax.xml.validation.SchemaFactory

fun executeJooq(
  project: Project,
  jooqXmlConfig: Configuration,
  jooqClasspath: FileCollection,
  configFile: File
): ExecResult {
  return project.javaexec {
    main = "org.jooq.codegen.GenerationTool"
    classpath = jooqClasspath
    args = listOf(configFile.toString())
    workingDir = project.projectDir

    writeConfiguration(configFile, jooqXmlConfig)
  }
}

private fun writeConfiguration(file: File, jooqXmlConfig: Configuration) {
  val sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
  val schema = sf.newSchema(GenerationTool::class.java.getResource("/xsd/" + Constants.XSD_CODEGEN))

  val ctx = JAXBContext.newInstance(Configuration::class.java)
  val marshaller = ctx.createMarshaller()
  marshaller.schema = schema

  file.outputStream().use { marshaller.marshal(jooqXmlConfig, it) }
}

fun addJooqConfiguration(project: Project): org.gradle.api.artifacts.Configuration {
  val jooqRuntime = project.configurations.create("jooqRuntime")
  jooqRuntime.description =
    "The classpath used to invoke the jOOQ jooq.generator. Add your JDBC drivers or jooq.generator extensions here."
  project.dependencies.add(jooqRuntime.name, "javax.xml.bind:jaxb-api:2.3.1")
  project.dependencies.add(jooqRuntime.name, "javax.activation:activation:1.1.1")
  project.dependencies.add(jooqRuntime.name, "com.sun.xml.bind:jaxb-core:2.3.0.1")
  project.dependencies.add(jooqRuntime.name, "com.sun.xml.bind:jaxb-impl:2.3.0.1")
  return jooqRuntime
}

fun createJooqConfig(
  pluginProps: PluginConfig,
  jooqCodegenTargetDirectory: String
): Configuration {
  return jooqConfig {
    jdbc {
      driver = pluginProps.driver
      url = pluginProps.url
      user = pluginProps.user
      password = pluginProps.password
    }
    generator {
      name = "org.jooq.codegen.JavaGenerator"
      database {
        includes = pluginProps.jooqSchemas
        excludes = ""
      }
      target {
        packageName = "com.geowarin.model"
        directory = jooqCodegenTargetDirectory
      }
      strategy {
        name = "org.jooq.codegen.DefaultGeneratorStrategy"
      }
    }
  }
}
