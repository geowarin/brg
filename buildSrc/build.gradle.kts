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
  }
}

dependencies {
  implementation("nu.studer:gradle-jooq-plugin:3.0.3")
  implementation("org.flywaydb:flyway-gradle-plugin:6.0.7")
  implementation("org.postgresql:postgresql:42.2.1")
  implementation("com.squareup.moshi:moshi-kotlin:1.9.0")
}
