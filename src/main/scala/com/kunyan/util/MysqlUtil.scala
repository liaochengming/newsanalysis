package com.kunyan.util

import java.sql.{PreparedStatement, SQLException}

import com.kunyan.Scheduler
import com.nlp.TitleDeduplication
import com.nlp.util.{EasyParser, NewsProcesser}

import scala.collection.mutable.ListBuffer

/**
  * Created by yangshuai on 2016/5/26.
  */
object MysqlUtil {

  val messageList = new ListBuffer[String]

  def insert(message:String,lazyConn: LazyConnections, newsProcesser: NewsProcesser, prep:PreparedStatement,easyParser: EasyParser,titleDeduplication: TitleDeduplication, params: Any*): Boolean = {

    try {

      for (i <- params.indices) {

        val param = params(i)

        param match {
          case param: String =>
            prep.setString(i + 1, param)
          case param: Int =>
            prep.setInt(i + 1, param)
          case param: Boolean =>
            prep.setBoolean(i + 1, param)
          case param: Long =>
            prep.setLong(i + 1, param)
          case param: Double =>
            prep.setDouble(i + 1, param)
          case _ =>
            println("Unknown Type")
        }
      }

      prep.executeUpdate

      println("Insertion finish")

      for(mess <- messageList){
        Scheduler.analyzer(mess,lazyConn,newsProcesser,easyParser,titleDeduplication)
        messageList.remove(messageList.indexOf(mess))
      }

      true

    } catch {

      case sqle: SQLException =>

        sqle.printStackTrace()
        println("向MySQL插入数据失败")
        false

      case e:Exception =>
        e.printStackTrace()
        println("向MySQL插入数据失败")

        if(messageList.size > 20000){
          println("历史丢失数据超过20000条")
        }else{
          messageList.+=(message)
          val size = messageList.size
          println(s"当前缺失新闻$size"+"条")
        }

        false


    }

  }

}
