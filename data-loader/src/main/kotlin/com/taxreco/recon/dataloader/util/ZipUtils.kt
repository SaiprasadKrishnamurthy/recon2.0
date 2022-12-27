package com.taxreco.recon.dataloader.util

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipFile

object ZipUtils {
    fun unzip(zipFilePath: File, destDirectory: String) {
        File(destDirectory).run {
            if (!exists()) {
                mkdirs()
            }
        }
        ZipFile(zipFilePath).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                zip.getInputStream(entry).use { input ->
                    val filePath = destDirectory + File.separator + entry.name
                    // skip the hidden files - MACOS generates hidden files.
                    if (!File(entry.name).name.startsWith(".")) {
                        if (!entry.isDirectory) {
                            // if the entry is a file, extracts it
                            extractFile(input, filePath)
                        } else {
                            // if the entry is a directory, make the directory
                            val dir = File(filePath)
                            dir.mkdir()
                        }
                    }
                }
            }
        }
    }

    private fun extractFile(inputStream: InputStream, destFilePath: String) {
        val bos = BufferedOutputStream(FileOutputStream(destFilePath))
        val bytesIn = ByteArray(4096)
        var read: Int
        while (inputStream.read(bytesIn).also { read = it } != -1) {
            bos.write(bytesIn, 0, read)
        }
        bos.close()
    }
}