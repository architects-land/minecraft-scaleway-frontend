# Minecraft Scaleway Frontend

Fake Minecraft Server used as a frontend to hourly paid Scaleway servers.

Automatically start the server if a player is waiting.
If the server is started, transfer the player to the server.
If the server is `stopped in place` (powered off, but still in Scaleway's hypervisor), remove it from Scaleway's hypervisor.

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

### CLI
```bash
java -jar server.jar \
  --zone instance-zone \
  --instance instance-id \
  --api-key your-api-key \
  --minecraft-host ip-of-minecraft-server
```

You can also use `--port` to set the port of the server or `--minecraft-port` to set the port
of the Minecraft server.

You can modify the server name with `--server-name string` (use quotes if your string contains space).

You specify a whitelist with `--whitelist`, e.g.: `--whitelist anhgelus,ascpial`, 
`--whitelist anhgelus,3f6ddb7c-f214-48a9-9f4a-eb22b9cf53f0`.

You can use a `.env` file to set these variables.
It converts automatically `_` into `-`.
Thus, `FOO_BAR=baz` will be loaded as `--foo-bar baz`.
CLI args overrides the `.env` variables.

### Docker

You can use the official Docker image `ghcr.io/architects-land/minecraft-scaleway-frontend`.

Tags:
- `latest` is always the latest one
- `main` is for the main branch
- `v*` is for a specific tag (e.g., `v1.0.0`)

Environments:
- `PORT` is the server's port
- `ZONE` is the instance's zone
- `INSTANCE` is the ID of the instance
- `API_KEY` is your API key
- `MINECRAFT_HOST` is the host of your Minecraft server
- `MINECRAFT_PORT` is the port of your Minecraft server
- `SERVER_NAME` is the name of this server
- `WHITELIST` is the whitelist

To save the logs, bind a volume to `/app/logs`.

To use a custom icon, bind your icon to `/app/server-icon.png`.

Example `docker-compose.yml`:
```yml
services:
  frontend:
    image: ghcr.io/architects-land/minecraft-scaleway-frontend:v1.0.0
    ports:
      - 25565:25565
    environment:
      PORT: 25565
      ZONE: fr-par-2
      SERVER: 00000000-0000-0000-0000-000000000000
      API_KEY: 00000000-0000-0000-0000-000000000000
      MINECRAFT_HOST: 198.51.100.0 # example IP
      MINECRAFT_PORT: 25565
      SERVER_NAME: "My frontend"
      WHITELIST: anhgelus
    volumes:
      - ./logs:/app/logs
      - ./server-icon.png:/app/server-icon.png
```

## Technology

- Minestom
- Kotlin
- Gradle (Kotlin DSL)
- kotlinx-serialization-json
- kotlinx-coroutine-core
- Log4J 2
- lenni0451's MCPing