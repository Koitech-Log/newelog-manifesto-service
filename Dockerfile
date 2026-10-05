# =========================
# BUILD
# =========================
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY src src

RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# =========================
# RUNTIME
# =========================
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ENV PORT=8081

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]