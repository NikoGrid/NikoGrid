FROM maven:3.9-eclipse-temurin-21 as builder

# Package the application
RUN mkdir -p /opt/app
ADD . /opt/app
RUN --mount=type=cache,target=/root/.m2 mvn -f /opt/app/pom.xml \
	-Dmaven.test.skip -Pnative clean package

WORKDIR /builder
# Copy the jar file to the working directory and rename it to application.jar
RUN cp /opt/app/target/*.jar application.jar
# Extract the jar file using an efficient layout
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted



# This is the runtime container
FROM eclipse-temurin:21-jre-alpine

LABEL org.opencontainers.image.source=https://github.com/NikoGrid/NikoGrid

# Install curl for healtchecks
RUN apk --no-cache add curl

WORKDIR /application
# Copy the extracted jar contents from the builder container into the working directory in the runtime container
# Every copy step creates a new docker layer
# This allows docker to only pull the changes it really needs
COPY --from=builder /builder/extracted/dependencies/ ./
COPY --from=builder /builder/extracted/spring-boot-loader/ ./
COPY --from=builder /builder/extracted/snapshot-dependencies/ ./
COPY --from=builder /builder/extracted/application/ ./
# Execute the CDS training run
RUN java -Dspring.aot.enabled=true -Dspring.context.exit=onRefresh \
	# Disable flyway migrations during CDS training because no database is available,
	# also hardcode the database dialect since the database can't be queried for it.
	-Dspring.flyway.enabled=false -Dspring.jpa.database=postgresql \
	-XX:ArchiveClassesAtExit=application.jsa \
	-jar application.jar

COPY ./docker/entrypoint.sh ./
ENTRYPOINT ["./entrypoint.sh"]

# vi: ft=dockerfile
