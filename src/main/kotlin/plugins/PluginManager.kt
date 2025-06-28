package world.anhgelus.world.architectsland.minecraftscalewayfrontend.plugins

import net.lenni0451.mcping.responses.MCPingResponse
import net.minestom.server.entity.Player
import net.minestom.server.event.GlobalEventHandler
import world.anhgelus.world.architectsland.minecraftscalewayfrontend.LOGGER
import world.anhgelus.world.architectsland.minecraftscalewayfrontend.api.EventListener
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

object PluginManager : PluginHelper {
    private val files = ArrayList<PluginData>()
    private val loaded = ArrayList<Plugin>()
    private val listeners = mutableListOf<EventListener>()
    private lateinit var handler: GlobalEventHandler
    private lateinit var discord: DiscordWebhookAPI
    private lateinit var scaleway: ScalewayAPI

    fun init(handler: GlobalEventHandler, scaleway: ScalewayAPI, discord: DiscordWebhookAPI) {
        this.handler = handler
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
            val data = PluginData.fromJson(ins.readBytes().toString(Charsets.UTF_8), it.name)
            files.add(data)
            jar.close()
            ins.close()
        }
    }

    fun start(): Int {
        var c = 0
        files.forEach {
            try {
                val loader = URLClassLoader.newInstance(arrayOf(URI("jar:file:${it.filename}!/").toURL()))
                val pl = loader.loadClass(it.main).getDeclaredConstructor().newInstance() as Plugin
                pl.onLoad(this)
                loaded.add(pl)
                c++
            } catch (e: Exception) {
                LOGGER.error("Error while loading ${it.filename}", e)
            }
        }
        return c
    }

    fun stop() {
        loaded.forEach { it.onUnload(this) }
    }

    override fun registerListener(listener: EventListener) {
        listeners.add(listener)
    }

    override fun getMinecraftEventHandler(): GlobalEventHandler {
        return handler
    }

    override fun getDiscordWebhook(): DiscordWebhookAPI {
        return discord
    }

    override fun getScalewayAPI(): ScalewayAPI {
        return scaleway
    }

    fun emitTransfer(p: Player): Boolean {
        return listeners.any { it.onTransfer(p) }
    }

    fun emitInstanceStart(): Boolean {
        return listeners.any { it.onInstanceStart() }
    }

    fun emitInstanceStarted() {
        listeners.forEach { it.onInstanceStarted() }
    }

    fun emitMinecraftStarted(ping: MCPingResponse) {
        listeners.forEach { it.onMinecraftStarted(ping) }
    }

    fun emitInstanceStop(): Boolean {
        return listeners.any { it.onInstanceStop() }
    }

    fun emitInstanceStopped() {
        listeners.forEach { it.onInstanceStopped() }
    }
}