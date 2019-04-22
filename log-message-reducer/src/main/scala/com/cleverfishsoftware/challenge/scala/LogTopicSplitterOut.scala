package com.cleverfishsoftware.challenge.scala

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.storage.StorageLevel
import org.apache.spark.sql.functions._
import org.apache.spark.sql._

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

import Utilities._

object LogTopicSplitterOut {

  def main(args: Array[String]) {

    if (args.length < 5) {
        System.err.println("Usage: LogTopicSplitterOut <brokers> <consumerGroupId> <consumer-topic> <producerOutTopic> <checkpointsDir> <outputsDir>\n"
                + "  <brokers> is a list of one or more Kafka brokers\n"
                + "  <consumerGroupId> is a consumer group name to consume from topics\n"
                + "  <consumerTopic> the topic to listen for mixed log messages\n"
                + "  <producerOutTopic> the topic to put non-error messages onto \n"
                + "  <checkpointDir> the location for spark streaming checkpoints\n"
                + "\n")
        System.exit(1)
    }

    val brokers=args(0)
    val consumerGroupId=args(1)
    val consumerTopic=args(2)
    val producerOutTopic=args(3)
    val checkpointDir=args(4)

    val systemParams = s"systemParams: {brokers: $brokers consumerGroupId: $consumerGroupId consumerTopic: $consumerTopic producerOutTopic: $producerOutTopic checkpointDir: $checkpointDir}"
    println(systemParams)


    val spark = SparkSession
      .builder
      .appName("LogTopicSplitterOut")
      .getOrCreate() // recover session from checkpoint if necessary

    import spark.implicits._

    // Create DataSet representing the stream of input lines from kafka
    val stream = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", brokers)
      .option("subscribe", consumerTopic)
      .option("startingOffsets", "earliest")
      .option("failOnDataLoss", "false")
      .load()
      .selectExpr("CAST(value AS STRING)")

    // Convert our raw text into a DataSet of LogEntry rows, then just select the columns we care about
    val ds = stream.flatMap(parseLog).select("level","dateTime","msg")

    ds.filter(not($"level" === "ERROR"))
      .selectExpr("CAST(msg AS STRING) AS value")
      .writeStream
      .format("kafka")
      .option("kafka.bootstrap.servers", brokers)
      .option("checkpointLocation",s"$checkpointDir/split/out")
      .option("topic", producerOutTopic)
      .outputMode("append")
      .start()
      .awaitTermination()

    spark.stop()

  }

}
