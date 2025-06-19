package world.anhgelus.world.architectsland.minecraftscalewayfrontend

import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerChatEvent
import net.minestom.server.extras.MojangAuth
import net.minestom.server.network.packet.server.common.TransferPacket
import java.util.logging.Level
import java.util.logging.Logger

val LOGGER: Logger = Logger.getLogger("MinecraftScalewayFrontend")

fun main(args: Array<String>) {
    LOGGER.level = Level.INFO
    LOGGER.info("Minecraft Scaleway Frontend launched")
    val parser = ArgsParser(args)

    if (!parser.has("zone")) {
        LOGGER.severe("Specify zone of the server")
        return
    }
    if (!parser.has("server")) {
        LOGGER.severe("Specify the server")
        return
    }
    if (!parser.has("api-key")) {
        LOGGER.severe("Specify the api key to use")
        return
    }
    if (!parser.has("minecraft-ip")) {
        LOGGER.severe("Specify the ip address of the minecraft server")
        return
    }

    val scaleway = ScalewayAPI(parser.get("api-key")!!, parser.get("zone")!!, parser.get("server")!!)

    val server = MinecraftServer.init()

    // make server use online mode
    MojangAuth.init()

    val instanceManager = MinecraftServer.getInstanceManager()

    val instance = instanceManager.createInstanceContainer()
    val handler = MinecraftServer.getGlobalEventHandler()
    // spawn player
    handler.addListener(AsyncPlayerConfigurationEvent::class.java) { event ->
        LOGGER.info("Player ${event.player.name} connected")
        val player = event.player
        event.spawningInstance = instance
        player.respawnPoint = Pos(0.0, 42.0, 0.0)

        if (scaleway.serverState() == ScalewayAPI.ServerState.RUNNING) {
            player.sendPacket(TransferPacket(
                parser.get("minecraft-ip")!!,
                parser.getIntOrDefault("minecraft-port", 25565)
            ))
        }
    }

    handler.addListener(
        PlayerChatEvent::class.java
    ) {
        LOGGER.info(it.formattedMessage.toString())
    }

    LOGGER.info("Minecraft Scaleway Frontend started")
    server.start("0.0.0.0", parser.getIntOrDefault("port", 25565))
}