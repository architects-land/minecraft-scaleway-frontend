# Minecraft Scaleway Frontend

Fake Minecraft Server used as a frontend to hourly paid Scaleway servers.

It automatically starts the server if a player is waiting.
If the server is started, it transfers the player to the server.
If the server is `stopped in place` (powered off, but still in Scaleway's hypervisor), it removes it from Scaleway's hypervisor.

## Usage

A complete documentation is hosted [here](https://architects-land.github.io/minecraft-scaleway-frontend/)

Vocabulary:
- "server" for this software
- "instance" for the server hosted by Scaleway (that is the product's official name)
- "Minecraft server" for the Minecraft server hosted on the instance 

The program requires 4 arguments to run:
- [your API key](https://www.scaleway.com/en/docs/iam/how-to/create-api-keys/), it's a UUID
- instance's ID (available on the instance's dashboard), it's a UUID
- [instance's zone](https://www.scaleway.com/en/docs/instances/concepts/#availability-zone) (they call it "Availability Zones")
- Minecraft's host that is used during the player's transfer; in most cases, this is the instance's IP

You can pass these optionals arguments:
- port of the server (default: `25565`)
- port of the Minecraft server (default: `25565`)
- name of the server visible in the debug screen (default: `Minecraft Scaleway Frontend`)
- whitelist (default: no whitelist), separate each user with a coma (`,`); you can use their Minecraft's username and their UUID
- Discord Webhook's link to use when sending update (default: not connected to Discord)

The icon used if the Minecraft server is offline is `server-icon.png`.
It must follow [these rules](https://minecraft.wiki/w/Tutorial:Server_maintenance#Setting_the_server's_icon).

Logs are in `logs/`.

The current logs are in `latest.log`.
This file is compressed with GZip when the program is stopped.
Its new name is `yyyy-MM-dd HH:mm.log.gz` (program launch date).

### Version

Each version of the project supports only one version of Minecraft.
The supported version is indicated in its name.
For example, `1.0.0+1.21.6` supports Minecraft 1.21.6 and `1.1.0+1.21.7` supports Minecraft 1.21.7.

### CLI
```bash
java -jar server.jar \
  --zone instance-zone \
  --instance instance-id \
  --api-key your-api-key \
  --minecraft-host ip-of-minecraft-server
```

### Docker

You can use the official Docker image `ghcr.io/architects-land/minecraft-scaleway-frontend`.

Tags:
- `latest` is always the latest one
- `main` is for the main branch
- `v*` is for a specific tag (e.g., `v1.0.3-1.21.6`)

Environments:
- `ZONE` is the instance's zone
- `INSTANCE` is the ID of the instance
- `API_KEY` is your API key
- `MINECRAFT_HOST` is the host of your Minecraft server

To save the logs, bind a volume to `/app/logs`.

Example `docker-compose.yml`:
```yml
services:
  frontend:
    image: ghcr.io/architects-land/minecraft-scaleway-frontend:v1.0.3-1.21.6
    ports:
      - 25565:25565
    environment:
      ZONE: fr-par-2
      SERVER: 00000000-0000-0000-0000-000000000000
      API_KEY: 00000000-0000-0000-0000-000000000000
      MINECRAFT_HOST: 198.51.100.0
    volumes:
      - ./logs:/app/logs
```

## Technology

- Minestom
- Kotlin
- Gradle (Kotlin DSL)
- kotlinx-serialization-json
- kotlinx-coroutine-core
- Log4J 2
- lenni0451's MCPing