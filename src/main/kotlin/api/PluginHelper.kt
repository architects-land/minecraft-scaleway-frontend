package world.anhgelus.architectsland.minecraftscalewayfrontend.api

import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import world.anhgelus.architectsland.minecraftscalewayfrontend.http.DiscordWebhookAPI
import world.anhgelus.architectsland.minecraftscalewayfrontend.http.ScalewayAPI

interface PluginHelper {
    fun getEventNode(): EventNode<Event>

    fun getDiscordWebhook(): DiscordWebhookAPI
    fun getScalewayAPI(): ScalewayAPI
}