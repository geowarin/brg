package com.geowarin.services.user

import com.geowarin.model.brg_security.Tables
import org.jooq.DSLContext
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest

@SpringBootApplication
internal class SpringConfig

@SpringBootTest(
  properties = [
    "spring.datasource.url=jdbc:tc:postgresql:11-alpine:///databasename",
    "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver"
  ]
)
internal class UserServiceTest(
  @Autowired
  val userService: UserService,
  @Autowired
  val jooq: DSLContext
) {

  @Test
  fun toto() {
    userService.insertUser()

    val users = jooq.selectFrom(Tables.BRG_USER).fetch()
    println(users)
  }
}