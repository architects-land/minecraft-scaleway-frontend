# Server Stopper

If you are using Minecraft without any modifications, you will not be able to stop the server if nobody is connected.
To fix this issue, you can use [Server Stopper](https://github.com/architects-land/server-stopper).
This is a mod and a service made for this.

Architects Land does not provide compiled version of these, so you must compile it yourself.
It requires Java 21, Gradle and Go 1.24+.

You can upload these to the instance.
Put the jar in the mod folder (`/var/minecraft/mods`).
Do not forget to give the permission to the `minecraft` user.
Put the service jar in `/usr/local/bin`.

Download the service unit file provided in GitHub and put it in `/etc/systemd/system/`
```bash
curl -o /etc/systemd/system/server-stopper.service https://raw.githubusercontent.com/architects-land/server-stopper/refs/heads/main/service/server-stopper.service
```
Edit this if required.

Now, enables the service and start it
```bash
systemctl enable --now server-stopper.service
```

:::tip Can't execute binary or service?
If you can't execute the binary or the service and if your OS use SELinux (like any RHEL-based distro), use
```bash
restorecon [path]
```
where `[path]` is the path of the binary or of the service.
:::

You can start the Minecraft server with
```bash
systemctl start minecraft.service
```
If the Minecraft server is empty during 5 minutes, the Minecraft server will be stopped and the instance will be powered
off.