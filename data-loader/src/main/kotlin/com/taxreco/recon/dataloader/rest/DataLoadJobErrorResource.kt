package com.taxreco.recon.dataloader.rest

import com.taxreco.recon.dataloader.model.*
import com.taxreco.recon.dataloader.repository.DataLoadJobErrorsRepository
import com.taxreco.recon.dataloader.repository.DataLoadJobInfoRepository
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*
import kotlin.collections.LinkedHashMap

@RequestMapping("/api/v1")
@RestController
class DataLoadJobErrorResource(
    private val apiUser: ApiUser,
    private val errorsRepository: DataLoadJobErrorsRepository
) {
    @GetMapping("/error-summary/{jobId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun jobErrorSummary(@PathVariable("jobId") jobId: String): ErrorSummaryView {
        val findErrorSummary = errorsRepository.findErrorSummary(jobId, apiUser)
        if (findErrorSummary == null) {
            throw MissingResourceException("", "", "")
        } else {
            return findErrorSummary
        }
    }

    @GetMapping("/error-detail/{jobId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun jobErrorDetail(@PathVariable("jobId") jobId: String): List<LinkedHashMap<String, Any?>> {
        val errorDetails = errorsRepository.findErrorDetails(jobId, apiUser)
        if (errorDetails == null) {
            throw MissingResourceException("", "", "")
        } else {
            return errorDetails
        }
    }
}