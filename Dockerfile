FROM gradle:8-jdk21-alpine as builder

WORKDIR /app

COPY . .

RUN gradle build

FROM eclipse-temurin:21-alpine

LABEL org.opencontainers.image.source="https://github.com/architects-land/minecraft-scaleway-frontend"
LABEL org.opencontainers.image.description="Fake Minecraft Server used as a frontend to hourly paid Scaleway servers."
LABEL org.opencontainers.image.licenses="AGPL-3.0-only"

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar .

ENV PORT=25565
ENV ZONE="fr-par-1"
ENV SERVER=""
ENV API_KEY=""
ENV MINECRAFT_HOST=""
ENV MINECRAFT_PORT=25565
ENV SERVER_NAME="Minecraft Scaleway Frontend"
ENV WHITELIST=""

CMD exec java -jar *.jar \
    --port "$PORT" \
    --zone "$ZONE" \
    --server "$SERVER" \
    --minecraft-host "$MINECRAFT_PORT" \
    --minecraft-port "$MINECRAFT_PORT" \
    --server-name "$SERVER_NAME" \
    --whitelist "$WHITELIST"
