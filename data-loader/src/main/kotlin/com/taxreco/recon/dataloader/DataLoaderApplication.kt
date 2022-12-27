package com.taxreco.recon.dataloader

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@EnableAsync
@EnableScheduling
@SpringBootApplication
class DataLoaderApplication

fun main(args: Array<String>) {
    System.setProperty("com.amazonaws.sdk.disableEc2Metadata", "true")
    runApplication<DataLoaderApplication>(*args)
}
