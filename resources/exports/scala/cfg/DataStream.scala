package perfect.cfg

import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files

import scala.collection.JavaConverters._
import scala.collection.mutable.Buffer
import scala.collection.mutable.ListBuffer

object DataStream {

  def records(dataFile: String, inputEncoding: String): Buffer[DataStream] = {
    try {
      for(line <- Files.readAllLines(new File(dataFile).toPath, Charset.forName(inputEncoding)).asScala)
        yield {
          // java.String 遇到 "x,y,z," 这样的字符串,会split为3个,而不是4个元素
          val endWithEmpty = line.endsWith(",")
          val records = if(endWithEmpty) {
            val rs = (line + " ").split(",")
            rs(rs.length - 1) = ""
            rs
          } else {
            line.split(",")
          }
          new DataStream(records.toBuffer)
        }
    }
    catch {
      case e: Exception => {
        e.printStackTrace()
        throw new RuntimeException("data file:" + dataFile + " loads fail!")
      }
    }
  }
}

final class DataStream(val lines: Buffer[String]) {
  private var index: Int = 0

  private def getNext: String = {
    val cur = index
    index += 1
    if (cur < lines.size)
      lines(cur)
    else
      null
  }

  private def error(err: String) {
    throw new RuntimeException(s"$index $err")
  }

  private def getNextAndCheckNotEmpty : String = {
    val s = getNext
    if (s == null) error("read not enough")
    s
  }

  def getBool() : Boolean = {
    val s: String = getNextAndCheckNotEmpty.toLowerCase
    s match {
      case "true" => true
      case "false" => false
      case _ =>
        error(s + " isn't bool")
        false
    }
  }

  def getInt(): Int = {
    val s: String = getNextAndCheckNotEmpty
    s.toInt
  }

  def getLong() : Long = {
    val s: String = getNextAndCheckNotEmpty
    java.lang.Long.parseLong(s)
  }

  def getFloat() : Float = {
    val s: String = getNextAndCheckNotEmpty
    java.lang.Float.parseFloat(s)
  }

  def getString() : String = {
    getNextAndCheckNotEmpty.replace("#enter#", "\n").replace("#comma#", ",")
  }

  def getObject(name: String) : CfgObject = {
    try {
      Class.forName(name).getConstructor(classOf[DataStream]).newInstance(this).asInstanceOf[CfgObject]
    }
    catch {
      case e: Exception => throw new RuntimeException(e)
    }
  }
}