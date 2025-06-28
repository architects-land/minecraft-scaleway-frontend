package world.anhgelus.world.architectsland.minecraftscalewayfrontend.api

import net.minestom.server.event.GlobalEventHandler
import world.anhgelus.world.architectsland.minecraftscalewayfrontend.api.event.EventListener

interface PluginHelper {
    fun registerListener(listener: EventListener)
    fun getMinecraftEventHandler(): GlobalEventHandler
}