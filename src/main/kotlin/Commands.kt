package world.anhgelus.world.architectsland.minecraftscalewayfrontend

import net.minestom.server.command.builder.Command
import net.minestom.server.entity.Player
import net.minestom.server.network.packet.server.common.TransferPacket
import org.replydev.mcping.MCPinger
import org.replydev.mcping.PingOptions
import java.io.IOException

class InfoCommand(val scaleway: ScalewayAPI) : Command("info") {
    init {
        setDefaultExecutor { sender, _ ->
            sender.sendMessage("Server's state: ${scaleway.serverState()}")
        }
    }
}

class ConnectCommand(val pinger: MCPinger, val options: PingOptions) : Command("connect") {
    init {
        setDefaultExecutor { sender, _ ->
            try {
                pinger.fetchData()

                if (sender !is Player) {
                    sender.sendMessage("You are not a player :(")
                    return@setDefaultExecutor
                }

                sender.sendPacket(TransferPacket(options.hostname, options.port))
            } catch (e: IOException) {
                sender.sendMessage("Cannot connect to the server (not started yet)")
            }
        }
    }
}