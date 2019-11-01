import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.HasConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.net.URLClassLoader

fun Project.getSourceSet(name: String): SourceSet {
  val sourceSets = extensions.getByName("sourceSets") as SourceSetContainer
  return sourceSets[name]
}

fun createClassLoader(configuration: FileCollection): ClassLoader {
  val urls = configuration.files.map { it.toURI().toURL() }
  return URLClassLoader(urls.toTypedArray())
}

val SourceSet.kotlin: SourceDirectorySet
  get() =
    (this as HasConvention)
      .convention
      .getPlugin(KotlinSourceSet::class.java)
      .kotlin


fun SourceSet.kotlin(action: SourceDirectorySet.() -> Unit) =
  kotlin.action()