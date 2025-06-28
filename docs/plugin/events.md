# Events

To perform any actions, the plugin must listen to events.
There are two kinds of events: Minecraft Scaleway Frontend's event and Minestom's event.

Minecraft Scaleway Frontend's events are managed and added by the server. 
These are useful to manage how Minecraft Scaleway Frontend works.  

Minestom's events are managed by Minestom, the framework behind the Minecraft server.
With this, you can handle how the Minecraft server works.

:::warning
Be careful when you are using Minestom's event: you can break Minecraft Scaleway Frontend.
:::

## Listening to Minecraft Scaleway Frontend's event

A listener is a class inheriting from `EventListener`.
To listen to a specific event, just override the method.

There are two kinds of events: the cancellable and the non-cancellable ones.

A non-cancellable event returns nothing.
A cancellable event returns a boolean: false if the event is the not cancelled and true if it is.

For example, this listener
```kotlin
object MyListener : EventListener {
    override fun onInstanceStart(): Boolean {
        return true
    }
}
```
will block the Scaleway's instance to start.

To inform Minecraft Scaleway Frontend to call the listener, you must register it via the `PluginHelper`.
This interface is given during the load and the unload event (in your main class).
Use the method `PluginHelper::registerListener(EventListener)` to register your listener.
```kotlin
fun onLoad(helper: PluginHelper) {
    helper.registerListener(MyListener)
}
```

:::info
You can register an event during the unload event, but I don't know why you want to do this.
:::

## Listening to Minestom's event

You can the `GlobalEventHandler` from Minestom with `PluginHelper::getMinecraftEventHandler()`.

Check [their documentation](https://minestom.net/docs/feature/events) for more information.

Minecraft Scaleway Frontend uses:
- `AsyncPlayerConfigurationEvent`
- `PlayerSpawnEvent`
- `PlayerDisconnectEvent`
- `PlayerChatEvent`
- `ServerListPingEvent`
- `PlayerCommandEvent`
