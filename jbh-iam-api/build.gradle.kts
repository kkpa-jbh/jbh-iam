// BootJar task error:
//This error occurs because
// for the jbh-iam-api module, but it can't find a main class.
// Since jbh-iam-api is not your main application module, you don't need to create a bootJar for it.
tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}

tasks.getByName<Jar>("jar") {
    enabled = true
}

dependencies {
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    implementation(project(":jbh-iam-common"))
    implementation(project(":jbh-iam-core"))


    implementation("org.liquibase:liquibase-core")
    // Actuator to check which endpoints are available
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Spring Boot Starter Validation (includes both the Bean Validation API and Hibernate Validator)
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.hibernate:hibernate-core:6.1.7.Final")
    // Add other API-related dependencies

    implementation("org.postgresql:postgresql:${property("postgresqlVersion")}")

}