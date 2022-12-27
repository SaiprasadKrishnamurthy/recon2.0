package com.taxreco.recon.dataloader.rest

import com.taxreco.recon.dataloader.model.ApiUser
import com.taxreco.recon.dataloader.model.JobDetailedView
import com.taxreco.recon.dataloader.model.JobListView
import com.taxreco.recon.dataloader.repository.DataLoadJobInfoRepository
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/v1")
@RestController
class DataLoadJobInfoResource(
    private val apiUser: ApiUser,
    private val jobInfoRepository: DataLoadJobInfoRepository
) {
    @GetMapping("/jobs", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun jobs(): List<JobListView> {
        return jobInfoRepository.findAllJobs(apiUser, 500)
    }

    @GetMapping("/job/{jobId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun job(@PathVariable("jobId") jobId: String): JobDetailedView {
        return jobInfoRepository.findJobDetail(apiUser, jobId)
    }
}