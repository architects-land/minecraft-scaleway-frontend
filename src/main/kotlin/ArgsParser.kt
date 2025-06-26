package world.anhgelus.world.architectsland.minecraftscalewayfrontend

import java.nio.file.Files
import java.nio.file.Path

class ArgsParser(args: Array<String>) {
    val map: MutableMap<String, String> = mutableMapOf()

    init {
        val envPath = Path.of(".env")
        if (Files.exists(envPath)) {
            val lines = Files.lines(envPath)
            lines.forEach { line ->
                if (line.startsWith("#") || line.isEmpty()) return@forEach
                val splits = line.split("=")
                if (splits.size != 2) throw IllegalArgumentException("Invalid line '$line' format in .env")
                map[splits[0].trim().lowercase().replace("_", "-")] = splits[1].trim()
            }
            lines.close()
        }

        var i = 0;
        while (i < args.size) {
            val current = args[i]
            if (current[0] == '-') {
                if (current.length > 1 && current[1] == '-') {
                    if (i == args.size - 1)
                        throw IllegalArgumentException("Invalid arguments.")
                    val next = args[++i]
                    map[current.substring(2)] = next
                } else {
                    map[current.substring(1)] = current.substring(1)
                }
            } else {
                map[current] = current
            }
            i++
        }
    }

    fun has(key: String): Boolean {
        return map[key] != null
    }

    fun get(key: String): String? {
        return map[key]
    }

    fun getOrDefault(key: String, defaultValue: String): String {
        return map.getOrDefault(key, defaultValue)
    }

    fun getIntOrDefault(key: String, defaultValue: Int): Int {
        val v = map[key]
        if (v == null) return defaultValue
        return v.toInt()
    }
}