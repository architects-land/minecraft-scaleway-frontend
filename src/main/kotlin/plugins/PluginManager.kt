package world.anhgelus.world.architectsland.minecraftscalewayfrontend.plugins

import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import world.anhgelus.world.architectsland.minecraftscalewayfrontend.LOGGER
import world.anhgelus.world.architectsland.minecraftscalewayfrontend.api.Plugin
import world.anhgelus.world.architectsland.minecraftscalewayfrontend.api.PluginHelper
import world.anhgelus.world.architectsland.minecraftscalewayfrontend.http.DiscordWebhookAPI
import world.anhgelus.world.architectsland.minecraftscalewayfrontend.http.ScalewayAPI
import java.io.File
import java.net.URI
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Paths
import java.util.jar.JarEntry
import java.util.jar.JarFile
import kotlin.io.path.exists

object PluginManager {
    private val pluginData = ArrayList<PluginData>()
    private val loaded = ArrayList< Pair<Plugin, String> >()
    private lateinit var node: EventNode<Event>
    private lateinit var discord: DiscordWebhookAPI
    private lateinit var scaleway: ScalewayAPI

    fun init(node: EventNode<Event>, scaleway: ScalewayAPI, discord: DiscordWebhookAPI) {
        this.node = node
        this.scaleway = scaleway
        this.discord = discord

        val results: MutableList<File> = ArrayList()

        val path = Paths.get("./plugins")
        if (!path.exists()) {
            path.toFile().mkdir()
        }

        Files.newDirectoryStream(path).use { stream ->
            for (p in stream) {
                if (p != null && p.fileName.toString().endsWith(".jar")) {
                    results.add(p.toFile())
                }
            }
        }

        results.forEach {
            val jar = JarFile(it)
            val conf: JarEntry = jar.getJarEntry("plugin.json")
                ?: throw InvalidPluginException(InvalidPluginException.Reason.INVALID_PLUGIN_YML, "not found")
            val ins = jar.getInputStream(conf)
                ?: throw InvalidPluginException(InvalidPluginException.Reason.INVALID_PLUGIN_YML, "empty")
            val data = PluginData.fromJson(ins.readBytes().toString(Charsets.UTF_8), it.path)
            pluginData.add(data)
            jar.close()
            ins.close()
        }
    }

    fun start(): Int {
        var c = 0
        pluginData.forEach {
            try {
                val loader = URLClassLoader.newInstance(arrayOf(URI("jar:file:${it.filename}!/").toURL()))
                val pl = loader.loadClass(it.main).getDeclaredConstructor().newInstance() as Plugin
                pl.onLoad(Helper(it.name))
                loaded.add(Pair(pl, it.name))
                c++
            } catch (e: Exception) {
                LOGGER.error("Error while loading ${it.filename}", e)
            }
        }
        return c
    }

    fun stop() {
        loaded.forEach { it.first.onUnload(Helper(it.second)) }
    }

    private class Helper(val name: String) : PluginHelper {
        val cNode: EventNode<Event> = EventNode.all(name)

        init {
            node.addChild(cNode)
        }

        override fun getEventNode(): EventNode<Event> {
            return cNode
        }

        override fun getDiscordWebhook(): DiscordWebhookAPI {
            return discord
        }

        override fun getScalewayAPI(): ScalewayAPI {
            return scaleway
        }
    }
}