package com.geowarin.jooqgraphql

import org.jooq.*
import org.jooq.impl.DSL
import org.jooq.impl.Internal
import org.jooq.impl.Internal.createUniqueKey

fun Table<Record>.createKey(vararg field: TableField<Record, *>): UniqueKey<Record> =
  createUniqueKey(this, *field)

fun Table<Record>.createFks(vararg fks: Pair<UniqueKey<Record>, TableField<Record, *>>): List<ForeignKey<Record, Record>> =
  fks.map { createFk(it.first, it.second) }

fun Table<Record>.createFk(key: UniqueKey<Record>, field: TableField<Record, *>): ForeignKey<Record, Record> =
  Internal.createForeignKey<Record, Record>(key, this, "fk_${key.name}", field)
