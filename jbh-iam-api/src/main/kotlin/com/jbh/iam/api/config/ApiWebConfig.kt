package com.jbh.iam.api.config

import org.springframework.web.servlet.config.annotation.PathMatchConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

//@Configuration
class ApiWebConfig : WebMvcConfigurer {
    override fun configurePathMatch(configurer: PathMatchConfigurer) {
        //configurer.addPathPrefix(Routes.IAM_BASE_PATH, HandlerTypePredicate.forAnnotation(RestController::class.java))
    }
}