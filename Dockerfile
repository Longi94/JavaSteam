# JavaSteam Development Container
# Provides a consistent environment with Java 11 and all build tools

FROM eclipse-temurin:11-jdk

# Install system dependencies
RUN apt-get update && apt-get install -y \
    git \
    curl \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /workspace

# Copy gradle wrapper and build files first (for better caching)
COPY gradlew gradlew.bat ./
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle/libs.versions.toml ./gradle/

# Copy buildSrc
COPY buildSrc ./buildSrc

# Download dependencies (cached layer)
RUN ./gradlew --no-daemon dependencies || true

# Copy source code
COPY src ./src
COPY javasteam-samples ./javasteam-samples
COPY javasteam-cs ./javasteam-cs
COPY javasteam-tf ./javasteam-tf
COPY javasteam-dota2 ./javasteam-dota2
COPY javasteam-deadlock ./javasteam-deadlock

# Build the project (generates proto and steamlanguage classes)
RUN ./gradlew build -x test -x signMavenJavaPublication --no-daemon

# Keep container running for development
CMD ["tail", "-f", "/dev/null"]
