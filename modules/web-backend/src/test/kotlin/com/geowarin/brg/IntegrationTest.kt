package com.geowarin.brg

import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  properties = [
    "spring.datasource.url=jdbc:tc:postgresql:10-alpine:///databasename",
    "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver"
  ],
  classes = [ServiceConfig::class, BrgApplication::class]
)
annotation class IntegrationTest