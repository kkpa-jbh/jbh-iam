# jbh-iam

Modular monolith for handling IAM in JBH.

This project is designed to centralize authentication, authorization, and user management for JBH applications.

It provides APIs, security, and a gateway for handling IAM concerns in a modular, maintainable way, using Kotlin, Spring Boot, and Gradle.

The structure supports code reuse, separation of concerns, and scalability.

## Modules

jbh-iam-api: Contains API endpoints, controllers, DTOs, and error handling for the IAM service. Handles HTTP requests and responses.
jbh-iam-common: Shared code and utilities (e.g., config, extensions, logging, payloads) used across other modules.
jbh-iam-core: Core business logic, domain models, and security/environment abstractions for IAM.
jbh-iam-security: Handles authentication and security concerns, likely providing authentication mechanisms and security filters.
jbh-iam-view-gateway: Acts as a gateway or frontend for the IAM system, possibly serving static resources, templates, and acting as an entry point for web clients.

# How to use it?

🌐 Server Configuration:
- Port: 9999 (configurable via INSTANCE_PORT env var)
- Context Path: /jbh (configurable via INSTANCE_CONTEXT_PATH env var)
- Base URL: http://localhost:9999/jbh

📄 Available HTML Pages:
1. Login Page: /view/auth/login
   - Full URL: http://localhost:9999/jbh/view/auth/login
   - Template: auth/login.html
2. Register Page: /view/auth/register
   - Full URL: http://localhost:9999/jbh/view/auth/register
   - Template: auth/register.html
3. Test Page: /test (likely)
   - Full URL: http://localhost:9999/jbh/test
   - Template: test.html

🔧 Additional Endpoints:
- Username availability check: /view/auth/check-username?username=...
- Email availability check: /view/auth/check-email?email=...
- Login POST: /view/auth/signin
- Register POST: /view/auth/register

🚀 To access your app:
Start the application and navigate to http://localhost:9999/jbh/view/auth/login for the login page.