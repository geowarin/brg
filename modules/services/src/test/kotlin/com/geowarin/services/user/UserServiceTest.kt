package com.geowarin.services.user

import com.geowarin.model.brg_security.Tables
import com.geowarin.services.test.ServiceTest
import org.jooq.DSLContext
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@ServiceTest
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