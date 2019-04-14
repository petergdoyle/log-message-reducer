package com.cleverfishsoftware.challenge.scala

import java.util.regex.Pattern
import java.util.regex.Matcher
import java.text.SimpleDateFormat
import java.util.Locale

import org.apache.spark.sql._

import java.util.regex.Pattern
import java.util.regex.Matcher
import java.text.SimpleDateFormat
import java.util.Locale

object Utilities {

    // Case class defining structured data for a line of Apache access log data
    case class LogEntry(dateTime:String, level:String, thread:String, location:String, msg:String)

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

}
