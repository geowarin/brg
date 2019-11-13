plugins {
  kotlin("jvm")
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

  implementation(project(":data-model"))
  implementation(project(":data-model-functions"))

  runtimeOnly("org.postgresql:postgresql")

  testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.2")
  testImplementation("org.junit.jupiter:junit-jupiter-engine:5.5.2")
}

tasks.withType<Test> {
  useJUnitPlatform()
}