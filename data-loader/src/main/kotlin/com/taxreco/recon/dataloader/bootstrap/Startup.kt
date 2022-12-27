package com.taxreco.recon.dataloader.bootstrap

import org.apache.commons.io.FileUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.File


@Configuration
class Startup(@Value("\${data.load.work.dir}") private val dataLoaderWorkDir: String) {

    @Bean
    fun init() = CommandLineRunner {
        FileUtils.forceMkdir(File(dataLoaderWorkDir))
    }
}