FROM gradle:8-jdk21-alpine as builder

WORKDIR /app

COPY . .

RUN gradle build

FROM eclipse-temurin:21-alpine

WORKDIR /app

COPY --from=builder ./build/libs/*.jar .

ENV PORT=25565
ENV ZONE="fr-par-1"
ENV SERVER=""
ENV API_KEY=""
ENV MINECRAFT_HOST=""
ENV MINECRAFT_PORT=25565
ENV SERVER_NAME="Minecraft Scaleway Frontend"
ENV WHITELIST=""

CMD [
    "java", "-jar", "*.jar",
    "--port", "$PORT",
     "--zone", "$ZONE",
     "--server", "$SERVER",
     "--minecraft-host", "$MINECRAFT_HOST",
     "--minecraft-port", "$MINECRAFT_PORT",
     "--server-name", "$SERVER_NAME",
     "--whitelist", "$WHITELIST"
]