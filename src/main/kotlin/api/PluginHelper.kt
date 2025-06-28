package world.anhgelus.world.architectsland.minecraftscalewayfrontend.api

import net.minestom.server.event.GlobalEventHandler

interface PluginHelper {
    fun registerListener(listener: EventListener)
    fun getMinecraftEventHandler(): GlobalEventHandler
}