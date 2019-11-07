package com.geowarin.brg

import com.geowarin.services.user.UserService
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication(scanBasePackageClasses = [UserService::class])
internal class ServiceConfig