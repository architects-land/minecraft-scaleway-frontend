package world.anhgelus.world.architectsland.minecraftscalewayfrontend

import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerChatEvent
import net.minestom.server.extras.MojangAuth
import net.minestom.server.network.packet.server.common.TransferPacket
import org.replydev.mcping.MCPinger
import org.replydev.mcping.PingOptions
import java.io.IOException
import java.util.Timer
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.concurrent.schedule

val LOGGER: Logger = Logger.getLogger("MinecraftScalewayFrontend")

val TIMER = Timer()

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

    val option = PingOptions.builder()
        .hostname(parser.get("minecraft-ip")!!)
        .port(parser.getIntOrDefault("minecraft-port", 25565))
        .timeout(1000)
        .build()
    val pinger = MCPinger.builder().pingOptions(option).build()

    var starting = false
    // spawn player
    handler.addListener(AsyncPlayerConfigurationEvent::class.java) { event ->
        LOGGER.info("Player ${event.player.name} connected")
        val player = event.player
        event.spawningInstance = instance
        player.respawnPoint = Pos(0.0, 42.0, 0.0)

        try {
            pinger.fetchData()

            player.sendPacket(TransferPacket(option.hostname, option.port))
        } catch (_: IOException) {
            if (starting) return@addListener
            starting = true
            val state = scaleway.serverState()
            if (state == ScalewayAPI.ServerState.RUNNING || state == ScalewayAPI.ServerState.STARTING) {
                LOGGER.warning("Server already running/starting")
                return@addListener
            } else if (state == ScalewayAPI.ServerState.LOCKED) {
                LOGGER.warning("Server locked")
                return@addListener
            }
            scaleway.startServer()
            TIMER.schedule(10*60*1000L, 10*60*1000L) {
                val state = scaleway.serverState()
                if (state != ScalewayAPI.ServerState.RUNNING) return@schedule
                TIMER.schedule(5*60*1000L, 5*60*1000L) {
                    try {
                        pinger.fetchData()

                        instance.players.forEach {
                            it.sendPacket(TransferPacket(option.hostname, option.port))
                        }
                    } catch (_: IOException) {}
                }
                cancel()
            }
        }
    }

    handler.addListener(PlayerChatEvent::class.java) {
        LOGGER.info(it.formattedMessage.toString())
    }

    LOGGER.info("Minecraft Scaleway Frontend started")
    server.start("0.0.0.0", parser.getIntOrDefault("port", 25565))
}