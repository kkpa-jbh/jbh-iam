# jbh-iam

Modular monolith for handling IAM in JBH.

This project is designed to centralize authentication, authorization, and user management for JBH applications. It provides APIs, security, and a gateway for handling IAM concerns in a modular, maintainable way, using Kotlin, Spring Boot, and Gradle. The structure supports code reuse, separation of concerns, and scalability.

## Modules

jbh-iam-api: Contains API endpoints, controllers, DTOs, and error handling for the IAM service. Handles HTTP requests and responses.
jbh-iam-common: Shared code and utilities (e.g., config, extensions, logging, payloads) used across other modules.
jbh-iam-core: Core business logic, domain models, and security/environment abstractions for IAM.
jbh-iam-security: Handles authentication and security concerns, likely providing authentication mechanisms and security filters.
jbh-iam-view-gateway: Acts as a gateway or frontend for the IAM system, possibly serving static resources, templates, and acting as an entry point for web clients.

## Suggestions

Suggestions:
a. Consistent Naming: Stick to a consistent naming convention across all your applications. Your proposed structure (
/jbh-{module}/{module}-api/) is good.
b. API Gateway: Implement an API Gateway as you've mentioned. This will handle routing, can provide a single entry point
for authentication, and can help with cross-cutting concerns like rate limiting.
c. Shared Libraries: For code that's common across your monoliths, consider creating shared libraries.
d. Database Strategy: Carefully consider your database strategy. Shared databases can simplify data consistency but may
limit independence. Separate databases offer more independence but require careful management of data consistency.
e. Service Communication: Define clear interfaces for inter-service communication. Consider using REST or gRPC for
synchronous communication, and message queues for asynchronous communication.
f. Authentication/Authorization: Implement a centralized auth service (your IAM could serve this purpose) that other
services can use.
g. Monitoring and Logging: Implement centralized logging and monitoring from the start. It becomes crucial as you add
more services.
h. Documentation: Maintain clear API documentation for each service. Consider using tools like Swagger/OpenAPI.
i. Containerization: Consider containerizing your applications (e.g., using Docker) to ensure consistency across
environments and to prepare for potential future scaling.
j. Feature Flags: Implement feature flags to enable/disable features across your distributed system easily.