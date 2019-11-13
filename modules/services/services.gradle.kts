import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
  id("org.springframework.boot") version "2.2.0.RELEASE"
  id("io.spring.dependency-management") version "1.0.8.RELEASE"
  kotlin("jvm")
  kotlin("plugin.spring") version "1.3.50"
}

tasks.getByName<BootJar>("bootJar") {
  enabled = false
}

tasks.getByName<Jar>("jar") {
  enabled = true
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("org.springframework.boot:spring-boot-starter")
  implementation("org.springframework.boot:spring-boot-starter-jooq")

  implementation(project(":data-model"))
  implementation(project(":data-model-functions"))

  runtimeOnly("org.postgresql:postgresql")

  testImplementation("org.flywaydb:flyway-core")
  testImplementation("org.testcontainers:testcontainers:1.12.3")
  testImplementation("org.testcontainers:postgresql:1.12.3")
  testImplementation("org.testcontainers:junit-jupiter:1.12.3")

  testImplementation("org.springframework.boot:spring-boot-starter-test") {
    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}