package world.anhgelus.world.architectsland.minecraftscalewayfrontend.plugins

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class PluginData(var filename: String = "", val name: String, val main: String, val version: String, val author: String) {
    companion object {
        fun fromJson(s: String, filename: String): PluginData {
            val o = Json.decodeFromString<PluginData>(s)
            o.filename = filename
            return o
        }
    }
}
