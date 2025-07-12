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
import net.minestom.server.ping.Status
import net.minestom.server.utils.identity.NamedAndIdentified
import net.minestom.server.world.DimensionType
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.message.ParameterizedMessage
import sun.misc.Signal
import world.anhgelus.world.architectsland.minecraftscalewayfrontend.http.DiscordWebhookAPI
import world.anhgelus.world.architectsland.minecraftscalewayfrontend.http.ScalewayAPI
import java.nio.charset.MalformedInputException
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.concurrent.schedule

val LOGGER: Logger = LogManager.getLogger("world.anhgelus.world.architectsland.minecraftscalewayfrontend")

lateinit var TIMER: Timer

private var powerOffTask: TimerTask? = null

fun main(args: Array<String>) {
    LOGGER.info("Minecraft Scaleway Frontend launched")
    val today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    val parser = ArgsParser(args)

    if (!parser.has("zone")) {
        LOGGER.error("Specify the zone of the server")
        return
    }
    if (!parser.has("instance")) {
        LOGGER.error("Specify the instance")
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

    var whitelist: List<String>? = null
    var whitelistEnabled = parser.has("whitelist")
    if (whitelistEnabled) {
        val p = parser.get("whitelist")!!
        if (p.isEmpty()) whitelistEnabled = false
        else whitelist = p.split(",")
    }

    TIMER =  Timer()

    val scaleway = ScalewayAPI(parser.get("api-key")!!, parser.get("zone")!!, parser.get("instance")!!)
    val serverName = parser.getOrDefault("server-name", "Minecraft Scaleway Frontend")
    var webhook = parser.get("discord-webhook")
    if (webhook != null && webhook.isEmpty()) webhook = null
    val discord = DiscordWebhookAPI(webhook, serverName)

    val server = MinecraftServer.init()
    MinecraftServer.setBrandName(serverName)

    var favicon: String? = null
    val faviconPath = Path.of("server-icon.png")
    if (Files.exists(faviconPath)) {
        try {
            Files.readString(faviconPath, Charsets.UTF_8).let { favicon = it }
        } catch (_: MalformedInputException) {
            LOGGER.error("server-icon.png is not an UTF-8 file. Skipping")
        }
    }

    // make server use online mode
    MojangAuth.init()

    val instanceManager = MinecraftServer.getInstanceManager()

    val instance = instanceManager.createInstanceContainer(DimensionType.THE_END)

    val handler = MinecraftServer.getGlobalEventHandler()

    val hostname = parser.get("minecraft-host")!!
    val port = parser.getIntOrDefault("minecraft-port", 25565)

    val pinger = { MCPing.pingModern().address(hostname, port).timeout(1000, 2000) }

    handler.addListener(AsyncPlayerConfigurationEvent::class.java) { event ->
        val player = event.player
        val name = PlainTextComponentSerializer.plainText().serialize(player.name)
        if (whitelistEnabled && !whitelist!!.any { name == it || player.uuid.toString() == it }) {
            LOGGER.info("Player {} ({}) not whitelisted", name, player.uuid)
            player.kick("You are not whitelisted.")
            return@addListener
        }
        LOGGER.info("Player {} ({}) connected", name, player.uuid)
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
            } else if (state == ScalewayAPI.ServerState.STOPPED_IN_PLACE) {
                LOGGER.info("Server were stopped in place, restarting it")
                powerOffTask?.cancel()
            }
            startServer(scaleway, discord, pinger, instance, hostname, port)
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
        LOGGER.info("<{}> {}", playerName, it.rawMessage)
    }

    handler.addListener(ServerListPingEvent::class.java) {
        val status = Status.builder()
        pinger().exceptionHandler { _ ->
            status.playerInfo(MinecraftServer.getConnectionManager().onlinePlayers.size, 20)
                .description(Component.text("The server is sleeping. Connect you to wake it up!"))
            if (favicon != null) status.favicon(favicon.encodeToByteArray())
            it.status = status.build()
        }.responseHandler { data ->
            status.description(Component.text(data.description))
                .favicon(data.favicon.encodeToByteArray())
            val info = Status.PlayerInfo.builder().maxPlayers(data.maxPlayers)
            data.players.sample.forEach { p -> info.sample(NamedAndIdentified.of(p.name, UUID.fromString(p.id))) }
            it.status = status.playerInfo(info.build()).build()
        }.sync
    }

    handler.addListener(PlayerCommandEvent::class.java) {
        val playerName = PlainTextComponentSerializer.plainText().serialize(it.player.name)
        LOGGER.info("{}: /{}", playerName, it.command)
    }

    val commands = MinecraftServer.getCommandManager()
    commands.register(InfoCommand(scaleway, pinger))
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
                GZip.compress("logs/latest.log", "logs/$today.log.gz")
                Files.delete(Path.of("logs/latest.log"))
            }
        }
    }
}

fun startServer(scaleway: ScalewayAPI, discord: DiscordWebhookAPI, pinger: () -> MCPing<MCPingResponse>, instance: InstanceContainer, hostname: String, port: Int) {
    LOGGER.info("Starting the server")
    instance.players.forEach { it.sendMessage(Component.text("Starting the server for you...")) }
    discord.sendMessage(":arrows_counterclockwise: Starting the server")
    scaleway.startServer()
    TIMER.schedule(10*1000L, 10*1000L) {
        val state = scaleway.serverState()
        if (state != ScalewayAPI.ServerState.RUNNING) {
            LOGGER.info("Server is still starting... Current state: $state")
            return@schedule
        }
        LOGGER.info("Server started, waiting for the Minecraft server")
        instance.players.forEach { it.sendMessage(Component.text("Waiting for the Minecraft server...")) }
        discord.sendMessage(":hourglass: Waiting for the Minecraft server")

        setupServerTransfer(discord, pinger, instance, hostname, port)
        setupServerPowerOff(scaleway, discord)

        cancel()
    }
}

fun setupServerTransfer(discord: DiscordWebhookAPI, pinger: () -> MCPing<MCPingResponse>, instance: InstanceContainer, hostname: String, port: Int) {
    TIMER.schedule(15*1000L, 5*1000L) {
        pinger().exceptionHandler {
            LOGGER.info("Assuming that the Minecraft server is still starting...")
            LOGGER.trace("Trying to connect to $hostname:$port")
            LOGGER.trace("Pinger exception", it)
        }.responseHandler {
            instance.players.forEach {
                LOGGER.info {
                    val name = PlainTextComponentSerializer.plainText().serialize(it.name)
                    ParameterizedMessage("Sending player {} ({}) to the Minecraft server", name, it.uuid)
                }
                it.sendPacket(TransferPacket(hostname, port))
            }
            discord.sendMessage(":white_check_mark: Minecraft server started")
            cancel()
        }.sync
    }
}

fun setupServerPowerOff(scaleway: ScalewayAPI, discord: DiscordWebhookAPI) {
    powerOffTask?.cancel()
    powerOffTask = object : TimerTask() {
        override fun run() {
            val state = scaleway.serverState()
            if (state != ScalewayAPI.ServerState.STOPPED_IN_PLACE) return
            LOGGER.info("Powering off server")
            discord.sendMessage(":no_entry: Server stopped")
            scaleway.powerOffServer()
            cancel()
        }
    }
    TIMER.schedule(powerOffTask, 2*60*1000L, 30*1000L)
}