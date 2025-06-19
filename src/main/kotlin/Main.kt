package world.anhgelus.world.architectsland.minecraftscalewayfrontend

import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent

fun main() {
    val name = "Kotlin"
    println("Hello, $name!")

    val server = MinecraftServer.init()

    val instanceManager = MinecraftServer.getInstanceManager()

    val instance = instanceManager.createInstanceContainer()
    val globalEventHandler = MinecraftServer.getGlobalEventHandler()
    // spawn player
    globalEventHandler.addListener(AsyncPlayerConfigurationEvent::class.java) { event ->
        val player = event.player
        event.spawningInstance = instance
        player.respawnPoint = Pos(0.0, 42.0, 0.0)
    }

    server.start("0.0.0.0", 25565)
}