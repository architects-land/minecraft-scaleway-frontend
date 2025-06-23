# Maintain

This configuration is complexe, so maintaining this could be complexe.
This document lists essentials commands to perform critical operation.

If you are using systemd (like in the tutorial), you can use all common systemd commands to perform all important 
operation.
```bash
systemctl start minecraft.service # start the minecraft server
systemctl stop minecraft.service # stop the minecraft server
systemctl restart minecraft.service # restart the minecraft server

journalctl -u minecraft.service # get the logs

systemctl enable minecraft.service # enable the automatic launch of the minecraft server
systemctl disable minecraft.service # disable the automatic launch of the minecraft server
```
You can replace `minecraft.service` by `server-stopper.service` to perform the same operations.

You can't access to the Minecraft server's console to send commands.
You must use RCON or use an operator account (`/op` on you before starting it with the init).

:::warning Don't mess up with UNIX Permissions!
Always use `sudo -u minecraft` before every command in `/var/minecraft` or do a `chown -R minecraft:minecraft` after
every operation.
:::