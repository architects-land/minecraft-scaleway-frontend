# Create a plugin

You must use a JVM compatible language.
The API was made with Kotlin, so all examples will be made with Kotlin.

## Gradle setup

Firstly, you must add this project to Gradle.
```kts
repositories {
    // previous repo
    maven { url = uri("https://jitpack.io") } 
}

dependencies {
    // previous deps
    implementation("com.github.architects-land:minecraft-scaleway-frontend:<--version-->")
}
```

You don't need to use shadow jar.
The server has everything required to run your plugin.

## Base

Create a new class (or an object) extending `Plugin`.
```kotlin
class MyPlugin : Plugin {
    override fun onLoad(helper: PluginHelper) {}
    override fun onUnload(helper: PluginHelper) {}
}
```
The method `onLoad` will be called during the plugin's load.
The method `onUnload` will be called during the plugin's unload.

Create a new resource called `plugin.json`.
This file contains every important information about your plugin.
```jsonc
{
  "name": "You name",
  "main": "org.example.myplugin.MyPlugin",
  "version": "1.0.0",
  "author": "John Doe"
}
```
`main` must be the reference to your main class!

Now, your plugin can be loaded by the server.

## Using internal API

You can get server's internal API by using the interface `PluginHelper` given during the load and the unload event.

`PluginHelper::getDiscordWebhook()` gives you a class to communicate with Discord via the webhook.

`PluginHelper::getScalewayAPI()` gives you a class to communicate with Scaleway via their API.

:::danger Scaleway API
Be aware that a bad usage of this API could increase the costs of the owner.
:::
