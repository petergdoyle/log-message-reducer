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

object LogTopicSplitter {

  // Case class defining structured data for a line of Apache access log data
  case class LogEntry(dateTime:String, level:String, thread:String, location:String, msg:String)

  def main(args: Array[String]) {

    if (args.length < 7) {
        System.err.println("Usage: LogTopicSplitter <brokers> <consumerGroupId> <consumer-topic> <producerErrTopic> <producerOutTopic> <checkpointsDir> <outputsDir>\n"
                + "  <brokers> is a list of one or more Kafka brokers\n"
                + "  <consumerGroupId> is a consumer group name to consume from topics\n"
                + "  <consumerTopic> the topic to listen for mixed log messages\n"
                + "  <producerErrTopic> the topic to put error messages onto \n"
                + "  <producerOutTopic> the topic to put non-error messages onto \n"
                + "  <checkpointDir> the location for spark streaming checkpoints\n"
                + "  <outputsDir> the location for any console output\n"
                + "\n")
        System.exit(1)
    }

    val brokers=args(0)
    val consumerGroupId=args(1)
    val consumerTopic=args(2)
    val producerErrTopic=args(3)
    val producerOutTopic=args(4)
    val checkpointDir=args(5)
    var outputsDir=args(6)

    val systemParams = s"systemParams: {brokers: $brokers consumerGroupId: $consumerGroupId consumerTopic: $consumerTopic producerErrTopic: $producerErrTopic checkpointDir: $checkpointDir outputsDir: $outputsDir}"
    println(systemParams)


    val spark = SparkSession
      .builder
      .appName("LogTopicSplitter")
      .getOrCreate() // recover session from checkpoint if necessary

    import spark.implicits._

    // Create DataSet representing the stream of input lines from kafka
    val df = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", brokers)
      .option("subscribe", consumerTopic)
      .option("startingOffsets", "earliest")
      //       Cannot fetch offset 2193 (GroupId: spark-kafka-source-5dc3a716-9542-44a5-af3e-f20b68bb4c42-1424840661-executor, TopicPartition: logs-1).
      // Some data may have been lost because they are not available in Kafka any more; either the
      //  data was aged out by Kafka or the topic may have been deleted before all the data in the
      //  topic was processed. If you don't want your streaming query to fail on such cases, set the
      //  source option "failOnDataLoss" to "false".
      .option("failOnDataLoss", "false")
      .load()
      .selectExpr("CAST(value AS STRING)")

    // Convert our raw text into a DataSet of LogEntry rows, then just select the columns we care about
    val ds = df.flatMap(parseLog).select("level","dateTime","msg")

    val outDs = ds.filter(not($"level" === "ERROR"))
    outDs
      .selectExpr("CAST(msg AS STRING) AS value")
      .writeStream // use `write` for batch, like DataFrame
      .format("kafka")
      .option("kafka.bootstrap.servers", brokers)
      .option("checkpointLocation",s"$checkpointDir/split/out")
      .option("topic", producerOutTopic)
      .outputMode("append")
      .start()
      .awaitTermination()

    // val query = outDs.writeStream
    //   .outputMode(OutputMode.Append)
    //   .format("console")
    //   .option("checkpointLocation",checkpointDir)
    //   .start()

    val errDs = ds.filter($"level" === "ERROR")
    errDs
      .selectExpr("CAST(msg AS STRING) AS value")
      .writeStream // use `write` for batch, like DataFrame
      .format("kafka")
      .option("kafka.bootstrap.servers", brokers)
      .option("checkpointLocation",s"$checkpointDir/split/err")
      .option("topic", producerErrTopic)
      .outputMode("append")
      .start()
      .awaitTermination()

    spark.stop()

  }

}
