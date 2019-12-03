package com.geowarin.jooqgraphql.utils

import org.testcontainers.containers.PostgreSQLContainer

object Containers {
  class PgContainer : PostgreSQLContainer<PgContainer>("postgres:10-alpine")

  val pgContainer: PgContainer

  init {
    pgContainer = PgContainer()
    pgContainer.start()
  }
}
