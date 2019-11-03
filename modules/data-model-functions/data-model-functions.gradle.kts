plugins {
  kotlin
  kodegen
}

dependencies {
  codegen(project(":jooq-kotlin-gen"))
  implementation("org.jooq:jooq:3.12.1")

  implementation(project(":data-model"))
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}
