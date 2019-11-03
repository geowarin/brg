package com.geowarin.services.test

import org.springframework.boot.test.context.SpringBootTest

@Retention(AnnotationRetention.RUNTIME)
@SpringBootTest(
  properties = [
    "spring.datasource.url=jdbc:tc:postgresql:10-alpine:///databasename",
    "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver"
  ],
  classes = [ServiceConfig::class]
)
internal annotation class ServiceTest