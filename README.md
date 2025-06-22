# Minecraft Scaleway Frontend

Fake Minecraft Server used as a frontend to hourly paid Scaleway servers.

Automatically start the server if a player is waiting.
If the server is started, transfer the player to the server.
If the server is `stopped in place` (powered off, but still in Scaleway's hypervisor), remove it from Scaleway's hypervisor.

## Usage

Logs are in `logs/`.

The current logs are in `latest.log`.
This file is compressed with GZip when the program is stopped.
Its new name is `yyyy-MM-dd HH:mm.log.gz` (program launch date).

### CLI
```bash
java -jar server.jar \
  --zone scaleway-zone \
  --server server-uuid \
  --api-key your-api-key \
  --minecraft-host ip-of-minecraft-server
```

You can also pass `--port uint` (default: 25565) to set the port of the server or `--minecraft-port` to set the port
of the Minecraft server (default: 25565).

You can modify the server name with `--server-name string` (escape space or use quotes if your string contains space).

You specify a whitelist with `--whitelist`.
Separate each user with a coma (`,`).
You can use their Minecraft's username or their UUID, e.g.: `anhgelus,ascpial`, `anhgelus,3f6ddb7c-f214-48a9-9f4a-eb22b9cf53f0`

### Docker

You can use the official Docker image `ghcr.io/architects-land/minecraft-scaleway-frontend`.

Tags:
- `latest` is always the latest one
- `main` is for the main branch
- `v*` is for a specific tag (e.g., `v1.0.0`)

Environments:
- `PORT` is the server's port
- `ZONE` is the Scaleway's zone
- `SERVER` is the UUID of the Scaleway's server
- `API_KEY` is your Scaleway's API key
- `MINECRAFT_HOST` is the host of your Minecraft server (will be used during the transfer)
- `MINECRAFT_PORT` is the port of your Minecraft server
- `SERVER_NAME` is the name of this server (shown in the debug screen)
- `WHITELIST` is the whitelist (same syntax of the CLI's arg)

To save the logs, bind a volume to `/app/logs`.

## Technology

- Minestom
- Kotlin
- Gradle (Kotlin DSL)
- kotlinx-serialization-json
- kotlinx-coroutine-core
- Log4J 2
- lenni0451's MCPing