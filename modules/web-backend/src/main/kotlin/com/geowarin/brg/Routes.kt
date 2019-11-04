package com.geowarin.brg

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.web.reactive.function.server.router

@Configuration
class Routes {

  @Bean
  fun routesFun() = router {
    GET("/", serveStatic(ClassPathResource("/graphiql.html")))
  }
}