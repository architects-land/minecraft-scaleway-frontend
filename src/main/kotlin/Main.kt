package world.anhgelus.world.architectsland.minecraftscalewayfrontend

import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.lenni0451.mcping.MCPing
import net.lenni0451.mcping.responses.MCPingResponse
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.GameMode
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerChatEvent
import net.minestom.server.event.player.PlayerCommandEvent
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import net.minestom.server.event.server.ServerListPingEvent
import net.minestom.server.extras.MojangAuth
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.network.packet.server.common.TransferPacket
import net.minestom.server.utils.identity.NamedAndIdentified
import net.minestom.server.world.DimensionType
import sun.misc.Signal
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.concurrent.schedule

val LOGGER: Logger = Logger.getLogger("MinecraftScalewayFrontend")

lateinit var TIMER: Timer

fun main(args: Array<String>) {
    LOGGER.level = Level.INFO
    LOGGER.info("Minecraft Scaleway Frontend launched")
    val parser = ArgsParser(args)

    if (!parser.has("zone")) {
        LOGGER.severe("Specify the zone of the server")
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
    if (!parser.has("minecraft-host")) {
        LOGGER.severe("Specify the ip address of the minecraft server")
        return
    }

    TIMER =  Timer()

    val scaleway = ScalewayAPI(parser.get("api-key")!!, parser.get("zone")!!, parser.get("server")!!)

    val server = MinecraftServer.init()
    MinecraftServer.setBrandName("Architects Land - Lobby")

    // make server use online mode
    MojangAuth.init()

    val instanceManager = MinecraftServer.getInstanceManager()

    val instance = instanceManager.createInstanceContainer(DimensionType.THE_END)

    val handler = MinecraftServer.getGlobalEventHandler()

    val hostname = parser.get("minecraft-host")!!
    val port = parser.getIntOrDefault("minecraft-port", 25565)

    val pinger = { MCPing.pingModern().address(hostname, port).timeout(500, 500) }

    var starting = false
    // spawn player
    handler.addListener(AsyncPlayerConfigurationEvent::class.java) { event ->
        val player = event.player
        LOGGER.info {
            val name = PlainTextComponentSerializer.plainText().serialize(player.name)
            "Player $name (${player.uuid}) connected"
        }
        event.spawningInstance = instance
        player.respawnPoint = Pos(0.0, 42.0, 0.0)
        player.gameMode = GameMode.SPECTATOR
    }

    handler.addListener(PlayerSpawnEvent::class.java) { event ->
        val player = event.player
        pinger().exceptionHandler {
            if (starting) {
                player.sendMessage(Component.text("The server is already starting..."))
                return@exceptionHandler
            }
            starting = true
            val state = scaleway.serverState()
            if (state == ScalewayAPI.ServerState.RUNNING || state == ScalewayAPI.ServerState.STARTING) {
                LOGGER.warning("Server already running/starting")
                return@exceptionHandler
            } else if (state == ScalewayAPI.ServerState.LOCKED) {
                LOGGER.warning("Server locked")
                return@exceptionHandler
            }
            startServer(scaleway, pinger, instance, hostname, port)
        }.responseHandler {
            player.sendPacket(TransferPacket(hostname, port))
        }.sync
    }

    handler.addListener(PlayerDisconnectEvent::class.java) { event ->
        LOGGER.info {
            val name = PlainTextComponentSerializer.plainText().serialize(event.player.name)
            "Player $name (${event.player.uuid}) disconnected"
        }
    }

    handler.addListener(PlayerChatEvent::class.java) {
        val playerName = PlainTextComponentSerializer.plainText().serialize(it.player.name)
        LOGGER.info("<$playerName> ${it.rawMessage}")
    }

    handler.addListener(ServerListPingEvent::class.java) {
        val respData = it.responseData
        respData.clearEntries()
        pinger().exceptionHandler {
            respData.setPlayersHidden(true)
            respData.description = Component.text("The server is sleeping. Connect you to wake it up!")
        }.responseHandler { data ->
            respData.description = Component.text("The server is running.")
            data.players.sample.forEach { p -> respData.addEntry(NamedAndIdentified.named(p.name)) }
        }.sync
    }

    handler.addListener(PlayerCommandEvent::class.java) {
        val playerName = PlainTextComponentSerializer.plainText().serialize(it.player.name)
        LOGGER.info("$playerName: /${it.command}")
    }

    val commands = MinecraftServer.getCommandManager()
    commands.register(InfoCommand(scaleway))
    commands.register(ConnectCommand(pinger, hostname, port))

    server.start("0.0.0.0", parser.getIntOrDefault("port", 25565))
    LOGGER.info("Minecraft Scaleway Frontend started")

    listOf("INT", "TERM").forEach { signalName ->
        Signal.handle(Signal(signalName)) { signal ->
            runBlocking {
                LOGGER.info("Stopping...")
                MinecraftServer.stopCleanly()
                TIMER.cancel()
            }
        }
    }
}

fun startServer(scaleway: ScalewayAPI, pinger: () -> MCPing<MCPingResponse>, instance: InstanceContainer, hostname: String, port: Int) {
    LOGGER.info("Starting the server")
    instance.players.forEach { it.sendMessage(Component.text("Starting the server for you...")) }
    scaleway.startServer()
    TIMER.schedule(10*1000L, 10*1000L) {
        val state = scaleway.serverState()
        if (state != ScalewayAPI.ServerState.RUNNING) {
            LOGGER.info("Server is still starting...")
            return@schedule
        }
        LOGGER.info("Server started, waiting for the Minecraft server")
        instance.players.forEach { it.sendMessage(Component.text("Waiting for the Minecraft server...")) }
        TIMER.schedule(5*1000L, 5*1000L) {
            pinger().exceptionHandler {
                LOGGER.info("Minecraft server is still starting...")
            }.responseHandler {
                instance.players.forEach {
                    LOGGER.info {
                        val name = PlainTextComponentSerializer.plainText().serialize(it.name)
                        "Sending $name (${it.uuid}) to the server"
                    }
                    it.sendPacket(TransferPacket(hostname, port))
                }
                cancel()
            }.sync
        }
        cancel()
    }
}