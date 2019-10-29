import nu.studer.gradle.jooq.JooqEdition

plugins {
  id("nu.studer.jooq")
  id("org.flywaydb.flyway") version "6.0.7"
  id("com.avast.gradle.docker-compose") version "0.9.5"
  id("java-library")
}

buildscript {
  dependencies {
    // for flyway
    classpath("org.postgresql:postgresql:42.2.1")
  }
}

repositories {
  jcenter()
}

dependencies {
  jooqRuntime("org.postgresql:postgresql:42.2.1")
}

dockerCompose {
  stopContainers = false
}

flyway {
  url = "jdbc:postgresql://localhost:5432/postgres"
  user = "postgres"
  password = ""
  driver = "org.postgresql.Driver"
  schemas = arrayOf("flyway", "brg_security")
//  baselineOnMigrate = true
//  locations = arrayOf("migration")
}

jooq {
  version = "3.11.11"
  edition = JooqEdition.OSS
  "sample"(sourceSets["main"]) {
    jdbc {
      driver = "org.postgresql.Driver"
      url = "jdbc:postgresql://localhost:5432/postgres"
      user = "postgres"
      password = ""
    }
    generator {
      name = "org.jooq.codegen.DefaultGenerator"
      database {
        includes = "brg_.*"
        excludes = ""
      }
      target {
        packageName = "com.geowarin.model"
      }
      strategy {
        name = "org.jooq.codegen.DefaultGeneratorStrategy"
      }
    }
  }
}
