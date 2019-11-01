package com.geowarin.services.user

import com.geowarin.model.builders.brgSecurity.newBrgUserRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Service

@Service
class UserService(
  val jooq: DSLContext
) {

  fun insertUser() {
    val userRecord = newBrgUserRecord(
      email = "toto",
      password = "toto"
    )
    jooq.executeInsert(userRecord)
  }
}