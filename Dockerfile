# Runtime image for jbh-iam.
# CI builds the Spring Boot jar first (./gradlew :jbh-iam-api:bootJar), so this image only copies it.
# Java 21 runtime: jbh-gateway-client is compiled for Java 21.
FROM eclipse-temurin:21-jre-alpine

# A fixed numeric user and group (10001). Kubernetes "runAsNonRoot" can only check a number:
# with a user name (USER jbh) the pod fails with "image has non-numeric user".
RUN addgroup -S -g 10001 jbh && adduser -S -u 10001 -G jbh jbh
WORKDIR /app

COPY --chown=10001:10001 jbh-iam-api/build/libs/*.jar /app/app.jar

USER 10001:10001
EXPOSE 9999

# JAVA_OPTS carries the heap cap (-Xmx) from compose. exec keeps java as PID 1 for clean shutdown.
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
