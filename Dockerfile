# Étape 1 : Build de l'application avec Maven
FROM maven:3.8-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# Étape 2 : Exécution de l'application (Utilisation d'Eclipse Temurin, très stable)
FROM eclipse-temurin:17-jdk-alpine
COPY --from=build /target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]