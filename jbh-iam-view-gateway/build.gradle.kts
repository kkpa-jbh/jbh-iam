// Enables the use of the bootJar task.
// This executable JAR includes your application code, all its dependencies, and an embedded server (like Tomcat) if you're using spring-boot-starter-web.
tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    mainClass.set("com.jbh.iam.viewgateway.JBHIAMApplication")
}

dependencies {
    implementation(project(":jbh-iam-common"))
    implementation(project(":jbh-iam-api"))
    implementation(project(":jbh-iam-security"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.apache.httpcomponents.client5:httpclient5")
    implementation("org.springframework.cloud:spring-cloud-starter-consul-discovery")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    developmentOnly("org.springframework.boot:spring-boot-devtools")

//implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    // Add HTMX dependency (you may need to find the appropriate Maven/Gradle dependency)
    // Add other view gateway-related dependencies
}