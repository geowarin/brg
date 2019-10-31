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
    jooqRuntime("org.jooq:jooq-codegen:3.11.9")
}

dockerCompose {
    stopContainers = false
}