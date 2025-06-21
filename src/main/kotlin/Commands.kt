package world.anhgelus.world.architectsland.minecraftscalewayfrontend

import net.lenni0451.mcping.MCPing
import net.lenni0451.mcping.responses.MCPingResponse
import net.minestom.server.command.builder.Command
import net.minestom.server.entity.Player
import net.minestom.server.network.packet.server.common.TransferPacket

class InfoCommand(val scaleway: ScalewayAPI) : Command("info") {
    init {
        setDefaultExecutor { sender, _ ->
            sender.sendMessage("Server's state: ${scaleway.serverState()}")
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