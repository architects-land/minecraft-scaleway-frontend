# Getting started

Minecraft Scaleway Frontend requires:
- something to host the server (could be a VPS, a serverless instance or just a Docker container running in a cluster);
- an instance from Scaleway using a Linux distribution (in this tutorial, we will use Rocky Linux).

::: tip Vocabulary
"Server" is the software "Minecraft Scaleway Frontend".

"Instance" is the instance from Scaleway.

"Minecraft server" is the Minecraft server hosted by the instance.
:::

## Instance's configuration

### Scaleway configuration

Choose the appropriate instance on the Scaleway's website.

Don't forget to use a public IPv4 in addition to this (Yggdrasil, the server used for Minecraft's authentification, does
not support IPv6).

Don't forget to add a storage.
It will contain the OS plus the Minecraft server.

Then, you can choose your Linux distribution.
Prefer a RHEL-based distribution: it is way more stable.

### Running the Minecraft server

Now, we will install Java to run Minecraft. 
For Rocky Linux, you can use
```bash
curl https://raw.githubusercontent.com/anhgelus/cloud-setups/refs/heads/main/minecraft/only-java/java21-rocky.sh | bash
```
to install Adoptium Temurin JRE 21.
You can use any OpenJDK distribution supporting Java 21.

We will create the folder containing and the user running the Minecraft server.
Run to create a new user `minecraft`
```bash
useradd -M minecraft
usermod -s /usr/sbin/nologin minecraft
```
You will not be able to connect to this user.
Run to create the folder container the Minecraft server
```bash
mkdir /var/minecraft
```
Install your Minecraft server in `/var/minecraft`.
Start it, stop it and modify `server.properties` to allow server transfer (protocol used by the server to send players 
to the Minecraft server).

:::tip Automatic installation with anhgelus/cloud-setups
You can use [anhgelus's cloud-setups](https://github.com/anhgelus/cloud-setups) to perform automatic installation of a
Minecraft server.

With these, you can easily install Java or a Modrinth modpack for a Rocky Linux.
:::

Modify the owner of `/var/minecraft` and set it to `minecraft:minecraft`
```bash
chown -R minecraft:minecraft /var/minecraft
```
Now, if you want to manage the Minecraft server via your shell, add `sudo -u minecraft` before every command.

We will create the systemd unit file for the server.
Put this content in `/etc/systemd/system/minecraft.service`
```service
[Unit]
Description=Minecraft server service
After=network-online.target
Wants=network-online.target

[Service]
User=minecraft
WorkingDirectory=/var/minecraft

# customize the CLI
ExecStart=bash -c "/usr/bin/java"

Restart=always
#RestartSec=30

[Install]
WantedBy=multi-user.target
```
Then, reload systemd and enable the service
```bash
systemctl daemon-reload
systemctl enable minecraft.service
```
It will start the Minecraft server everytime the instance restarts.

:::tip Can't execute binary or service?
If you can't execute the binary or the service and if your OS use SELinux (like any RHEL-based distro), use
```bash
restorecon [path]
```
where `[path]` is the path of the binary or of the service.
:::

#### Better way to stop the server

You can stop the Minecraft via the rcon by using [server stopper](/server-stopper).
If you are using it, add a `ExecStop` running this in the service section of your unit file.

### Stopping the instance and the Minecraft server if no one is connected

Read [Server Stopper](server-stopper.md).

## Server configuration

You can use the binary or use Docker to deploy the server.
You can deploy this where do you want (it uses less than 100MB RAM when one player is connected waiting for the 
Minecraft server to be on).

The server has 4 required arguments:
- [your API key](https://www.scaleway.com/en/docs/iam/how-to/create-api-keys/), it's a UUID
- instance's ID (available on the instance's dashboard), it's a UUID
- [instance's zone](https://www.scaleway.com/en/docs/instances/concepts/#availability-zone) (they call it "Availability Zones")
- Minecraft's host that is used during the player's transfer; in most cases, this is the instance's IP

### Version

Each version of the project supports only one version of Minecraft.
The supported version is indicated in its name.
For example, `1.0.0+1.21.6` supports Minecraft 1.21.6 and `1.1.0+1.21.7` supports Minecraft 1.21.7.

The "first version" (i.e. the thing *before* the `+`) is the version of the server.
The "second version" (i.e. the thing *after* the `+`) is the version of Minecraft.

### CLI

```bash
java -jar server.jar \
  --zone instance-zone \
  --instance instance-id \
  --api-key your-api-key \
  --minecraft-host ip-of-minecraft-server
```

Check [advanced configuration](/advanced-configuration) for more information.

### Docker

You can use the official Docker image `ghcr.io/architects-land/minecraft-scaleway-frontend`.

Tags:
- `latest` is always the latest one
- `main` is for the main branch
- `v*` is for a specific tag (e.g., `v1.0.3-1.21.6`)

Required environments:
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

Check [advanced configuration](/advanced-configuration) for more information.

### Logs

Logs are in `logs/`.

The current logs are in `latest.log`.
This file is compressed with GZip when the program is stopped.
Its new name is `yyyy-MM-dd HH:mm.log.gz` (program launch date).
