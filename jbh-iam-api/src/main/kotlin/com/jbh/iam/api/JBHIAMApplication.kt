package com.jbh.iam.api

import com.jbh.iam.domain.config.IAMDomainConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import
import java.util.*

@SpringBootApplication(scanBasePackages = ["com.jbh.iam", "com.jbh.iam.api"])
@Import(IAMDomainConfig::class)
class JBHIAMApplication

fun main(args: Array<String>) {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    runApplication<JBHIAMApplication>(*args)
}
