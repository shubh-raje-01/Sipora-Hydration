# ═══════════════════════════════════════════════════════════════════════
# Sipora Hydration — Multi-Stage Dockerfile
# ═══════════════════════════════════════════════════════════════════════
#
# Stage 1 (builder): Maven + full JDK — compiles the JAR
# Stage 2 (runtime): JRE 21 slim     — runs the JAR
#
# Final image size: ~250MB (vs ~700MB with a full JDK image)
#
# Layered JAR is enabled in pom.xml (spring-boot-maven-plugin).
# Docker layer order (slowest-changing → fastest-changing):
#   1. JRE base image
#   2. Spring Boot loader + dependencies (changes rarely)
#   3. Snapshot dependencies  (changes occasionally)
#   4. Application code       (changes every commit)
# This means layer 1–2 are almost always cache hits on rebuild.
#
# Build:   docker build -t sipora-backend .
# Run:     docker run -p 8080:8080 --env-file .env sipora-backend
# ═══════════════════════════════════════════════════════════════════════

# ─────────────────────────────────────────────────────────────────────
# STAGE 1 — Builder
# Uses the official Eclipse Temurin JDK 21 image with Maven pre-installed
# ─────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /build

# Copy Maven wrapper and pom first — allows Docker to cache
# the dependency download layer separately from the source code.
# Dependencies are only re-downloaded when pom.xml changes.
COPY mvnw .
COPY .mvn/ .mvn/
COPY pom.xml .

# Download all dependencies (cache this layer)
RUN chmod +x mvnw && \
    ./mvnw dependency:go-offline -B -q

# Now copy source code (this layer rebuilds on every code change)
COPY src/ src/

# Build the layered JAR, skip tests (tests run in CI separately)
RUN ./mvnw package -B -q -DskipTests && \
    java -Djarmode=layertools -jar target/*.jar extract --destination target/extracted

# ─────────────────────────────────────────────────────────────────────
# STAGE 2 — Runtime
# Lean JRE-only image — no compiler, no Maven, no build tools
# ─────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre AS runtime

WORKDIR /app

# Non-root user for security — never run as root in production
RUN groupadd --gid 1001 sipora && \
    useradd --uid 1001 --gid sipora --no-create-home --shell /bin/false sipora

# Copy layered JAR layers from builder (slowest-changing first for cache efficiency)
COPY --from=builder --chown=sipora:sipora /build/target/extracted/dependencies/          ./
COPY --from=builder --chown=sipora:sipora /build/target/extracted/spring-boot-loader/    ./
COPY --from=builder --chown=sipora:sipora /build/target/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=sipora:sipora /build/target/extracted/application/           ./

# Create log directory
RUN mkdir -p /var/log/sipora && chown sipora:sipora /var/log/sipora

USER sipora

# Expose the application port
EXPOSE 8080

# Health check — uses the Actuator health endpoint
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -sf http://localhost:8080/actuator/health || exit 1

# JVM flags:
#  -XX:+UseContainerSupport       — respect Docker CPU/memory limits
#  -XX:MaxRAMPercentage=75.0      — use 75% of container RAM for heap
#  -XX:+UseG1GC                   — G1 GC is best for web applications
#  -Djava.security.egd=...        — faster SecureRandom startup for JWT
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+UseG1GC", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod}", \
    "org.springframework.boot.loader.launch.JarLauncher"]