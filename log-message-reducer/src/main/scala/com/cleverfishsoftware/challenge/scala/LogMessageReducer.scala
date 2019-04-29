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

object LogMessageReducer {

  def main(args: Array[String]) {

    if (args.length < 6) {
        System.err.println("Usage: LogMessageReducer <brokers> <consumerGroupId> <consumer-topic-stdout> <consumer-topic-stderr> <producer-reduced-topic> <checkpointsDir>\n"
                + "  <brokers> is a list of one or more Kafka brokers\n"
                + "  <consumerGroupId> is a consumer group name to consume from topics\n"
                + "  <consumer-topic-stdout> the topic to listen for stdout messages\n"
                + "  <consumer-topic-stderr> the topic to listen for stderr messages\n"
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
    import org.apache.spark.sql.types.{DataTypes, StructType}
    import scala.concurrent.duration._

    val struct = new StructType()
      .add("level", DataTypes.StringType)
      .add("trackId", DataTypes.StringType)
      .add("body", DataTypes.StringType)
      .add("ts", DataTypes.StringType)

    val stdOutDf = spark
      .readStream
        .format("kafka")
        .option("kafka.bootstrap.servers", brokers)
        .option("subscribe", consumerTopicStdOut)
        .option("startingOffsets", "latest")
        .option("failOnDataLoss", "false")
        .load()
        .selectExpr("CAST(value AS STRING)") // take the "value" field from the Kafka ConsumerRecord
        .select(from_json($"value", struct) as("stdout")) // convert to json objects
        .select("stdout.*")


    val stdErrDf = spark
      .readStream
        .format("kafka")
        .option("kafka.bootstrap.servers", brokers)
        .option("subscribe", consumerTopicStdErr)
        .option("startingOffsets", "latest")
        .option("failOnDataLoss", "false")
        .load()
        .selectExpr("CAST(value AS STRING)") // take the "value" field from the Kafka ConsumerRecord
        .select(from_json($"value", struct) as("stderr")) // convert to json objects
        .select("stderr.*")


    val joinedDf = stdErrDf.alias("err").join(stdOutDf.alias("out"),Seq("trackId"))
        // val joinedDf = stdErrDf.withWatermark(“eventTime1”, “10 seconds).join(stdOutDf,"trackId")
        .select("err.trackId","out.level","out.body","out.ts")


    joinedDf
      // .groupBy("trackId")
      // .count()
      .writeStream
        .format("console")
        .option("checkpointLocation",s"$checkpointDir/reducer")
        .option("truncate", false)
        .trigger(Trigger.ProcessingTime(5.seconds))
        .outputMode(OutputMode.Append)
        // .outputMode(OutputMode.Complete)
        .start()
        .awaitTermination



    spark.stop()

  }

}
