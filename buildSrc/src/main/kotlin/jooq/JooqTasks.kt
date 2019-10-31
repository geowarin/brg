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

fun createJooqConfig(
  pluginProps: PluginConfig,
  project: Project
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
        directory = "${project.buildDir}/generated-src/jooq/"
      }
      strategy {
        name = "org.jooq.codegen.DefaultGeneratorStrategy"
      }
    }
  }
}
