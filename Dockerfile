# Runtime image for jbh-iam.
# CI builds the Spring Boot jar first (./gradlew :jbh-iam-api:bootJar), so this image only copies it.
# Java 21 runtime: jbh-gateway-client is compiled for Java 21.
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S jbh && adduser -S jbh -G jbh
WORKDIR /app

COPY --chown=jbh:jbh jbh-iam-api/build/libs/*.jar /app/app.jar

USER jbh
EXPOSE 9999

# JAVA_OPTS carries the heap cap (-Xmx) from compose. exec keeps java as PID 1 for clean shutdown.
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
