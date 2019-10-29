include(
  "data-model",
  "web-backend"
)

rootProject.name = "brg"

rootProject.children.forEach { project ->
  val projectDirName = "modules/${project.name}"
  project.projectDir = File(settingsDir, projectDirName)
  project.buildFileName = "${project.name}.gradle.kts"
}
