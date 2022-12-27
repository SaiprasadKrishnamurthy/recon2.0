package com.taxreco.recon.dataloader.ftp

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.ListObjectsRequest
import com.amazonaws.services.s3.model.ObjectListing
import com.amazonaws.services.s3.model.ObjectMetadata
import com.taxreco.recon.dataloader.model.ApiUser
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.util.*


@Service
class S3Manager(
    @Value("\${cloud.aws.credentials.access-key}") private val accessKey: String,
    @Value("\${cloud.aws.credentials.secret-key}") private val secretKey: String,
    @Value("\${data.load.work.dir}") private val downloadDir: String
) {

    fun findFiles(user: ApiUser, subDir: String): List<String> {
        val credentials = BasicAWSCredentials(
            accessKey, secretKey
        )
        val s3client = AmazonS3ClientBuilder
            .standard()
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.AP_SOUTH_1)
            .build()
        val tenant = if (user.tenant == "public") "taxreco" else user.objectStorageId
        val objectListing = s3client.listObjects(tenant, subDir)
        val files = mutableListOf<String>()
        for (os in objectListing.objectSummaries) {
            if (!os.key.endsWith("/")) {
                println(os)
                val fileName = os.key.substring(os.key.lastIndexOf("/") + 1)
                files.add(fileName)
            }
        }
        return files
    }

    fun findFileswithFolderPath(user: ApiUser, subDir: String): List<String> {
        val credentials = BasicAWSCredentials(
            accessKey, secretKey
        )
        val s3client = AmazonS3ClientBuilder
            .standard()
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.AP_SOUTH_1)
            .build()
        val tenant = if (user.tenant == "public") "taxreco" else user.objectStorageId
        val objectListing = s3client.listObjects(tenant, subDir)
        val files = mutableListOf<String>()
        for (os in objectListing.objectSummaries) {
            println(os)
            if (!os.key.endsWith("/")) {
                val fileName = os.key.substring(os.key.indexOf("/") + 1) + "/${os.size}kb/${os.lastModified}"
                files.add(fileName)
            }
        }
        return files
    }


    fun download(user: ApiUser, s3Dir: String, toLocalDir: String = ""): List<File> {
        val credentials = BasicAWSCredentials(
            accessKey, secretKey
        )
        val s3client = AmazonS3ClientBuilder
            .standard()
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.AP_SOUTH_1)
            .build()
        val tenant = if (user.tenant == "public") "taxreco" else user.objectStorageId
        val objectListing = s3client.listObjects(tenant, s3Dir)
        val files = mutableListOf<File>()
        for (os in objectListing.objectSummaries) {
            if (!os.key.endsWith("/")) {
                val folderName = os.key.substringBefore("/")
                if (s3Dir == folderName) {
                    val fileName = os.key.substring(os.key.lastIndexOf("/") + 1)
                    val s3object = s3client.getObject(tenant, os.key)
                    val inputStream = s3object.objectContent
                    val file =
                        if (toLocalDir.isNotEmpty()) File("$downloadDir/$toLocalDir/$fileName") else File("$downloadDir/$fileName")
                    FileUtils.copyInputStreamToFile(inputStream, file)
                    files.add(file)
                }
            }
        }
        return files
    }

    fun download(user: ApiUser, parentS3Dir: String, s3Dir: String, toLocalDir: String = ""): List<File> {
        val credentials = BasicAWSCredentials(
            accessKey, secretKey
        )
        val s3client = AmazonS3ClientBuilder
            .standard()
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.AP_SOUTH_1)
            .build()
        val tenant = if (user.tenant == "public") "taxreco" else user.objectStorageId
        val objectListing = s3client.listObjects(tenant, parentS3Dir)
        val files = mutableListOf<File>()
        for (os in objectListing.objectSummaries) {
            if (!os.key.endsWith("/")) {
                val folderName = os.key.substringBefore("/")
                if (s3Dir == folderName) {
                    val fileName = os.key.substring(os.key.lastIndexOf("/") + 1)
                    val s3object = s3client.getObject(tenant, os.key)
                    val inputStream = s3object.objectContent
                    val file =
                        if (toLocalDir.isNotEmpty()) File("$downloadDir/$toLocalDir/$fileName") else File("$downloadDir/$fileName")
                    FileUtils.copyInputStreamToFile(inputStream, file)
                    files.add(file)
                }
            }
        }
        return files
    }

    fun downloadOne(user: ApiUser, s3Dirs: String, toLocalDir: String = ""): File? {
        val credentials = BasicAWSCredentials(
            accessKey, secretKey
        )
        val s3client = AmazonS3ClientBuilder
            .standard()
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.AP_SOUTH_1)
            .build()
        val tenant = if (user.tenant == "public") "taxreco" else user.objectStorageId

        val listReq = ListObjectsRequest()
        listReq.withBucketName(tenant).withPrefix(s3Dirs)
        val objectListing = s3client.listObjects(listReq)
        for (os in objectListing.objectSummaries) {
            if (!os.key.endsWith("/")) {
                val fileName = os.key.substring(os.key.lastIndexOf("/") + 1)
                val s3object = s3client.getObject(tenant, os.key)
                val inputStream = s3object.objectContent
                val file =
                    if (toLocalDir.isNotEmpty()) File("$downloadDir/$toLocalDir/$fileName") else File("$downloadDir/$fileName")
                FileUtils.copyInputStreamToFile(inputStream, file)
                return file
            }
        }
        return null
    }

    fun listFiles(user: ApiUser, s3Dir: String, toLocalDir: String = ""): List<String> {
        val credentials = BasicAWSCredentials(
            accessKey, secretKey
        )
        val s3client = AmazonS3ClientBuilder
            .standard()
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.AP_SOUTH_1)
            .build()
        val tenant = if (user.tenant == "public") "taxreco" else user.objectStorageId

        val listReq = ListObjectsRequest()
        listReq.withBucketName(tenant).withPrefix(s3Dir)

        val objectListing = s3client.listObjects(listReq)
        val files = mutableListOf<String>()
        for (os in objectListing.objectSummaries) {
            if (!os.key.endsWith("/")) {
                val fileName = os.key.substring(os.key.lastIndexOf("/") + 1)
                files.add(fileName)
            }
        }
        return files
    }


    fun downloadFile(user: ApiUser, dir: String, file: String): File? {
        val credentials = BasicAWSCredentials(
            accessKey, secretKey
        )
        val s3client = AmazonS3ClientBuilder
            .standard()
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.AP_SOUTH_1)
            .build()
        val tenant = if (user.tenant == "public") "taxreco" else user.objectStorageId
        val objectListing = s3client.listObjects(tenant, dir)
        val files = mutableListOf<File>()
        for (os in objectListing.objectSummaries) {
            if (!os.key.endsWith("/")) {
                val fileName = os.key.substring(os.key.lastIndexOf("/") + 1)
                if (fileName == file) {
                    val s3object = s3client.getObject(tenant, os.key)
                    val inputStream = s3object.objectContent
                    val downloadedFile = File("$downloadDir/$tenant/$fileName")
                    FileUtils.copyInputStreamToFile(inputStream, downloadedFile)
                    files.add(downloadedFile)
                }
            }
        }
        return files.firstOrNull()
    }

    fun upload(user: ApiUser, file: File) {
        val credentials = BasicAWSCredentials(
            accessKey, secretKey
        )
        val s3client = AmazonS3ClientBuilder
            .standard()
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.AP_SOUTH_1)
            .build()
        val tenant = if (user.tenant == "public") "taxreco" else user.objectStorageId
        s3client.putObject(tenant, "reports/${file.name}", file)
    }

    // upload file in a particular folder
    fun upload(user: ApiUser, file: File, folder: String) {
        val credentials = BasicAWSCredentials(
            accessKey, secretKey
        )
        val s3client = AmazonS3ClientBuilder
            .standard()
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.AP_SOUTH_1)
            .build()
        val tenant = if (user.tenant == "public") "taxreco" else user.objectStorageId
        s3client.putObject(tenant, "$folder/${file.name}", file)
    }

    fun upload(user: ApiUser, fileName: String, stream: InputStream, folder: String) {
        val credentials = BasicAWSCredentials(
            accessKey, secretKey
        )
        val contentBytes = IOUtils.toByteArray(stream)
        val metadata = ObjectMetadata()
        metadata.contentLength = contentBytes.size.toLong()
        val s3client = AmazonS3ClientBuilder
            .standard()
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.AP_SOUTH_1)
            .build()
        val tenant = if (user.tenant == "public") "taxreco" else user.objectStorageId
        s3client.putObject(tenant, "$folder/$fileName", ByteArrayInputStream(contentBytes), metadata)

    }

    fun move(user: ApiUser, file: File, sourceBucket: String, targetBucket: String) {
        val credentials = BasicAWSCredentials(
            accessKey, secretKey
        )
        val s3client = AmazonS3ClientBuilder
            .standard()
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.AP_SOUTH_1)
            .build()
        val tenant = if (user.tenant == "public") "taxreco" else user.objectStorageId

        s3client.putObject(tenant, "$targetBucket/${file.name}", file)
        s3client.deleteObject(tenant, "$sourceBucket/${file.name}")
    }

    fun uploadToBucket(bucket: String, fileName: String, inputStream: InputStream) {
        println("bucket >> $bucket fileName >> $fileName")
        val contentBytes = IOUtils.toByteArray(inputStream)
        val metadata = ObjectMetadata()
        metadata.contentLength = contentBytes.size.toLong()
        val credentials = BasicAWSCredentials(
            accessKey, secretKey
        )
        val s3client = AmazonS3ClientBuilder
            .standard()
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.AP_SOUTH_1)
            .build()
        s3client.putObject(bucket, fileName, ByteArrayInputStream(contentBytes), metadata)
    }

    fun downloadFromBucket(bucket: String, inputFileName: String): File? {
        val credentials = BasicAWSCredentials(
            accessKey, secretKey
        )
        val s3client = AmazonS3ClientBuilder
            .standard()
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.AP_SOUTH_1)
            .build()
        val objectListing = s3client.listObjects(bucket)
        val files = mutableListOf<File>()
        for (os in objectListing.objectSummaries) {
            if (!os.key.endsWith("/")) {
                val fileName = os.key.substring(os.key.lastIndexOf("/") + 1)
                if (fileName == inputFileName) {
                    val s3object = s3client.getObject(bucket, os.key)
                    val inputStream = s3object.objectContent
                    val file = File("$downloadDir/$fileName")
                    FileUtils.copyInputStreamToFile(inputStream, file)
                    files.add(file)
                }
            }
        }
        return files.firstOrNull()
    }

    fun deleteAllInputSource(user: ApiUser) {
        val credentials = BasicAWSCredentials(
            accessKey, secretKey
        )
        val s3client = AmazonS3ClientBuilder
            .standard()
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.AP_SOUTH_1)
            .build()
        val tenant = if (user.tenant == "public") "taxreco" else user.objectStorageId
        //removeAllFilefromAS3Folder(tenant,"26as",s3client)
        removeAllFilefromAS3Folder(tenant, "sales", s3client)
        removeAllFilefromAS3Folder(tenant, "purchase", s3client)
    }

    fun deleteSales(user: ApiUser) {
        val credentials = BasicAWSCredentials(
            accessKey, secretKey
        )
        val s3client = AmazonS3ClientBuilder
            .standard()
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.AP_SOUTH_1)
            .build()
        val tenant = if (user.tenant == "public") "taxreco" else user.objectStorageId
        removeAllFilefromAS3Folder(tenant, "sales", s3client)
    }

    fun delete26AS(user: ApiUser) {
        val credentials = BasicAWSCredentials(
            accessKey, secretKey
        )
        val s3client = AmazonS3ClientBuilder
            .standard()
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.AP_SOUTH_1)
            .build()
        val tenant = if (user.tenant == "public") "taxreco" else user.objectStorageId
        removeAllFilefromAS3Folder(tenant, "26as", s3client)
    }

    fun deleteExpense(user: ApiUser, financialYear: String, financialMonth: String, fileName: String) {
        val credentials = BasicAWSCredentials(
            accessKey, secretKey
        )
        val s3client = AmazonS3ClientBuilder
            .standard()
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.AP_SOUTH_1)
            .build()
        val tenant = if (user.tenant == "public") "taxreco" else user.objectStorageId
        return s3client.deleteObject(tenant, "expenses/$financialYear/$financialMonth/$fileName")
    }

    fun deleteAllOutputData(user: ApiUser) {
        val credentials = BasicAWSCredentials(
            accessKey, secretKey
        )
        val s3client = AmazonS3ClientBuilder
            .standard()
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.AP_SOUTH_1)
            .build()
        val tenant = if (user.tenant == "public") "taxreco" else user.objectStorageId
        removeAllFilefromAS3Folder(tenant, "reports", s3client)
        removeAllFilefromAS3Folder(tenant, "masterdata_templates", s3client)
    }

    fun removeFiles(user: ApiUser, folder: String) {
        val credentials = BasicAWSCredentials(
            accessKey, secretKey
        )
        val s3client = AmazonS3ClientBuilder
            .standard()
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.AP_SOUTH_1)
            .build()
        val tenant = if (user.tenant == "public") "taxreco" else user.objectStorageId
        val taxBookFiles = s3client.listObjects(tenant, folder)
        for (objectSummary in taxBookFiles.objectSummaries) {
            if (!objectSummary.key.endsWith('/')) {
                println("removing ${objectSummary.key}")
                s3client.deleteObject(tenant, objectSummary.key)
            }
        }


    }

    fun removeAllFilefromAS3Folder(tenant: String?, folder: String, s3client: AmazonS3) {
        val taxBookFiles = s3client.listObjects(tenant, folder)
        for (objectSummary in taxBookFiles.objectSummaries) {
            if (!objectSummary.key.endsWith('/')) {
                println("removing ${objectSummary.key}")
                s3client.deleteObject(tenant, objectSummary.key)
            }
        }
    }

    fun delete(user: ApiUser, folder: String, file: String) {
        val tenant = if (user.tenant == "public") "taxreco" else user.objectStorageId
        val credentials = BasicAWSCredentials(
            accessKey, secretKey
        )
        val s3client = AmazonS3ClientBuilder
            .standard()
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.AP_SOUTH_1)
            .build()
        val listReq = ListObjectsRequest()
        listReq.withBucketName(tenant).withPrefix(folder)
        val files = s3client.listObjects(listReq)
        for (objectSummary in files.objectSummaries) {
            if (!objectSummary.key.endsWith('/')) {
                val fileName = objectSummary.key.substring(objectSummary.key.lastIndexOf("/") + 1)
                if (fileName == file) {
                    s3client.deleteObject(tenant, objectSummary.key)
                }
            }
        }
    }

    fun downloadFileFromDir(user: ApiUser, parentDir: String, file: String, toLocalDir: File): File? {
        val tenant = if (user.tenant == "public") "taxreco" else user.objectStorageId
        val credentials = BasicAWSCredentials(
            accessKey, secretKey
        )
        val s3client = AmazonS3ClientBuilder
            .standard()
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.AP_SOUTH_1)
            .build()
        val listReq = ListObjectsRequest()
        listReq.withBucketName(tenant).withPrefix(parentDir)
        val files = s3client.listObjects(listReq)
        for (objectSummary in files.objectSummaries) {
            if (!objectSummary.key.endsWith('/')) {
                val fileName = objectSummary.key.substring(objectSummary.key.lastIndexOf("/") + 1)
                if (fileName == file) {
                    val s3object = s3client.getObject(tenant, objectSummary.key)
                    val inputStream = s3object.objectContent
                    val localFile = File("${toLocalDir.path}${File.separator}$fileName")
                    FileUtils.copyInputStreamToFile(inputStream, localFile)
                    return localFile
                }
            }
        }
        return null
    }

    fun deleteRecursively(user: ApiUser, parentDir: String) {
        val tenant = if (user.tenant == "public") "taxreco" else user.objectStorageId
        val credentials = BasicAWSCredentials(
            accessKey, secretKey
        )
        val s3client = AmazonS3ClientBuilder
            .standard()
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.AP_SOUTH_1)
            .build()
        val listReq = ListObjectsRequest()
        val req = listReq.withBucketName(tenant).withPrefix(parentDir)
        val files = s3client.listObjects(req)
        for (objectSummary in files.objectSummaries) {
            if (!objectSummary.key.endsWith('/')) {
                s3client.deleteObject(tenant, objectSummary.key)
            }
        }
    }

    fun recursiveListFromS3(user: ApiUser, folder: String): List<String> {
        val tenant = if (user.tenant == "public") "taxreco" else user.objectStorageId
        val credentials = BasicAWSCredentials(
            accessKey, secretKey
        )
        val s3client = AmazonS3ClientBuilder
            .standard()
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.AP_SOUTH_1)
            .build()
        var prefix = folder
        val delimiter = "/"
        if (!prefix.endsWith(delimiter)) {
            prefix += delimiter
        }
        val paths: MutableList<String> = LinkedList()
        val request = ListObjectsRequest().withBucketName(tenant).withPrefix(prefix)
        var result: ObjectListing
        do {
            result = s3client.listObjects(request)
            for (summary in result.objectSummaries) {
                // Make sure we are not adding a 'folder'
                if (!summary.key.endsWith(delimiter)) {
                    paths.add(summary.key)
                }
            }
            request.marker = result.marker
        } while (result.isTruncated)
        return paths.reversed()
    }


}