# Use an official OpenJDK runtime as a parent image
#FROM maven:3.8.5-openjdk-17

# Set the working directory in the container
#WORKDIR /app

# Copy the application JAR file into the container at /app
#COPY target/TodoList-0.0.1-SNAPSHOT.jar /app/TodoList-0.0.1-SNAPSHOT.jar

# Expose the port the app runs on
#EXPOSE 8080

# Run the application when the container starts
#ENTRYPOINT ["java", "-jar", "TodoList-0.0.1-SNAPSHOT.jar"]


# ---------- Build stage ----------
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

# Build the jar (tests optional - you can remove -DskipTests if you want)
RUN mvn -B clean package -DskipTests


# ---------- Run stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
