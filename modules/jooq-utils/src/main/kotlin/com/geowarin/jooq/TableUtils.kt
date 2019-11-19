package com.geowarin.jooq

import org.jooq.*
import org.jooq.impl.Internal
import org.jooq.impl.Internal.createUniqueKey

fun Table<Record>.createKey(vararg field: TableField<Record, *>): UniqueKey<Record> =
  createUniqueKey(this, "pk_${this.name}${field.joinToString {  "_" + it.name }}", *field)

fun Table<Record>.createFks(vararg fks: Pair<UniqueKey<Record>, TableField<Record, *>>): List<ForeignKey<Record, Record>> =
  fks.map { createFk(it.first, it.second) }

fun Table<Record>.createFk(key: UniqueKey<Record>, field: TableField<Record, *>): ForeignKey<Record, Record> =
  Internal.createForeignKey(key, this, "fk_${key.name.removePrefix("pk")}", field)
