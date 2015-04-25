import scala.io.Source

import com.twitter.scalding._

import org.apache.hadoop.util.ToolRunner
import org.apache.hadoop.conf.Configuration

object Run {
  def main(args: Array[String]) {
    Run.run(args(0), "", args.tail)
  }

  def run(name: String, message: String, args: Array[String]) = {
    println(s"\n==== $name " + ("===" * 20))
    println(message)
    val argsWithName = name +: args
    println(s"Running: ${argsWithName.mkString(" ")}")
		ToolRunner.run(new Configuration, new Tool, argsWithName)
  }

  def printSomeOutput(outputFileName: String, message: String = "") = {
    if (message.length > 0) println(message)
    println("Output in $outputFileName:")
    Source.fromFile(outputFileName).getLines.take(10) foreach println
    println("...\n")
  }
}