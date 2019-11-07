plugins {
  kotlin("jvm")
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("org.jooq:jooq:3.12.1")

  implementation(project(":data-model"))
  implementation(project(":data-model-functions"))
  implementation("com.graphql-java:graphql-java:11.0")

  testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.2")
  testImplementation("org.junit.jupiter:junit-jupiter-engine:5.5.2")
}

tasks.withType<Test> {
  useJUnitPlatform()
}