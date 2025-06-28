package world.anhgelus.world.architectsland.minecraftscalewayfrontend.plugins

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.jar.JarEntry
import java.util.jar.JarFile
import kotlin.io.path.exists

object PluginManager {
    private val files = ArrayList<PluginData>()

    fun init() {
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
            val data = PluginData.fromJson(ins.readBytes().toString(Charsets.UTF_8))
            files.add(data)
            jar.close()
            ins.close()
        }
    }
}