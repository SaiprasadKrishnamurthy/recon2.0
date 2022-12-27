package com.taxreco.recon.dataloader.bootstrap

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.aws.messaging.config.SimpleMessageListenerContainerFactory
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor


@Configuration
class SpringCloudSQSConfig {
    @Value("\${cloud.aws.region.static}")
    private lateinit var region: String

    @Value("\${cloud.aws.credentials.access-key}")
    private lateinit var accessKey: String

    @Value("\${cloud.aws.credentials.secret-key}")
    private lateinit var secretKey: String

    @Value("\${data.load.event.processing.threadpool.size}")
    private lateinit var processingThreadpoolSize: String

    @Value("\${data.load.event.processing.queue.size}")
    private lateinit var processingQueueSize: String

    @Bean
    fun queueMessagingTemplate(): QueueMessagingTemplate {
        return QueueMessagingTemplate(amazonSQSAsync())
    }

    fun amazonSQSAsync(): AmazonSQSAsync {
        val amazonSQSAsyncClientBuilder = AmazonSQSAsyncClientBuilder.standard()
        var amazonSQSAsync: AmazonSQSAsync? = null
        amazonSQSAsyncClientBuilder.withRegion(region)
        val basicAWSCredentials = BasicAWSCredentials(accessKey, secretKey)
        amazonSQSAsyncClientBuilder.withCredentials(AWSStaticCredentialsProvider(basicAWSCredentials))
        amazonSQSAsync = amazonSQSAsyncClientBuilder.build()
        return amazonSQSAsync
    }

    @Bean
    fun simpleMessageListenerContainerFactory(): SimpleMessageListenerContainerFactory? {
        val factory = SimpleMessageListenerContainerFactory()
        factory.setAmazonSqs(amazonSQSAsync())
        val asyncTaskExecutor = ThreadPoolTaskExecutor()
        asyncTaskExecutor.corePoolSize = processingThreadpoolSize.trim().toInt()
        asyncTaskExecutor.maxPoolSize = processingThreadpoolSize.trim().toInt()
        asyncTaskExecutor.setQueueCapacity(processingQueueSize.trim().toInt())
        asyncTaskExecutor.setThreadNamePrefix("threadPoolExecutor-SimpleMessageListenerContainer-")
        asyncTaskExecutor.initialize()
        factory.setTaskExecutor(asyncTaskExecutor)
        return factory
    }
}