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

object LogMessageReducer {

  def main(args: Array[String]) {

    if (args.length < 6) {
        System.err.println("Usage: LogMessageReducer <brokers> <consumerGroupId> <consumer-topic-stdout> <consumer-topic-stderr> <producer-reduced-topic> <checkpointsDir>\n"
                + "  <brokers> is a list of one or more Kafka brokers\n"
                + "  <consumerGroupId> is a consumer group name to consume from topics\n"
                + "  <onsumer-topic-stdout> the topic to listen for stdout messages\n"
                + "  <onsumer-topic-stderr> the topic to listen for stderr messages\n"
                + "  <producer-reduced-topic> the topic to put joined/reduced messages onto \n"
                + "  <checkpointDir> the location for spark streaming checkpoints\n"
                + "\n")
        System.exit(1)
    }

    val brokers=args(0)
    val consumerGroupId=args(1)
    val consumerTopicStdOut=args(2)
    val consumerTopicStdErr=args(3)
    val producerReducedTopic=args(4)
    val checkpointDir=args(5)

    val systemParams = s"systemParams: {brokers: $brokers consumerGroupId: $consumerGroupId consumerTopicStdOut: $consumerTopicStdOut consumerTopicStdErr: $consumerTopicStdErr producerReducedTopic: $producerReducedTopic checkpointDir: $checkpointDir}"
    println(systemParams)

    val spark = SparkSession
      .builder
      .appName("LogMessageReducer")
      .getOrCreate() // recover session from checkpoint if necessary

    import spark.implicits._
    import org.apache.spark.sql.streaming.{OutputMode, Trigger}
    import scala.concurrent.duration._

    // Create DataSet representing the stream of input lines from kafka
    spark
      .readStream
        .format("kafka")
        .option("kafka.bootstrap.servers", brokers)
        .option("subscribe", consumerTopicStdOut)
        .option("startingOffsets", "latest")
        .option("failOnDataLoss", "false")
        .load()
        .selectExpr("CAST(value AS STRING)")
        .flatMap(parseLog)
        .select("level","dateTime")
        .groupBy("level")
        .count()
        .orderBy("level")
      .writeStream
          .outputMode(OutputMode.Complete)
          .format("console")
          .start()
          .awaitTermination()

    // references for more advance aggregations:
    // https://stackoverflow.com/questions/39505599/spark-dataframe-does-groupby-after-orderby-maintain-that-order

    // // Create DataSet representing the stream of input lines from kafka
    // val stdouts = spark
    //   .readStream
    //   .format("kafka")
    //   .option("kafka.bootstrap.servers", brokers)
    //   .option("subscribe", consumerOutTopic)
    //   .option("startingOffsets", "earliest")
    //   .option("failOnDataLoss", "false")
    //   .load()
    //   .selectExpr("CAST(value AS STRING)")


    // ds.filter($"level" === "ERROR")
    //   .selectExpr("CAST(msg AS STRING) AS value")
    //   .writeStream
    //   .format("kafka")
    //   .option("kafka.bootstrap.servers", brokers)
    //   .option("checkpointLocation",s"$checkpointDir/reduce")
    //   .option("topic", producerReducedTopic)
    //   .outputMode("append")
    //   .start()
    //   .awaitTermination()

    spark.stop()

  }

}
