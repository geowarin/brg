package codegen

import getSourceSet
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.process.ExecResult


fun executeCodegen(
  project: Project,
  classpathConfig: FileCollection,
  destination: String,
  catalog: String
): ExecResult {
  return project.javaexec {
    main = "codegen.KotlinTableFunctionsGeneratorKt"
    classpath = classpathConfig + cp(project)
    args = listOf(destination, catalog)
    workingDir = project.projectDir
    errorOutput = System.err
    standardOutput = System.out
  }
}

fun cp(project: Project): FileCollection {
  val mainSourceSet = project.getSourceSet("main")
//  return mainSourceSet.output +
  return mainSourceSet.compileClasspath + mainSourceSet.runtimeClasspath
}
