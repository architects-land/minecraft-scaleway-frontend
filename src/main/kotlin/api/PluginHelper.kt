package world.anhgelus.world.architectsland.minecraftscalewayfrontend.api

import net.minestom.server.event.GlobalEventHandler
import world.anhgelus.world.architectsland.minecraftscalewayfrontend.http.DiscordWebhookAPI
import world.anhgelus.world.architectsland.minecraftscalewayfrontend.http.ScalewayAPI

interface PluginHelper {
    fun registerListener(listener: EventListener)
    fun getMinecraftEventHandler(): GlobalEventHandler

    fun getDiscordWebhook(): DiscordWebhookAPI
    fun getScalewayAPI(): ScalewayAPI
}