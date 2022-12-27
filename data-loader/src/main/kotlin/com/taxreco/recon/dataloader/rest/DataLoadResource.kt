package com.taxreco.recon.dataloader.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.taxreco.recon.dataloader.model.ApiUser
import com.taxreco.recon.dataloader.model.DataDefinitions
import com.taxreco.recon.dataloader.model.DataLoadRequest
import com.taxreco.recon.dataloader.model.DataLoadResponse
import com.taxreco.recon.dataloader.service.DataLoader
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.concurrent.thread

@RequestMapping("/api/v1")
@RestController
class DataLoadResource(
    @Value("\${data.load.work.dir}") private val dataLoaderWorkDir: String,
    private val dataLoader: DataLoader,
    private val apiUser: ApiUser
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        @JvmStatic
        private val logger = LoggerFactory.getLogger(javaClass.enclosingClass)
    }

    @PostMapping("/data-load/{name}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun dataLoad(
        @PathVariable("name") name: String,
        @RequestParam("tags", required = false, defaultValue = "") tags: String,
        @RequestPart("dataDefinition") mappingDefinitions: String,
        @RequestPart("file") files: MultipartFile
    ): DataLoadResponse {
        val definitions = jacksonObjectMapper().readValue(
            mappingDefinitions.replace("\\r", "").replace("\\n", ""),
            DataDefinitions::class.java
        )
        val jobId = UUID.randomUUID().toString()
        val zipDir = File(dataLoaderWorkDir + File.separator + jobId)
        FileUtils.forceMkdir(zipDir)
        val zipFile = zipDir.path + File.separator + jobId + ".zip"
        IOUtils.copy(files.inputStream, FileOutputStream(zipFile))
        val req = DataLoadRequest(
            jobId = jobId,
            name = name,
            zipFilePath = zipFile,
            apiUser = apiUser.clone(),
            dataDefinitions = definitions
        )
        // process in the background
        thread(start = true) {
            dataLoader.submitDataLoadJob(req)
        }
        return DataLoadResponse(jobId, name, tags.split(","))
    }
}