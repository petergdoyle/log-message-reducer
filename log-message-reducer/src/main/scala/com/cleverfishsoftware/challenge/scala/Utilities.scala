package com.cleverfishsoftware.challenge.scala

import java.util.regex.Pattern
import java.util.regex.Matcher
import java.text.SimpleDateFormat
import java.util.Locale

import org.apache.spark.sql._

object Utilities {

    // Case class defining structured data for a line of Apache access log data
    case class LogEntry(dateTime:String, level:String, thread:String, location:String, msg:String)

    val sample_msg = "[22/Apr/2019:05:32:46 +0000] DEBUG [pool-2-thread-1] LogMessage.java:83 - {\"level\":\"debug\",\"trackId\":\"5b952441-878d-4cfe-828f-578b5a960d58\",\"body\":\"menandri viverra vel dissentiunt homero esse an reprehendunt labores aliquam habitasse perpetua dictas\"}"

    def log4jLogPattern(): Pattern = {
      val regexx = "(\\[.+?\\])? (\\S+) (.+) (.+) - (.+)"
      Pattern.compile(regexx)
    }

    // def log4jLogPattern(): Pattern = {
    //   val dateTime = "(\\[.+?\\])?"
    //   val level = "(\\S+)"
    //   val thread = "(\\[.+?\\])"
    //   val location = "(\\S+)"
    //   val msg = "(.+)"
    //   val regex = s"$dateTime $level $thread $location - $msg"
    //   Pattern.compile(regex)
    // }

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
    def parseLog(row:Row) : Option[LogEntry] = {
      val s = Option(row.getString(0).trim).getOrElse("")
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
        // val matcher2:Matcher = logPattern.matcher(row.getString(0).trim);
        // println(matcher2.matches())
        // // println("[CANNOT PARSE] logPattern: " + logPattern + " row size: "+ row.size + " row: " + row.getString(0))
        // // println("error cannot match")
        return None
        // return Some(LogEntry(
        // "error",
        // "error",
        // "error",
        // "error",
        // "error"
        // ))
      }
    }

}
