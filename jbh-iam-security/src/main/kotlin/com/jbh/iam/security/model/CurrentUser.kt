package com.jbh.iam.security.model

import org.springframework.security.core.annotation.AuthenticationPrincipal
import java.lang.annotation.Documented

/**
 * Spring security provides an annotation called @AuthenticationPrincipal to access the currently authenticated user in the controllers.

The following CurrentUser annotation is a wrapper around @AuthenticationPrincipal annotation.

We’ve created a meta-annotation so that we don’t get too much tied up of with Spring Security related annotations everywhere in our project.
This reduces the dependency on Spring Security. So if we decide to remove Spring Security from our project,
we can easily do it by simply changing the CurrentUser annotation-<


You can use this annotation to easily inject the currently authenticated user
into your controller methods or service methods. Here's an example:

@GetMapping("/users/{id}")
public User getUser(@CurrentUser UserDetails user) {
return user;
}

Spring Security will automatically resolve the @CurrentUser annotation and
inject the currently authenticated user's UserDetails object.

 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Documented
@AuthenticationPrincipal
annotation class CurrentUser