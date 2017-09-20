package com.kunyan

import java.util.Date

import _root_.kafka.serializer.StringDecoder
import com.alibaba.fastjson.JSON
import com.ibm.icu.text.SimpleDateFormat
import com.kunyan.conf.Platform
import com.kunyan.nlp.KunLP
import com.kunyan.nlp.task.NewsProcesser
import com.kunyan.util.{HbaseUtil, LazyConnections, MysqlUtil}
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkEnv}

import scala.xml.XML

/**
 * Created by lcm on 2017/7/17.
 * 新的新闻解析
 */
object Scheduler {


  def main(args: Array[String]) {

    val sparkConf = new SparkConf().setAppName("New_News")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.kryoserializer.buffer.max", "2000")

    val ssc = new StreamingContext(sparkConf, Seconds(60))

    val sparkContext = ssc.sparkContext

    val path = args(0)
    val xml = XML.loadFile(path)
    val lazyConn = LazyConnections(xml)
    val lazyConnBr = sparkContext.broadcast(lazyConn)

    // 初始化行业、概念、股票字典
    val newsProcesser = NewsProcesser(sparkContext, (xml \ "mysqlSen" \ "url").text)
    val newsProcesserBr = sparkContext.broadcast(newsProcesser)

    val groupId = (xml \ "kafka" \ "groupId").text
    val brokerList = (xml \ "kafka" \ "brokerList").text
    val topicsSet = Set[String]((xml \ "kafka" \ "newsreceive").text)
    val kafkaParams = Map[String, String]("metadata.broker.list" -> brokerList,
      "group.id" -> groupId)

    //信息
    try {
      val allMessages = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](
            ssc, kafkaParams, topicsSet)

      //信息处理
      allMessages.map(_._2).filter(_.length > 0).foreachRDD(rdd => {
        //
        rdd.foreach(message => {
          //

          val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
          val date = sdf.format(new Date())
          println(s"$date get kafka Topic message: " + message)
          analyzer(message,
            lazyConnBr.value,
            newsProcesserBr.value)

        })
      })
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }

    ssc.start()
    ssc.awaitTermination()
  }


  def analyzer(message: String, lazyConn: LazyConnections, newsProcesser: NewsProcesser): Unit = {

    try{

      val json = JSON.parseObject(message)
      //val jsonObj = new JSONObject(message)
      val platform = json.getString("platform")
      val tableName = json.getString("hbase_table_name")
      val url = json.getString("hbase_rowkey")

      val result = HbaseUtil.query(tableName, url, lazyConn)
      val time = result._1
      val content = result._2.replace("\"","").replace("“","").replace("”","")
      var title = result._3.replaceFirst("\"","“")
      title = title.replaceFirst("\"","”")
      title = title.replaceFirst("\"","“")
      title = title.replaceFirst("\"","”")

      val t1 = System.currentTimeMillis()
      if (!lazyConn.jedisExists(String.format("news:%s", title)) && !lazyConn.existSimilarKey(title)) {
        val t2 = System.currentTimeMillis()
        println("遍历redis查询标题耗时: " + (t2-t1))
        var digest = ""
        var tempDigest = ""

        if (content != "") {

          try {
            tempDigest = KunLP.getSummary(title, content)
          } catch {
            case e: Exception =>
              println("提取摘要异常")
          }

        }

        if (tempDigest != "") {
          digest = getFirstSignData(tempDigest, "\t")
        }

        val summary = interceptData(tempDigest, 300)
        val newDigest = interceptData(digest, 500)

        // 情感
        var sen = 1

        if (content != "") {

          val sentiment = KunLP.getSentiment(title, content)

          if (sentiment == "neg")
            sen = 0

        } else {
          sen = -1
        }

        // 行业
        val industry = newsProcesser.getIndustry(content)

        // 概念
        val section = newsProcesser.getSection(content)

        // 股票
        val stock = newsProcesser.getStock(content)


        var newsType = 0

        val platformId = platform.toInt

        if (platformId > 10000 && platformId < 20000)
          newsType = 1 //快讯
        else if (platformId > 60000 && platformId < 70000)
          newsType = 3 //研报
        else if (platformId > 50000 && platformId < 60000)
          newsType = 4 //公告

        val jsonId = System.currentTimeMillis() * 100 + SparkEnv.get.executorId.toInt
        val mysqlConn = lazyConn.mysqlNewsConn

        val pst = mysqlConn.prepareStatement("INSERT INTO news_info (n_id, type, platform, title, url, news_time, industry, section, stock, digest,summary, sentiment, updated_time, source)" +
          " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)")

        val ifInsert = MysqlUtil.insert(message, lazyConn, newsProcesser, pst,
          jsonId, newsType, platform, title,
          url, time, industry, section,
          stock, summary, newDigest, sen,
          System.currentTimeMillis(), Platform.apply(platformId).toString)

        if(ifInsert){
          //把title消息发到kafka  sentiment_title这个topic
          val message = KunLP.segment(title, isUseStopWords = false)
            .map(_.word).mkString(",")
          lazyConn.sendTask("sentiment_title",url+"\t"+message)
        }

        lazyConn.jedisSet(String.format("news:%s", title), url, 2 * 24 * 60 * 60)

      }


    }catch {
      case e:Exception =>
        e.printStackTrace()
    }

  }


  def getFirstSignData(data: String, sign: String): String = {

    if (data.contains(sign))
      data.substring(0, data.indexOf(sign))
    else data

  }

  def interceptData(content: String, number: Int): String = {

    var summary: String = ""

    if (content.length > number && number >= 0) {
      summary = content.substring(0, number)
    } else if (content.length < number) {
      summary = content
    } else {
      println("索引错误，结果不存在")
    }

    summary
  }

}
