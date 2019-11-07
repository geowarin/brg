import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("org.springframework.boot") version "2.2.0.RELEASE"
  id("io.spring.dependency-management") version "1.0.8.RELEASE"
  kotlin("jvm")
  kotlin("plugin.spring") version "1.3.50"
}

group = "com.geowarin"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

val developmentOnly by configurations.creating
configurations {
  runtimeClasspath {
    extendsFrom(developmentOnly)
  }
  compileOnly {
    extendsFrom(configurations.annotationProcessor.get())
  }
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-jooq")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
  implementation("com.graphql-java:graphql-java-spring-boot-starter-webflux:1.0")

  implementation(project(":services"))
  implementation(project(":data-model"))
  implementation(project(":graphql"))

  developmentOnly("org.springframework.boot:spring-boot-devtools")
  runtimeOnly("org.postgresql:postgresql")

  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
  testImplementation("org.springframework.boot:spring-boot-starter-test") {
    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
  }
  testImplementation("io.projectreactor:reactor-test")
  testImplementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
  testImplementation("org.testcontainers:postgresql:1.12.3")
  testImplementation("org.testcontainers:junit-jupiter:1.12.3")
  testImplementation("org.flywaydb:flyway-core")
}

tasks.withType<Test> {
  useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs = listOf("-Xjsr305=strict")
    jvmTarget = "1.8"
  }
}

task("generateSchema", JavaExec::class) {
  main = "com.geowarin.graphql.SchemaGeneratorKt"
  classpath = sourceSets["main"].runtimeClasspath
  args = listOf(file("${projectDir}/brg.graphql").absolutePath)
}