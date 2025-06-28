package world.anhgelus.architectsland.minecraftscalewayfrontend.api

interface Plugin {
    fun onLoad(helper: PluginHelper)
    fun onUnload(helper: PluginHelper)
}