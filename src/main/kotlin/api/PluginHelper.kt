package world.anhgelus.world.architectsland.minecraftscalewayfrontend.api

import world.anhgelus.world.architectsland.minecraftscalewayfrontend.api.event.EventListener

interface PluginHelper {
    fun registerListener(listener: EventListener)
}