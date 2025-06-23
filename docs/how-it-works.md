# How it works ?

The server is a proxy and the Minecraft server hosted on the instance is behind it.

## Commands

The server adds 2 commands: `/info` and `/connect`.

`/info` sends you the current state of the instance and of the Minecraft server if the instance is running.

`/connect` forces the connection to the Minecraft server. Useful if you think that the server bug.

## First connection

When a player connect to the server for the first time, the server starts the instance.

It checks every 10 seconds if the instance is started.

If the instance is started, it checks every 5 seconds after a delay of 15 seconds if the Minecraft server is running.
If it is, the server sends every player to the Minecraft server using the paquet Transfer (from the commands `/transfer`).

It also checks every 30 seconds after a delay of 2 minutes if the instance is `stopped in place`.
It is when the instance is powered off but still in Scaleway's hypervisor.
If it is, it removes the instance form Scaleway's hypervisor. 
This avoids useless cost.

## When a player connect

If the instance is running, it checks if the Minecraft server is started.
If it is, the server sends the player to the Minecraft server.
If it isn't, it waits until the Minecraft server is ready.

If the instance is not running (including if it is `stopped in place`), the server will consider it as the first connection.

## When a player disconnect

The mod [Server Stopper](https://github.com/architects-land/server-stopper) sends the number of connected player to the 
service [Server Stopper](https://github.com/architects-land/server-stopper) using a UNIX socket.

If the Minecraft server is empty, the service waits 5 minutes.
If the Minecraft server remains empty, the service powers off the instance.
The instance is `stopped in place` and can be fully stopped by the server.