# --- build stage ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
# tests run in CI, not in the image build
RUN mvn -B -q package -DskipTests

# --- runtime stage ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/simulator.jar simulator.jar
ENTRYPOINT ["java", "-jar", "simulator.jar"]
