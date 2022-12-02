package com.taxreco.recon.engine

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.taxreco.recon.engine.model.ReconciliationSetting
import com.taxreco.recon.engine.rdl.parser.*
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.charset.Charset

object RDLParser {
    @JvmStatic
    fun mains(args: Array<String>) {

        val q = FileUtils.readFileToString(
            File("/Users/saiprasadkrishnamurthy/2.0/recon2.0/eg.txt"),
            Charset.defaultCharset()
        )
        val lines = q.split("DEFINE".toRegex()).filter { it.isNotEmpty() }.map { "DEFINE $it" }
        val parsers = listOf(
            DatasourceRDLFragmentParser(),
            EntryWiseOneToOneChecksRDLFragmentParser(),
            FieldChecksRDLFragmentParser(),
            TotalsChecksRDLFragmentParser(),
            EntryWiseOneToManyChecksRDLFragmentParser()
        )
        val setting = ReconciliationSetting()

        lines.forEachIndexed { i, line ->
            val fragment = line.replace("\n", "").replace("\r", "")
            parsers.forEach { p ->
                p.parse((i + 1), fragment, setting)
            }
        }
        println(jacksonObjectMapper().writeValueAsString(setting))
    }
}