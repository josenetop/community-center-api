FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .

# Baixa as dependências do Maven para um cache local no contêiner.
RUN mvn dependency:go-offline -B

COPY src src

RUN mvn clean install -DskipTests -B

FROM eclipse-temurin:21-jre-jammy AS runner

WORKDIR /app

ARG JAR_FILE_NAME=community-center-api-0.0.1-SNAPSHOT.jar

ENV JAR_FILE=$JAR_FILE_NAME

COPY --from=builder /app/target/$JAR_FILE_NAME .

EXPOSE 8080
ENTRYPOINT ["/bin/sh", "-c", "java -jar $JAR_FILE"]