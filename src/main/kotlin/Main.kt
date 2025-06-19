package world.anhgelus.world.architectsland.minecraftscalewayfrontend

import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.extras.MojangAuth
import java.util.logging.Level
import java.util.logging.Logger

val LOGGER = Logger.getLogger("MinecraftScalewayFrontend")

fun main() {
    LOGGER.level = Level.INFO
    LOGGER.info("Minecraft Scaleway Frontend launched")
    val server = MinecraftServer.init()

    // make server use online mode
    MojangAuth.init();

    val instanceManager = MinecraftServer.getInstanceManager()

    val instance = instanceManager.createInstanceContainer()
    val globalEventHandler = MinecraftServer.getGlobalEventHandler()
    // spawn player
    globalEventHandler.addListener(AsyncPlayerConfigurationEvent::class.java) { event ->
        val player = event.player
        event.spawningInstance = instance
        player.respawnPoint = Pos(0.0, 42.0, 0.0)
    }

    LOGGER.info("Minecraft Scaleway Frontend started")
    server.start("0.0.0.0", 25565)
}