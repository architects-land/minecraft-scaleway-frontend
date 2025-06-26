package world.anhgelus.world.architectsland.minecraftscalewayfrontend

import net.lenni0451.mcping.MCPing
import net.lenni0451.mcping.responses.MCPingResponse
import net.minestom.server.command.builder.Command
import net.minestom.server.entity.Player
import net.minestom.server.network.packet.server.common.TransferPacket
import world.anhgelus.world.architectsland.minecraftscalewayfrontend.http.ScalewayAPI

class InfoCommand(val scaleway: ScalewayAPI, val pinger: () -> MCPing<MCPingResponse>) : Command("info") {
    init {
        setDefaultExecutor { sender, _ ->
            val state = scaleway.serverState()
            val base = "Instance's state: $state"
            if (state != ScalewayAPI.ServerState.RUNNING) {
                sender.sendMessage(base)
                return@setDefaultExecutor
            }
            pinger().exceptionHandler {
                sender.sendMessage("$base\nMinecraft server not started yet.")
            }.responseHandler {
                sender.sendMessage("$base\nMinecraft server is online. Use /connect to connect you.\nThis is a bug, please report it.")
            }.sync
        }
    }
}

class ConnectCommand(val pinger: () -> MCPing<MCPingResponse>, val hostname: String, val port: Int) : Command("connect") {
    init {
        setDefaultExecutor { sender, _ ->
            pinger().exceptionHandler {
                it.printStackTrace()
                sender.sendMessage("Cannot connect to the server (not started yet)")
            }.responseHandler {
                if (sender !is Player) {
                    sender.sendMessage("You are not a player :(")
                    return@responseHandler
                }
                sender.sendPacket(TransferPacket(hostname, port))
            }.sync
        }
    }
}