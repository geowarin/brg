package com.geowarin.services.user

import com.geowarin.model.brg_security.enums.Role
import com.geowarin.model.builders.brgSecurity.newBrgUserRecord
import com.geowarin.model.builders.brgSecurity.newUserRolesRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService(
  val jooq: DSLContext
) {

  fun insertUser(email: String, vararg roles: Role) {
    val userId = UUID.randomUUID()
    val userRecord = newBrgUserRecord(
      id = userId,
      email = email,
      password = "password"
    )
    val roleRecords = roles.map {
      newUserRolesRecord(
        userId = userId,
        role = it
      )
    }
    jooq.executeInsert(userRecord)
    jooq.batchInsert(roleRecords)
  }
}