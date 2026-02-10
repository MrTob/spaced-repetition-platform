# ── Build stage: compile native image ──
FROM ghcr.io/graalvm/graalvm-community:25 AS build
WORKDIR /app
RUN microdnf install -y findutils
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw && ./mvnw dependency:resolve -B
COPY src src
RUN ./mvnw -Pnative native:compile -DskipTests -B

# ── Runtime stage: minimal image ──
FROM debian:bookworm-slim
RUN apt-get update && apt-get install -y --no-install-recommends libc6 && rm -rf /var/lib/apt/lists/*
COPY --from=build /app/target/spaced-repetition-service /app/srs
EXPOSE 8080
ENTRYPOINT ["/app/srs"]