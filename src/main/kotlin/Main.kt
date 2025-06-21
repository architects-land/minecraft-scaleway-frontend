package world.anhgelus.world.architectsland.minecraftscalewayfrontend

import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.lenni0451.mcping.MCPing
import net.lenni0451.mcping.responses.MCPingResponse
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.GameMode
import net.minestom.server.event.player.*
import net.minestom.server.event.server.ServerListPingEvent
import net.minestom.server.extras.MojangAuth
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.network.packet.server.common.TransferPacket
import net.minestom.server.utils.identity.NamedAndIdentified
import net.minestom.server.world.DimensionType
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.message.ParameterizedMessage
import sun.misc.Signal
import java.util.*
import kotlin.concurrent.schedule

val LOGGER: Logger = LogManager.getLogger("world.anhgelus.world.architectsland.minecraftscalewayfrontend")

lateinit var TIMER: Timer

fun main(args: Array<String>) {
    LOGGER.info("Minecraft Scaleway Frontend launched")
    val parser = ArgsParser(args)

    if (!parser.has("zone")) {
        LOGGER.error("Specify the zone of the server")
        return
    }
    if (!parser.has("server")) {
        LOGGER.error("Specify the server")
        return
    }
    if (!parser.has("api-key")) {
        LOGGER.error("Specify the api key to use")
        return
    }
    if (!parser.has("minecraft-host")) {
        LOGGER.error("Specify the ip address of the minecraft server")
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

    // spawn player
    handler.addListener(AsyncPlayerConfigurationEvent::class.java) { event ->
        val player = event.player
        LOGGER.info {
            val name = PlainTextComponentSerializer.plainText().serialize(player.name)
            ParameterizedMessage("Player {} ({}) connected", name, player.uuid)
        }
        event.spawningInstance = instance
        player.respawnPoint = Pos(0.0, 42.0, 0.0)
        player.gameMode = GameMode.SPECTATOR
    }

    handler.addListener(PlayerSpawnEvent::class.java) { event ->
        val player = event.player
        pinger().exceptionHandler {
            val state = scaleway.serverState()
            if (state == ScalewayAPI.ServerState.RUNNING || state == ScalewayAPI.ServerState.STARTING) {
                player.sendMessage(Component.text("The server is already starting..."))
                return@exceptionHandler
            } else if (state == ScalewayAPI.ServerState.LOCKED) {
                LOGGER.warn("Server locked")
                return@exceptionHandler
            }
            startServer(scaleway, pinger, instance, hostname, port)
        }.responseHandler {
            LOGGER.info {
                val name = PlainTextComponentSerializer.plainText().serialize(event.player.name)
                ParameterizedMessage("Sending player {} ({}) to the Minecraft server", name, player.uuid)
            }
            player.sendPacket(TransferPacket(hostname, port))
        }.sync
    }

    handler.addListener(PlayerDisconnectEvent::class.java) { event ->
        LOGGER.info {
            val name = PlainTextComponentSerializer.plainText().serialize(event.player.name)
            ParameterizedMessage("Player {} ({}) disconnected", name, event.player.uuid)
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
            respData.maxPlayer = respData.maxPlayer
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
                LOGGER.info("Stopped")
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
            LOGGER.info("Server is still starting... Current state: $state")
            return@schedule
        }
        LOGGER.info("Server started, waiting for the Minecraft server")
        instance.players.forEach { it.sendMessage(Component.text("Waiting for the Minecraft server...")) }

        setupServerTransfer(pinger, instance, hostname, port)
        setupServerPowerOff(scaleway)

        cancel()
    }
}

fun setupServerTransfer(pinger: () -> MCPing<MCPingResponse>, instance: InstanceContainer, hostname: String, port: Int) {
    TIMER.schedule(5*1000L, 5*1000L) {
        pinger().exceptionHandler {
            LOGGER.info("Assuming that the Minecraft server is still starting...", it)
        }.responseHandler {
            instance.players.forEach {
                LOGGER.info {
                    val name = PlainTextComponentSerializer.plainText().serialize(it.name)
                    ParameterizedMessage("Sending player {} ({}) to the Minecraft server", name, it.uuid)
                }
                it.sendPacket(TransferPacket(hostname, port))
            }
            cancel()
        }.sync
    }
}

fun setupServerPowerOff(scaleway: ScalewayAPI) {
    TIMER.schedule(2*60*1000L, 30*1000L) {
        val state = scaleway.serverState()
        if (state != ScalewayAPI.ServerState.STOPPED_IN_PLACE) return@schedule
        LOGGER.info("Powering off server (state: $state)")
        scaleway.powerOffServer()
        cancel()
    }
}