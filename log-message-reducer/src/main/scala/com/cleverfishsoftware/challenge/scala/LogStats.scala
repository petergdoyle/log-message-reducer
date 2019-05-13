package com.cleverfishsoftware.challenge.scala

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.sql.functions._
import org.apache.spark.sql._

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

import Utilities._

object LogStats {

  def main(args: Array[String]) {

    if (args.length < 4) {
        System.err.println("Usage: LogStats <brokers> <consumerGroupId> <topic> <checkpointsDir>\n"
                + "  <brokers> is a list of one or more Kafka brokers\n"
                + "  <consumerGroupId> is a consumer group name to consume from topics\n"
                + "  <topic> the topic to listen for mixed log messages\n"
                + "  <checkpointDir> the location for spark streaming checkpoints\n"
                + "\n")
        System.exit(1)
    }

    val brokers=args(0)
    val consumerGroupId=args(1)
    val topic=args(2)
    val checkpointDir=args(3)

    val systemParams = s"systemParams: {brokers: $brokers consumerGroupId: $consumerGroupId topic: $topic checkpointDir: $checkpointDir}"
    println(systemParams)

    val spark = SparkSession
      .builder
      .appName("LogStats")
      .getOrCreate() // recover session from checkpoint if necessary

    import spark.implicits._
    import org.apache.spark.sql.streaming.{OutputMode, Trigger}
    import scala.concurrent.duration._

    spark
      .readStream
        .format("kafka")
        .option("kafka.bootstrap.servers", brokers)
        .option("group.id",consumerGroupId)
        .option("subscribe", topic)
        .option("startingOffsets", "earliest")
        .option("failOnDataLoss", "false")
        .load()
        .selectExpr("CAST(value AS STRING)")
        .flatMap(parseLog).select("level")
        .groupBy("level")
        .count()
      .writeStream
        .format("console")
        // .option("checkpointLocation",s"$checkpointDir/stats") // removed checkpoints
        // .option("truncate", false)
        // .trigger(Trigger.ProcessingTime(5.seconds))
        // .outputMode(OutputMode.Complete)
        .start()
        .awaitTermination

    // val structured = stream.flatMap(parseLog).select("level","dateTime")
    //
    // val windowed = structured
    //   .groupBy(window($"dateTime","1 seconds"), $"level")
    //   .count()
    //   .orderBy("window")
    //
    // // dump records to the console every 5 seconds
    // val query = windowed.writeStream
    //   .format("console")
    //   .option("checkpointLocation",s"$checkpointDir/stats")
    //   .option("truncate", false)
    //   .trigger(Trigger.ProcessingTime(5.seconds))
    //   .outputMode(OutputMode.Complete)
    //   .start()
    //   .awaitTermination

    spark.stop()

  }

}
