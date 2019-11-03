plugins {
  `kotlin-dsl`
}

repositories {
  jcenter()
  mavenCentral()
}

gradlePlugin {
  plugins {
    register("database-generation") {
      id = "database-generation"
      implementationClass = "DatabaseGenerationPlugin"
    }
    register("kodegen") {
      id = "kodegen"
      implementationClass = "codegen.CodegenPlugin"
    }
  }
}

dependencies {
  // Jooq
  implementation("org.jooq:jooq-codegen:3.12.1")
  implementation("javax.xml.bind:jaxb-api:2.3.1")
  implementation("javax.activation:activation:1.1.1")
  implementation("com.sun.xml.bind:jaxb-core:2.3.0.1")
  implementation("com.sun.xml.bind:jaxb-impl:2.3.0.1")

  implementation("org.flywaydb:flyway-core:6.0.7")
  implementation("org.jetbrains.kotlin:kotlin-gradle-plugin")
}
