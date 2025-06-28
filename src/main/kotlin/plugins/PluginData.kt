package world.anhgelus.world.architectsland.minecraftscalewayfrontend.plugins

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class PluginData(val filename: String, val name: String, val main: String, val version: String, val author: String) {
    companion object {
        fun fromJson(s: String): PluginData {
            return Json.decodeFromString(s)
        }
    }
}
