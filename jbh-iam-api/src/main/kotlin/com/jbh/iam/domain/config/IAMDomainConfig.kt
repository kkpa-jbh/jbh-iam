package com.jbh.iam.domain.config

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories(basePackages = ["com.jbh.iam.domain"])
@EntityScan(basePackages = ["com.jbh.iam.domain"])
class IAMDomainConfig {
}