plugins {
  kotlin("jvm")
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("org.jooq:jooq:3.12.1")

  implementation(project(":jooq-utils"))
  implementation("com.graphql-java:graphql-java:11.0")

  testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.2")
  testImplementation("org.junit.jupiter:junit-jupiter-engine:5.5.2")

  testImplementation("com.fasterxml.jackson.core:jackson-databind:2.10.0")
  testImplementation("org.assertj:assertj-core:3.13.2")

  testImplementation("org.slf4j:slf4j-api:1.7.28")
  testImplementation("ch.qos.logback:logback-classic:1.2.3")
  testImplementation("org.skyscreamer:jsonassert:1.5.0")
  testImplementation ("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.0")

  testImplementation("org.postgresql:postgresql:42.2.1")
  testImplementation("org.testcontainers:postgresql:1.12.3")
  testImplementation("org.testcontainers:junit-jupiter:1.12.3")
}

tasks.withType<Test> {
  useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
  kotlinOptions {
    jvmTarget = "1.8"
  }
}
