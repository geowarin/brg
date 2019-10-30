plugins {
    id("com.avast.gradle.docker-compose") version "0.9.5"
    id("java-library")
    `database-generation`
}

repositories {
    jcenter()
}

dockerCompose {
    stopContainers = false
}