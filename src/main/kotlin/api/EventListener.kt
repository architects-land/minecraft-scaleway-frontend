package world.anhgelus.world.architectsland.minecraftscalewayfrontend.api

import net.minestom.server.entity.Player

abstract class EventListener {
    /**
     * Called when a player is transferred to the Minecraft server
     * @return true if you want to cancel the event
     */
    fun onTransfer(p: Player): Boolean {
        return false
    }

    /**
     * Called when the instance is requested to start
     * @return true if you want to cancel the event
     */
    fun onInstanceStart(): Boolean {
        return false
    }

    /**
     * Called when the instance is started
     */
    fun onInstanceStarted() {}
}