package com.cleverfishsoftware.challenge.scala

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.sql.functions._
import org.apache.spark.sql._

import java.util.regex.Pattern
import java.util.regex.Matcher
import java.text.SimpleDateFormat
import java.util.Locale

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

object LogTopicsSplitter {

  // Case class defining structured data for a line of Apache access log data
  case class LogEntry(dateTime:String, level:String, thread:String, location:String, msg:String)

  def main(args: Array[String]) {

    if (args.length < 4) {
        System.err.println("Usage: LogTopicsSplitter <spark-master> <brokers> <groupId> <topic1,topic2,...> <batchsize> <threshold> <checkpointsDir> <violationsDir>\n"
                + "  <spark-master> is used by the spark context to determine how to execute\n"
                + "  <brokers> is a list of one or more Kafka brokers\n"
                + "  <groupId> is a consumer group name to consume from topics\n"
                + "  <topics> is a list of one or more kafka topics to consume from\n\n")
        System.exit(1)
    }

    val master=args(0)
    val brokers=args(1)
    val groupId=args(2)
    val topics=args(3)
    val checkpointDir="/spark/checkpoint"
    var errorsDir="./errors"

    def log4jLogPattern(): Pattern = {
      val dateTime = "(\\[.+?\\])?"
      val level = "(\\S+)"
      val thread = "(\\[.+?\\])"
      val location = "(\\S+)"
      val msg = "(.+)"
      val regex = s"$dateTime $level $thread $location - $msg"
      Pattern.compile(regex)
    }

    val datePattern = Pattern.compile("\\[(.*?) .+]") // will help out parse parts of a timestamp
    // Function to convert log timestamps to what Spark
    def parseDateField(field: String): Option[String] = {
    val matcher = datePattern.matcher(field)
      if (matcher.find) {
        val dateString = matcher.group(1)
        val dateFormat = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss", Locale.ENGLISH)
        val date = (dateFormat.parse(dateString))
        val timestamp = new java.sql.Timestamp(date.getTime());
        return Option(timestamp.toString())
      } else {
        None
      }
    }

    val logPattern = log4jLogPattern()
    // Convert a raw line of Apache access log data to a structured LogEntry object (or None if line is corrupt)
    def parseLog(x:Row) : Option[LogEntry] = {
      val s = Option(x.getString(0)).getOrElse("")
      val matcher:Matcher = logPattern.matcher(s);
      if (matcher.matches()) {
        return Some(LogEntry(
        parseDateField(matcher.group(1)).getOrElse(""),
        matcher.group(2),
        matcher.group(3),
        matcher.group(4),
        matcher.group(5)
        ))
      } else {
        return None
      }
    }

    val spark = SparkSession
      .builder
      .appName("LogTopicsJoiner")
      .getOrCreate() // recover session from checkpoint if necessary

    import spark.implicits._

    // Create DataSet representing the stream of input lines from kafka
    val stream = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", brokers)
      .option("subscribe", topics)
      .load()
      .selectExpr("CAST(value AS STRING)")

    // Convert our raw text into a DataSet of LogEntry rows, then just select the two columns we care about
    val structuredData = stream.flatMap(parseLog)

    val query = structuredData.writeStream
      .outputMode("complete")
      .format("console")
      .option("checkpointLocation",checkpointDir)
      .start()

    query.awaitTermination()

    spark.stop()

  }

}
