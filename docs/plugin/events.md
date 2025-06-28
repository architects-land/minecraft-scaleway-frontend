# Events

To perform any actions, the plugin must listen to events.
Minecraft Scaleway Frontend uses Minestom's framework to send custom events.

## Listening events

Check [Minestom documentation](https://minestom.net/docs/feature/events) for more information.

To create a handler, you must get your plugin's node with `PluginHelper#getEventNode()`.
Then, you can register a listener with `EventNode<E>#addListener(Class<E>, Consumer<Class<E>>)`, e.g.
```kotlin
val node = helper.getEventNode()
node.addListener(TransferEvent.class) { event ->
    // your code
}
```

Usually, this is done during the load process, but you can do it during the unload process!
(I don't know why you could use this "feature".)

:::tip
Every custom event is in the package `world.anhgelus.architectsland.minecraftscalewayfrontend.event`.
:::

## What's hidden behind

Minestom uses a tree to represent event handlers.

Each plugin has their own node with their handlers.
These are grouped in a super node that is contained in the root node.
The server's handlers are in the root node.
With this architecture, a plugin cannot break the server.
