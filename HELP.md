# Just Be Honest (JBH) 2.0 with Kotlin

JBH-2.0[Kotlin/SpringBoot] is the backend component of a self-managed personal finance tracking software. The idea of this personal project is to offer me a comprehensive overview of my financial landscape by allowing manual input and management of financial account data, transfers, and expenses. With a focus on user-driven control, 'Just Be Honest' empowers me to take charge of my financial well-being.

## History

I initiated the development of a monolithic application at https://github.com/ccpena/JBH, employing Java for the backend and Angular for the frontend.

Recognizing the benefits of modularization, I made the decision to decompose the application into distinct front-end and back-end components.
Transitioning to `Kotlin` as the primary language for the backend and adopting `React` as the frontend library, this evolution allowed for greater flexibility and maintainability.

Throughout this process, I have actively explored and utilized different programming languages, contributing to the enhancement of my software language knowledge and practical skills.
As the next step in the evolution of this project, I am planning to create a standalone microservice.
The aim is to leverage the strengths of Go or Python for this specific microservice, enhancing the overall scalability and performance of the system.




## How to Debug?

1. Add VM Options to IDE:
```
-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:6666
```

2. Create Debug Configuration using the same port



# DB Config

```sql
CREATE ROLE jbh_admin;
ALTER ROLE jbh_admin WITH PASSWORD ':password';
ALTER ROLE jbh_admin CREATEDB;
ALTER ROLE jbh_admin CREATEROLE;
ALTER ROLE jbh_admin LOGIN;
CREATE DATABASE "JBH" WITH OWNER = jbh_admin ENCODING = 'UTF8' LC_COLLATE = 'en_US.UTF-8' LC_CTYPE = 'en_US.UTF-8' TABLESPACE = pg_default CONNECTION LIMIT = -1;
GRANT ALL PRIVILEGES ON DATABASE "JBH" TO jbh_admin;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA "public" TO jbh_admin;
GRANT CONNECT ON DATABASE "JBH" TO jbh_admin;
GRANT ALL PRIVILEGES ON SCHEMA "public" TO jbh_admin;
ALTER DATABASE "JBH" SET TIME ZONE 'America/Bogota';
CREATE EXTENSION pgcrypto; // In JBH
SELECT pg_reload_conf() // with postgres to reload changes made to role;
```
# Migration

I decided to migrate the project:

```java
 java = '1.8' to '17'
 kotlinVersion = '1.3.20' to '1.9.20'
 springBootVersion = '2.1.2.RELEASE' to '3.2.0' 
 gradle = '4.9' to '8.5'
```


1. https://lastjavabuilder.medium.com/step-by-step-migration-to-spring-3-x-jakarta-security-hibernate-6-openapi-cache-925401fa7b2c
2. https://github.com/spring-projects/spring-framework/wiki/Upgrading-to-Spring-Framework-6.x

## Spring Security 6.x 

Spring Security allows customizing HTTP security for features, such as endpoints authorization or the authentication manager configuration, by extending a WebSecurityConfigurerAdapter class. However, in recent versions, Spring deprecates this approach and encourages a component-based security configuration.

With previous version I had configured:
* *UserDetailsService*: Via AuthenticationManagerBuilder - Allow to load principal object by userNameorEmail or byId.
* *AuthenticationManager*: Bean registration.
* *HttpSecurity*: To set
  * cors
  * AuthenticationEntryPoint
  * SessionCreationPolicy (Stateless)
  * Static matchers (.html, images types, css, js)
  * White list
  * Persistent login via cookie or redis
  * Custom JWT security filter
  * @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")

###  Compile With -parameters

Spring Framework 6.1 removes LocalVariableTableParameterNameDiscoverer. This affects how @PreAuthorize and other method security annotations will process parameter names. If you are using method security annotations with parameter names:

You must compile with -parameters to ensure that the parameter names are available at runtime.

```xml
tasks.withType<JavaCompile>(){
	options.compilerArgs.add("-parameters")
}

<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <configuration>
  <parameters>true</parameters>
  </configuration>
</plugin>
```

### WebSecurityConfigurerAdapter is deprecated
https://www.baeldung.com/spring-deprecated-websecurityconfigureradapter

With Spring 3.x, there have been some radical changes in security definitions. The adapter structure has been completely removed. In the previous version, configuration definitions were made by extending the **WebSecurityConfigurerAdapter** class, but with Spring 3.x, the WebSecurityConfigurerAdapter class structure was removed.Instead, you can configure the filterChain Bean definition.

*Spring Security 6 provides a default AuthenticationManager bean if you don't define your own. This is sufficient for most basic applications. You don't need to explicitly define the bean in your configuration.* 

#### Fixed 
* Field authenticationManager in required a bean of type 'org.springframework.security.authentication.AuthenticationManager' that could not be found.
  The injection point has the following annotations:
  - @org.springframework.beans.factory.annotation.Autowired(required=true)
*   It was deprecated for Spring 6.2: https://docs.spring.io/spring-security/reference/migration/index.html
*   I added `implementation("org.springframework.security:spring-security-web:6.2.0")`
  * I had to configure authorized request


## Swagger has changed: I had to remove this

I added the following dependency: `implementation("org.springdoc:springdoc-openapi-security:1.6.15") //swagger`

### Jakarta

javax is deprecated. Now, you have to use `jakarta`.
* javax.persistence renamed to jakarta.persistence:
  * Standardization: Jakarta EE provides a standardized set of APIs for various enterprise functionalities, including transactions. Using the Jakarta API ensures consistency and compatibility across different platforms and frameworks.
  * Future-proofing: Migrating to the Jakarta API now ensures your code is compatible with future versions of Spring and other Jakarta EE-compliant frameworks.
  * Modularization: The Jakarta EE APIs are modularized, allowing you to choose and use only the components you need, potentially reducing dependencies and improving code clarity.
  
The usage of import javax.transaction.Transactional has been deprecated and replaced with the jakarta.transaction.Transactional annotation. This change aligns with the overall shift towards the Jakarta EE platform and its APIs.

* If you are using Hibernate in your project, it is recommended to use Hibernate minimum 6.x version together with SpringBoot 3.x; I added `implementation "org.hibernate:hibernate-core:6.1.7.Final"` now it is `import jakarta.persistence.Column`

* Kotlin Plugin - Intellij: I re-installed the plugin 
* Consider defining a bean of type 'javax.validation.Validator' in your configuration.
  * I added implementation("org.springframework.boot:spring-boot-starter-validation")
  * I refactored the code in order to use the Validator interface from Spring.

* No more pattern data allowed after {*...} or ** pattern element
   Solved by: `spring.mvc.pathmatch.matching-strategy=ANT_PATH_MATCHER`



### Reference Documentation
For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/3.2.0/gradle-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/3.2.0/gradle-plugin/reference/html/#build-image)
* [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/3.2.0/reference/htmlsingle/index.html#actuator)
* [Spring Web](https://docs.spring.io/spring-boot/docs/3.2.0/reference/htmlsingle/index.html#web)
* [Spring Security](https://docs.spring.io/spring-boot/docs/3.2.0/reference/htmlsingle/index.html#web.security)
* [Spring Data JPA](https://docs.spring.io/spring-boot/docs/3.2.0/reference/htmlsingle/index.html#data.sql.jpa-and-spring-data)
* [Spring Boot DevTools](https://docs.spring.io/spring-boot/docs/3.2.0/reference/htmlsingle/index.html#using.devtools)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service with Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/)
* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Securing a Web Application](https://spring.io/guides/gs/securing-web/)
* [Spring Boot and OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2/)
* [Authenticating a User with LDAP](https://spring.io/guides/gs/authenticating-ldap/)
* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)

### Additional Links
These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)



# Just Be Honest (JBH) 2.0 with Kotlin

JBH-2.0[Kotlin/SpringBoot] is the backend component of a self-managed personal finance tracking software. The idea of this personal project is to offer me a comprehensive overview of my financial landscape by allowing manual input and management of financial account data, transfers, and expenses. With a focus on user-driven control, 'Just Be Honest' empowers me to take charge of my financial well-being.

## History

I initiated the development of a monolithic application at https://github.com/ccpena/JBH, employing Java for the backend and Angular for the frontend.

Recognizing the benefits of modularization, I made the decision to decompose the application into distinct front-end and back-end components.
Transitioning to `Kotlin` as the primary language for the backend and adopting `React` as the frontend library, this evolution allowed for greater flexibility and maintainability.

Throughout this process, I have actively explored and utilized different programming languages, contributing to the enhancement of my software language knowledge and practical skills.
As the next step in the evolution of this project, I am planning to create a standalone microservice.
The aim is to leverage the strengths of Go or Python for this specific microservice, enhancing the overall scalability and performance of the system.


## Swagger

http://localhost:9999/jbh/swagger-ui.html

# Database Migration

## Create DB

```
CREATE DATABASE "JBH" WITH OWNER = jbh_admin ENCODING = 'UTF8' LC_COLLATE = 'en_US.UTF-8' LC_CTYPE = 'en_US.UTF-8' TABLESPACE = pg_default CONNECTION LIMIT = -1;
```
## Create extension

```
CREATE EXTENSION pgcrypto;
```
## Set global variables

```
ALTER DATABASE "JBH" SET TIME ZONE 'America/Bogota';
```

## Remove All Data

```
-- Remove All Data
DELETE FROM transactions;
DELETE FROM accounts;
DELETE FROM sub_categories;
DELETE FROM categories;
DELETE FROM users_group;
DELETE FROM user_roles;
DELETE FROM jbh_user;
```