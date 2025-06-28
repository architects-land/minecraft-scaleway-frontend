package world.anhgelus.world.architectsland.minecraftscalewayfrontend.api

import net.minestom.server.entity.Player

abstract class EventListener {
    fun onTransfer(p: Player): Boolean {
        return false
    }
}