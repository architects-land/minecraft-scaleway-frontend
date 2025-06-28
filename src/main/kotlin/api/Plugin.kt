package world.anhgelus.world.architectsland.minecraftscalewayfrontend.api

interface Plugin {
    fun onLoad(helper: PluginHelper)
    fun onUnload()
}