plugins {
  kotlin("jvm")
}

dependencies {
  implementation("com.squareup:kotlinpoet:1.4.3")
  implementation("com.google.guava:guava:22.0")
  implementation("org.jooq:jooq:3.11.9")
  implementation(kotlin("reflect"))

  testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.2")
  testImplementation("org.junit.jupiter:junit-jupiter-engine:5.5.2")
}

tasks.withType<Test> {
  useJUnitPlatform()
}
