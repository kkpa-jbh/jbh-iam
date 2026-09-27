// This is now the main application module
// Enable bootJar to create an executable JAR
tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = true
    // Kotlin compiles the top-level `fun main` in JBHIAMApplication.kt into the class JBHIAMApplicationKt.
    mainClass.set("com.jbh.iam.api.JBHIAMApplicationKt")
}

// Disable regular jar since we're creating a bootJar
tasks.getByName<Jar>("jar") {
    enabled = false
}

dependencies {
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Internal module dependencies
    implementation(project(":jbh-iam-common"))
    implementation(project(":jbh-iam-core"))
    implementation(project(":jbh-iam-security"))
    implementation(project(":jbh-iam-cache"))

    // JBH Modules
    implementation("com.jbh:jbh-gateway-client:1.0.0-SNAPSHOT")

    // Database and migration
    implementation("org.liquibase:liquibase-core")
    implementation("org.postgresql:postgresql:${property("postgresqlVersion")}")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // Web and API
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")


    // Service discovery
    implementation("org.springframework.cloud:spring-cloud-starter-consul-discovery")

    // HTTP Client
    implementation("org.apache.httpcomponents.client5:httpclient5")

    // Google sign-in: verifies Google ID tokens against Google's public keys
    implementation("com.google.api-client:google-api-client:2.8.0")
}