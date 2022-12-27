package com.taxreco.recon.dataloader.service

import com.taxreco.recon.dataloader.ftp.S3Manager
import com.taxreco.recon.dataloader.messaging.MessageSender
import com.taxreco.recon.dataloader.model.*
import com.taxreco.recon.dataloader.repository.DataLoadJobInfoRepository
import com.taxreco.recon.dataloader.repository.DataLoadRepository
import com.taxreco.recon.dataloader.util.FileSplitter
import com.taxreco.recon.dataloader.util.ZipUtils
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.dao.DeadlockLoserDataAccessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.File

@Service
class DefaultDataLoadService(
    @Value("\${data.chunk.size}") private val dataChunkSize: Int,
    @Value("\${data.load.max.retry.attempts.if.deadlocked}") private val maxRetryAttempts: Int,
    private val s3Manager: S3Manager,
    private val dataLoadJobInfoRepository: DataLoadJobInfoRepository,
    private val dataLoadRepository: DataLoadRepository,
    private val messageSender: MessageSender
) : DataLoadService {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        @JvmStatic
        private val logger = LoggerFactory.getLogger(javaClass.enclosingClass)
    }

    @Transactional
    override fun submitDataLoadJob(dataLoadRequest: DataLoadRequest): List<DataLoadEvent> {
        val zipFilePath = File(dataLoadRequest.zipFilePath)
        try {

            dataLoadRepository.dropIndices(
                dataLoadRequest.apiUser,
                dataLoadRequest.name,
                dataLoadRequest.dataDefinitions
            )

            val chunksDir = File(zipFilePath.parent + File.separator + "chunks")
            FileUtils.forceMkdir(chunksDir)
            ZipUtils.unzip(zipFilePath, zipFilePath.parent)
            File(zipFilePath.parent).listFiles()
                ?.filter { it.extension != "zip" && it.isFile }
                ?.forEach { file ->
                    FileSplitter.splitFile(file.path, chunksDir.path, dataChunkSize)
                }
            return chunksDir.listFiles()?.map { chunk ->
                // save the job
                dataLoadJobInfoRepository.save(
                    dataLoadRequest.apiUser,
                    DataLoadJobInfo(
                        jobId = dataLoadRequest.jobId,
                        name = dataLoadRequest.name,
                        chunkName = chunk.name,
                        userId = dataLoadRequest.apiUser.id,
                        tenant = dataLoadRequest.apiUser.tenant,
                        dataDefinitions = dataLoadRequest.dataDefinitions
                    )
                )
                s3Manager.upload(dataLoadRequest.apiUser, chunk, "data_load/${dataLoadRequest.jobId}")
                dataLoadJobInfoRepository.updateStatus(
                    dataLoadRequest.apiUser,
                    chunk.name,
                    dataLoadRequest.jobId,
                    DataLoadJobStatus.ChunkUploadedToStore
                )
                DataLoadEvent(
                    jobId = dataLoadRequest.jobId,
                    name = dataLoadRequest.name,
                    tags = dataLoadRequest.tags,
                    apiUser = dataLoadRequest.apiUser,
                    chunkName = chunk.name,
                    dataDefinitions = dataLoadRequest.dataDefinitions,
                    tenant = dataLoadRequest.apiUser.tenant,
                    objectStorageId = dataLoadRequest.apiUser.objectStorageId
                )
            } ?: emptyList()
        } finally {
            FileUtils.deleteQuietly(zipFilePath.parentFile)
        }
    }

    @Transactional
    override fun loadData(dataLoadEvent: DataLoadEvent) {
        try {
            dataLoadRepository.setupTables(dataLoadEvent)
            dataLoadRepository.loadData(dataLoadEvent)
            dataLoadJobInfoRepository.updateStatus(
                dataLoadEvent.apiUser,
                dataLoadEvent.chunkName,
                dataLoadEvent.jobId,
                DataLoadJobStatus.ChunkLoadedInDB,
                true
            )
            if (dataLoadJobInfoRepository.allChunksLoadedInDB(
                    dataLoadEvent.apiUser,
                    dataLoadEvent.chunkName,
                    dataLoadEvent.jobId
                )
            ) {
                dataLoadRepository.setupIndices(dataLoadEvent)
                logger.info(" Created All the necessary indexes in DB ")
            }
        } catch (deadlock: DeadlockLoserDataAccessException) {
            if (dataLoadEvent.attemptNo <= maxRetryAttempts) {
                logger.warn(" Retrying for chunk :${dataLoadEvent.chunkName} due to deadlock (Current Retry Attempt No: ${dataLoadEvent.attemptNo}")
                Thread.sleep(1000)
                messageSender.sendDataLoadEvent(dataLoadEvent.copy(attemptNo = dataLoadEvent.attemptNo + 1))
            } else {
                logger.error(" Max Retry Attempts Exceeded for job: ${dataLoadEvent.jobId}, Chunk: ${dataLoadEvent.chunkName}")
                dataLoadJobInfoRepository.updateStatusWithError(
                    dataLoadEvent.apiUser,
                    dataLoadEvent.chunkName,
                    dataLoadEvent.jobId,
                    DataLoadJobStatus.ChunkFailedToLoadInDB,
                    "Max retry attempts exceeded due to database deadlock"
                )
            }
        } catch (ex: Exception) {
            logger.error(" Exception for job: ${dataLoadEvent.jobId}, Chunk: ${dataLoadEvent.chunkName}", ex)
            dataLoadJobInfoRepository.updateStatusWithError(
                dataLoadEvent.apiUser,
                dataLoadEvent.chunkName,
                dataLoadEvent.jobId,
                DataLoadJobStatus.ChunkFailedToLoadInDB,
                ex.message ?: ex.localizedMessage
            )
        }
    }
}