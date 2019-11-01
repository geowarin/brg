import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.get
import java.net.URLClassLoader

fun Project.getSourceSet(name: String): SourceSet {
  val sourceSets = extensions.getByName("sourceSets") as SourceSetContainer
  return sourceSets[name]
}

fun createClassLoader(configuration: FileCollection): ClassLoader {
  val urls = configuration.files.map { it.toURI().toURL() }
  return URLClassLoader(urls.toTypedArray())
}