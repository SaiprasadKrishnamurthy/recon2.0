package com.taxreco.recon.dataloader.util

import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicInteger

object FileSplitter {

    fun splitFile(
        file: String,
        outDir: String,
        limit: Int = 1000,
        keyFn: ((headers: Array<String>) -> Int)? = null
    ): List<File> {
        val header = Files.lines(Paths.get(file), Charset.defaultCharset())
            .findFirst()
            .get()
        FileUtils.forceMkdir(File(outDir))
        val input = File(file)
        val chunk = AtomicInteger(1)
        val outFiles = mutableListOf<File>()
        println(" Processing Chunk ${chunk.get()}")
        val firstFile =
            File(outDir + File.separator + input.nameWithoutExtension + "_" + chunk.getAndIncrement() + "." + input.extension)
        var out = PrintWriter(FileWriter(firstFile), true)
        outFiles.add(firstFile)

        val counter = AtomicInteger(1)

        Files.lines(Paths.get(file), Charset.defaultCharset())
            .forEach { line ->
                if (counter.toInt() % limit == 0) {
                    out.close()
                    val fileName = input.nameWithoutExtension + "_" + chunk.getAndIncrement() + "." + input.extension
                    val outFile = File(outDir + File.separator + fileName)
                    out = PrintWriter(FileWriter(outFile), true)
                    out.println(header)
                    outFiles.add(outFile)
                    counter.set(1)
                } else {
                    out.println(line)
                    counter.getAndIncrement()
                }
            }
        out.close()
        return outFiles

    }
}