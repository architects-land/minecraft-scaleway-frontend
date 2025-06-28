package world.anhgelus.architectsland.minecraftscalewayfrontend

import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.GZIPOutputStream

object GZip {
    fun compress(file: String, gzip: String) {
        val fis = FileInputStream(file)
        val fos = FileOutputStream(gzip)
        val gzipOS = GZIPOutputStream(fos)
        val buffer = ByteArray(1024)
        while (fis.read(buffer) > 0) gzipOS.write(buffer, 0, buffer.size)
        gzipOS.close()
        fos.close()
        fis.close()
    }
}