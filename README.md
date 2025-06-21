# Minecraft Scaleway Frontend

Fake Minecraft Server used as a frontend to hourly paid Scaleway servers

## Usage

CLI:
```bash
java -jar server.jar \
  --zone scaleway-zone \
  --server server-uuid \
  --api-key your-api-key \
  --minecraft-ip ip-of-minecraft-server
```

You can also pass `--port uint` (default: 25565) to set the port of the server or `--minecraft-port` to set the port
of the Minecraft server (default: 25565).

## Technology

- Minestom
- Kotlin
- Gradle (Kotlin DSL)
- kotlinx-serialization-json
- kotlinx-coroutine-core