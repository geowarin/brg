plugins {
    id("com.avast.gradle.docker-compose") version "0.9.5"
    kotlin("jvm") version "1.3.50"
    `database-generation`
}

repositories {
    jcenter()
}

dependencies {
    jooqRuntime("org.postgresql:postgresql:42.2.1")
    jooqRuntime("org.jooq:jooq-codegen:3.11.9")
    codegen(project(":jooq-kotlin-gen"))
    implementation("org.jooq:jooq:3.11.9")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
}

dockerCompose {
    stopContainers = false
}