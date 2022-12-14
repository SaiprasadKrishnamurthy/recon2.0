package com.taxreco.recon.engine.repository

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.vincentrussell.query.mongodb.sql.converter.QueryConverter
import com.taxreco.recon.engine.model.DataSource
import com.taxreco.recon.engine.model.TransactionRecord
import org.bson.Document
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Repository

@Repository
class TransactionRecordRepository(private val mongoTemplate: MongoTemplate) {

    fun getTransactionRecords(dataSource: DataSource, bucketValue: String): List<TransactionRecord> {
        val predicate = "attrs.${dataSource.bucketField}='${bucketValue.trim()}' AND ${dataSource.predicate}"
        val queryConverter = QueryConverter.Builder()
            .sqlString("select * from transactionRecord where $predicate")
            .build()

        val mongoDBQueryHolder = queryConverter.mongoQuery
        val collection = mongoTemplate.getCollection(mongoDBQueryHolder.collection)
        val mapper = jacksonObjectMapper()
        return collection.find(mongoDBQueryHolder.query)
            .projection(mongoDBQueryHolder.projection)
            .map { a: Document -> a }
            .map { mapper.readValue(it.toJson(), TransactionRecord::class.java) }
            .toList()
    }

    fun getBuckets(dataSource: DataSource, bucketValue: String): List<TransactionRecord> {
        val queryConverter = QueryConverter.Builder()
            .sqlString("select * from transactionRecord where ${dataSource.predicate}")
            .build()

        val mongoDBQueryHolder = queryConverter.mongoQuery
        val collection = mongoTemplate.getCollection(mongoDBQueryHolder.collection)
        val mapper = jacksonObjectMapper()
        return collection.find(mongoDBQueryHolder.query)
            .projection(mongoDBQueryHolder.projection)
            .map { a: Document -> a }
            .map { mapper.readValue(it.toJson(), TransactionRecord::class.java) }
            .toList()
    }

    fun findBuckets(ds: DataSource): List<String> {
        val queryConverter = QueryConverter.Builder()
            .sqlString("select DISTINCT attrs.${ds.bucketField} from transactionRecord where ${ds.predicate}")
            .build()

        val mongoDBQueryHolder = queryConverter.mongoQuery
        val collection = mongoTemplate.getCollection(mongoDBQueryHolder.collection)
        return collection.distinct(
            "attrs.${ds.bucketField}",
            mongoDBQueryHolder.query.toBsonDocument(),
            String::class.java
        )
            .toList()
    }
}