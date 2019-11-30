package com.geowarin.brg

import com.geowarin.model.forum_example.Keys
import com.geowarin.model.forum_example.Tables.PERSON
import com.geowarin.model.forum_example.Tables.POST
import com.geowarin.model.forum_example.tables.records.PersonRecord
import com.geowarin.model.forum_example.tables.records.PostRecord
import org.jooq.ForeignKey
import org.jooq.Result
import org.jooq.TableRecord
import org.jooq.UpdatableRecord
import org.jooq.impl.DSL
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BrgApplication

fun main(args: Array<String>) {
  runApplication<BrgApplication>(*args)
//
//  val toto: TableRecord<*> = PostRecord()
//
//  val result: org.jooq.Result<PostRecord> = DSL.selectFrom(POST).fetch()
////  val result: org.jooq.Result<out UpdatableRecord<*>> = DSL.selectFrom(POST).fetch()
//
////  val groups = result.intoGroups(Keys.POST__POST_AUTHOR_ID_FKEY.fieldsArray)
//  val authorIdFkey: ForeignKey<PostRecord, out UpdatableRecord<*>> = Keys.POST__POST_AUTHOR_ID_FKEY
//  val parents = result.fetchParents(authorIdFkey)
//
//
//  val personResult = DSL.selectFrom(PERSON).fetch()
//  val children = personResult.fetchChildren(Keys.POST__POST_AUTHOR_ID_FKEY)
}
