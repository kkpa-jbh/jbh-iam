// Spring Cloud 2025.0 is the release train for Spring Boot 3.5.
extra["springCloudVersion"] = "2025.0.0"


plugins {
    kotlin("jvm") version "1.9.25" apply false
    kotlin("plugin.spring") version "1.9.25" apply false
    id("org.springframework.boot") version "3.5.16" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("com.vanniktech.dependency.graph.generator") version "0.8.0" // To check conflicts between dependencies ./gradlew generateDependencyGraph
    java
}


allprojects {
    group = "com.kkpa"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenLocal()
        mavenCentral()
        // CI only: jbh-gateway-client and jbh-notification-contracts come from GitHub Packages.
        // They belong to other repos, so GITHUB_TOKEN cannot read them: CI passes a classic PAT
        // with read:packages (repo secret PACKAGES_READ_TOKEN). Local builds use ~/.m2 instead.
        System.getenv("PACKAGES_READ_TOKEN")?.let { token ->
            listOf("jbh-gateway-client", "jbh-personal-finance").forEach { repo ->
                maven {
                    url = uri("https://maven.pkg.github.com/kkpa-jbh/$repo")
                    credentials {
                        username = System.getenv("GITHUB_ACTOR")
                        password = token
                    }
                }
            }
        }
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "java")

    java {
        sourceCompatibility = JavaVersion.VERSION_17
    }

    // Disable bootJar and enable jar for all subprojects
    // The standalone application is `view-gateway` module.

    //Gradle is trying to create a bootJar (executable JAR) for every subproject.
    tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
        enabled = false
    }

    // Enable jar for all subprojects (To Share submodules as a library)
    tasks.getByName<Jar>("jar") {
        enabled = true
    }

    dependencies {
        implementation("org.apache.httpcomponents.client5:httpclient5") // version from the Spring Boot BOM
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.springframework.boot:spring-boot-starter")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
    }


    // To apply the dependency management plugin/block to all subprojects
    the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().apply {
        imports {
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs += "-Xjsr305=strict"
            jvmTarget = "17"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}