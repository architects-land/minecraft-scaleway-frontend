# Advanced configuration

The server is heavily configurable.

[Getting started](/getting-started) only covered the required arguments.
If you want to set a whitelist, the server's port and more, you can!

## Using a `.env`

You can use a `.env` file (even if you are not using Docker) to set variables.

When the server loads this file, it converts automatically `_` into `-`.
Thus, `FOO_BAR=baz` will be loaded as `--foo-bar baz`.

:::info CLI args with `.env`
CLI args overrides the `.env` variables.
:::

:::info Docker image convention
The Docker image uses the same convention
:::

## Setting ports

By default, the server is running on `25565`.
You can modify it with `--port uint`.

The Minecraft server ports is `25565` by default.
You can modify it with `--minecraft-port uint`.

## Custom name

You can modify the server's name with `--server-name string`.
It's visible in the debug screen (`F3`).

## Server icon

You can set the server's icon to use if the Minecraft server is offline by creating a `server-icon.png` file.
It must follow [these rules](https://minecraft.wiki/w/Tutorial:Server_maintenance#Setting_the_server's_icon).

## Using a whitelist

You can use a whitelist to prevent unwanted users to connect to the server.
You can set it with `--whitelist string`.
If the whitelist is empty, the server considers there is no whitelist.

Separate each user with a coma (`,`).
Users can be whitelisted with their username or with their UUID.
For example, `anhgelus` and `129d6d88-6d8a-4444-b3c7-0d2ba32e9b12` are both valid.

Valid whitelists:
```
anhgelus,ascpial
129d6d88-6d8a-4444-b3c7-0d2ba32e9b12,ascpial
129d6d88-6d8a-4444-b3c7-0d2ba32e9b12,3f6ddb7c-f214-48a9-9f4a-eb22b9cf53f0
```

Invalid whitelists:
```
anhgelus ascpial
anhgelus;ascpial
129d6d88-6d8a-4444-b3c7-0d2ba32e9b12 ascpial
```

## Discord integration

The server has a Discord integration using [webhook](https://support.discord.com/hc/en-us/articles/228383668-Intro-to-Webhooks).

Use `--discord-webhook string` to set it.

The server sends a message when:
- the instance is starting (message: ":arrows_counterclockwise: Starting the server")
- the instance is started (message: ":arrows_counterclockwise: Waiting for the Minecraft server")
- the Minecraft server is started (message: ":white_check_mark: Minecraft server started")
- the instance is powered off (message: ":no_entry: Server stopped")
