plugins {
  id("com.avast.gradle.docker-compose") version "0.9.5"
  java
  `database-generation`
}

repositories {
  jcenter()
}

dependencies {
  jooqRuntime("org.postgresql:postgresql:42.2.1")
  jooqRuntime("org.jooq:jooq-codegen:3.12.1")

  implementation("org.jooq:jooq:3.12.1")
  implementation("javax.annotation:javax.annotation-api:1.3.2")
}

codegen {
  url = "jdbc:postgresql://localhost:5432/postgres"
  user = "postgres"
  password = ""
  driver = "org.postgresql.Driver"
  schemas = listOf("flyway", "brg_security", "forum_example", "forum_example_private")
  jooqSchemas = "brg_.*|forum_example.*"
}

dockerCompose {
  stopContainers = false
}