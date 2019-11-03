package codegen

import KT_CODEGEN_TASK_NAME
import getSourceSet
import kotlin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.process.ExecResult
import java.io.File

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

@Suppress("unused")
class CodegenPlugin : Plugin<Project> {
  override fun apply(project: Project): Unit = project.run {
    val codegenConfig = project.configurations.create("codegen")
    val kotlinCodegenTargetDirectory = "${project.buildDir}/generated-src/codegen/"

    val sourceSet = getSourceSet("main")
    val compileKotlinTask = project.tasks.getByName(sourceSet.getCompileTaskName("kotlin"))
//    val compileJavaTask = project.tasks.getByName(sourceSet.compileJavaTaskName)

    sourceSet.kotlin.srcDir { kotlinCodegenTargetDirectory }

    val kotlinCodegenTask = tasks.create(KT_CODEGEN_TASK_NAME, tasks.KotlinCodegenTask::class.java)
    kotlinCodegenTask.classpath = codegenConfig
    kotlinCodegenTask.kotlinCodegenTargetDirectory = File(kotlinCodegenTargetDirectory)

    compileKotlinTask.dependsOn += kotlinCodegenTask
  }
}
