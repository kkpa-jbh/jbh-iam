dependencies {
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    
    implementation(project(":jbh-iam-common"))
    implementation(project(":jbh-iam-core"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    // Versions come from the Spring Boot BOM. Never pin Spring Security by hand.
    implementation("org.springframework.boot:spring-boot-starter-security")
    // Same jjwt version as jbh-gateway, which checks the tokens this module signs.
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
    // Add other security-related dependencies
}