package world.anhgelus.world.architectsland.minecraftscalewayfrontend

import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerChatEvent
import net.minestom.server.extras.MojangAuth
import java.util.logging.Level
import java.util.logging.Logger

val LOGGER: Logger = Logger.getLogger("MinecraftScalewayFrontend")

fun main(args: Array<String>) {
    LOGGER.level = Level.INFO
    LOGGER.info("Minecraft Scaleway Frontend launched")
    val server = MinecraftServer.init()

    // make server use online mode
    MojangAuth.init();

    val instanceManager = MinecraftServer.getInstanceManager()

    val instance = instanceManager.createInstanceContainer()
    val handler = MinecraftServer.getGlobalEventHandler()
    // spawn player
    handler.addListener(AsyncPlayerConfigurationEvent::class.java) { event ->
        LOGGER.info("Player ${event.player.name} connected")
        val player = event.player
        event.spawningInstance = instance
        player.respawnPoint = Pos(0.0, 42.0, 0.0)
    }

    handler.addListener(
        PlayerChatEvent::class.java
    ) {
        LOGGER.info(it.formattedMessage.toString())
    }


    LOGGER.info("Minecraft Scaleway Frontend started")
    server.start("0.0.0.0", 25565)
}