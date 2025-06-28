package world.anhgelus.world.architectsland.minecraftscalewayfrontend.api

import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import world.anhgelus.world.architectsland.minecraftscalewayfrontend.http.DiscordWebhookAPI
import world.anhgelus.world.architectsland.minecraftscalewayfrontend.http.ScalewayAPI

interface PluginHelper {
    fun getEventNode(): EventNode<Event>

    fun getDiscordWebhook(): DiscordWebhookAPI
    fun getScalewayAPI(): ScalewayAPI
}